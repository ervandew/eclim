/**
 * Copyright (C) 2014 - 2017 Daniel Leong
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.eclim.plugin.core.command.project;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IStreamListener;

import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;

/**
 * Manages Launches started by ProjectRunCommand
 */
public class EclimLaunchManager implements Runnable
{
  private static HashMap<String, LaunchSet> sLaunches =
    new HashMap<String, LaunchSet>();
  private static Thread thread;

  @Override
  public void run()
  {
    while (true) {
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        // we're probably done
        thread = null;
        break;
      }

      cleanLaunches();
    }
  }

  private static synchronized void cleanLaunches()
  {
    Iterator<LaunchSet> iter = sLaunches.values().iterator();
    while (iter.hasNext()) {
      final LaunchSet set = iter.next();
      if (set == null) {
        continue;
      }

      if (set.launch.isTerminated()) {
        iter.remove();
        set.output.sendTerminated();
      }
    }
  }

  public static synchronized boolean isRunning(final String launchId)
  {
    return sLaunches.containsKey(launchId);
  }

  public static synchronized boolean terminate(final String launchId)
    throws DebugException
  {
    LaunchSet set = sLaunches.remove(launchId);
    if (set == null) {
      return false;
    }

    terminate(set);
    return true;
  }

  private static void terminate(final LaunchSet set)
    throws DebugException
  {
    if (set.launch.isTerminated()) {
      // already terminated... consider success?
      return;
    }

    set.launch.terminate();
    set.output.sendTerminated();
  }

  public static synchronized void terminateAll()
  {
    Iterator<LaunchSet> iter = sLaunches.values().iterator();
    while (iter.hasNext()) {
      LaunchSet set = iter.next();
      try {
        terminate(set);
      } catch (DebugException e) {
        // just keep moving
      }
      iter.remove();
    }
  }

  /**
   * @param launch ILaunch instance.
   * @param output OutputHandler instance.
   * @throws IllegalArgumentException if the OutputHandler
   *  provided doesn't support async output and we're trying
   *  to perform it. The original exception can be retreived
   *  from #getCause()
   */
  public static synchronized void manage(
      final ILaunch launch,
      final OutputHandler output)
    throws IllegalArgumentException
  {
    final IProcess[] procs = launch.getProcesses();
    if (procs == null || procs.length == 0) {
      // we're done here
      return;
    }

    // attach NOW so we don't miss anything
    IStreamListener errListener = new IStreamListener()
    {
      @Override
      public void streamAppended(String text, IStreamMonitor monitor)
      {
        output.sendErr(text);
      }
    };

    IStreamListener outListener = new IStreamListener()
    {
      @Override
      public void streamAppended(String text, IStreamMonitor monitor)
      {
        output.sendOut(text);
      }
    };

    for (final IProcess proc : procs) {
      IStreamMonitor stdout = proc.getStreamsProxy().getOutputStreamMonitor();
      IStreamMonitor stderr = proc.getStreamsProxy().getErrorStreamMonitor();

      // dump buffered content, if any
      final String pendingOut = stdout.getContents();
      final String pendingErr = stderr.getContents();
      if (pendingOut.length() > 0){
        output.sendOut(pendingOut);
      }
      if (pendingErr.length() > 0){
        output.sendErr(pendingErr);
      }

      // attach listeners
      stdout.addListener(outListener);
      stderr.addListener(errListener);
    }

    final String id = allocateId(launch);

    // procs remaining; prepare the output
    try {
      output.prepare(id);
    } catch (final Exception e) {
      remove(id);
      try {
        launch.terminate();
      } catch (final DebugException e2) {
        // we're quitting anyway
      }

      // re-raise
      throw new IllegalArgumentException(
          "OutputHandler does not support async output", e);
    }

    sLaunches.put(id, new LaunchSet(launch, output));

    if (thread == null) {
      thread = new Thread(new EclimLaunchManager());
      thread.setDaemon(true);
      thread.start();
    }
  }

  private static synchronized String allocateId(ILaunch launch)
  {
    final String name = launch.getLaunchConfiguration().getName();
    if (!sLaunches.containsKey(name)) {
      // reserve
      sLaunches.put(name, null);
      return name;
    }

    final int token = 1;
    String id;
    do {
      id = String.format("%s:%d", name, token);
    } while (sLaunches.containsKey(id));

    sLaunches.put(id, null);
    return id;
  }

  private static synchronized void remove(String id)
  {
    sLaunches.remove(id);
  }

  public interface OutputHandler
  {
    /**
     * If your OutputHandler's prepare might take a non-trivial amount of time,
     * you MUST queue up any outputs that come along before you started. Future
     * work could abstract that out as necessary
     *
     * @param launchId The launch id.
     */
    public void prepare(final String launchId);
    public void sendErr(String line);
    public void sendOut(String line);
    public void sendTerminated();
  }

  private static class LaunchSet
  {
    final ILaunch launch;
    final OutputHandler output;

    LaunchSet(ILaunch launch, OutputHandler output)
    {
      this.launch = launch;
      this.output = output;
    }
  }
}
