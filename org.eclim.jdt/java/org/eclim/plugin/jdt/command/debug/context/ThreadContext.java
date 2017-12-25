/**
 * Copyright (C) 2014 - 2017  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.debug.context;

import java.util.Collection;
import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.debug.core.DebugException;

import org.eclipse.jdt.debug.core.IJavaThread;

/**
 * Maintains the state of all threads belonging to a debug session.
 */
public class ThreadContext
{
  /**
   * Thread that is being currently stepped through. There can be more than one
   * thread suspended in some breakpoint ready to be stepped through. But only
   * one of them can be stepped through at a time. This field holds that
   * thread.
   */
  private IJavaThread steppingThread = null;

  private Map<Long, IJavaThread> threadMap =
    new ConcurrentHashMap<Long, IJavaThread>();

  public synchronized IJavaThread getSteppingThread()
  {
    if (steppingThread != null) {
      // Check if it is still valid thread and in suspended state
      if (steppingThread.isSuspended()) {
        return steppingThread;
      }
    }

    // Find the first suspended thread and set it as stepping thread
    steppingThread = null;
    for (Map.Entry<Long, IJavaThread> entry : threadMap.entrySet()) {
      if (entry.getValue().isSuspended()) {
        steppingThread = entry.getValue();
      }
    }

    return steppingThread;
  }

  /**
   * Explicitly sets the stepping thread.
   *
   * @param thread The IJavaThread
   */
  public synchronized void setSteppingThread(IJavaThread thread)
  {
    this.steppingThread = thread;
  }

  public Collection<IJavaThread> getThreads()
  {
    return threadMap.values();
  }

  public IJavaThread getThread(long threadId)
  {
    return threadMap.get(threadId);
  }

  public synchronized void update(IJavaThread thread)
    throws DebugException
  {

    long threadId = thread.getThreadObject().getUniqueId();
    threadMap.put(threadId, thread);
  }

  public synchronized void remove(IJavaThread thread)
    throws DebugException
  {
    long threadId = thread.getThreadObject().getUniqueId();
    threadMap.remove(threadId);

    if (steppingThread != null &&
        steppingThread.getThreadObject().getUniqueId() == threadId)
    {
      steppingThread = null;
    }
  }

  /**
   * Suspends the given thread.
   *
   * @param threadId The id of the thread to suspend.
   * @throws DebugException on failure.
   */
  public void suspend(long threadId)
    throws DebugException
  {
    IJavaThread thread = threadMap.get(threadId);
    if (thread != null) {
      thread.suspend();
    }
  }

  /**
   * Resumes execution of the given thread.
   *
   * @param threadId The id of the thread to resume.
   * @throws DebugException on failure.
   */
  public void resume(long threadId)
    throws DebugException
  {
    IJavaThread thread = threadMap.get(threadId);
    if (thread != null) {
      thread.resume();
    }
  }
}
