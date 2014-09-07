/**
 * Copyright (C) 2014  Eric Van Dewoestine
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclim.logging.Logger;

import org.eclim.plugin.core.util.VimClient;

import org.eclim.plugin.jdt.command.debug.event.DebugEventSetListener;

import org.eclim.plugin.jdt.command.debug.ui.ViewUtils;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclim.util.CollectionUtils;

import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;

import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ISourceLocator;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import org.eclipse.jdt.internal.launching.SocketAttachConnector;

import org.eclipse.jdt.launching.sourcelookup.JavaSourceLocator;

/**
 * Maintains the state of currently active debug session and exposes methods to
 * interact with it.
 */
public class DebuggerContext
{
  private static final Logger logger = Logger.getLogger(DebuggerContext.class);

  private static final String KEY_HOSTNAME = "hostname";
  private static final String KEY_PORT = "port";

  private IProject project;

  private String host;

  private int port;

  /**
   * ID of this context obtained by concatenating user input.
   * This should be unique across all active contexts.
   */
  private String id;

  private volatile DebuggerState state = DebuggerState.CONNECTING;

  private final ThreadContext threadCtx;
  private final VariableContext varCtx;

  /**
   * Debug target for currently active session.
   */
  private IDebugTarget debugTarget;

  private VimClient vimClient;

  private IDebugEventSetListener listener = new DebugEventSetListener();

  /**
   * Starts the debug session by creating the debug target with given parameters.
   */
  public DebuggerContext(
      IProject project,
      String host,
      int port,
      String vimInstanceId)
    throws Exception
  {
    this.project = project;
    this.host = host;
    this.port = port;
    this.id = project.getName() + " - " + host + ":" + port;
    this.vimClient = new VimClient(vimInstanceId);

    this.threadCtx = new ThreadContext();
    this.varCtx = new VariableContext(threadCtx);
  }

  public void start()
    throws Exception
  {
    logger.info(
        "Starting debug session at " + host + ":" + port +
        " for vim instance: " + vimClient.getId());

    // Setup listener before connecting to VM so that events are not missed
    DebugPlugin.getDefault().addDebugEventListener(listener);

    ILaunchConfiguration config = null;

    ArrayList<IProject> projects = new ArrayList<IProject>();
    projects.add(project);
    CollectionUtils.addAll(projects, project.getReferencedProjects());
    CollectionUtils.addAll(projects, project.getReferencingProjects());

    ArrayList<IJavaProject> javaProjects = new ArrayList<IJavaProject>();
    for (IProject p : projects) {
      if (p.exists() && p.hasNature(JavaCore.NATURE_ID)){
        javaProjects.add(JavaUtils.getJavaProject(p));
      }
    }
    ISourceLocator srcLocator = new JavaSourceLocator(
        javaProjects.toArray(new IJavaProject[javaProjects.size()]), true);

    ILaunch launch = new Launch(config, ILaunchManager.DEBUG_MODE, srcLocator);
    IProgressMonitor monitor = null;
    Map<String, String> args = new HashMap<String, String>();
    args.put(KEY_HOSTNAME, host);
    args.put(KEY_PORT, String.valueOf(port));

    SocketAttachConnector connector = new SocketAttachConnector();
    try {
      connector.connect(args, monitor, launch);
    } catch (CoreException e) {
      throw new RuntimeException(
          "Debug VM not available at " + host + ":" + port + ". " +
          "Check hostname and port number.");
    }

    this.debugTarget = launch.getDebugTarget();
  }

  /**
   * Suspends the debug session.
   */
  public void suspend()
    throws DebugException
  {
    logger.info("Suspending debug session");

    if (debugTarget != null) {
      debugTarget.suspend();
    }
  }

  /**
   * Disconnects the debug session.
   */
  public void stop()
    throws DebugException
  {
    logger.info("Stopping debug session");

    if (debugTarget != null) {
      debugTarget.disconnect();
    }

    clear();
  }

  /**
   * Terminates the debug session.
   */
  public void terminate()
    throws DebugException
  {
    logger.info("Terminating debug session");
    if (debugTarget != null) {
      debugTarget.terminate();
    }

    clear();
  }

  /**
   * Resumes execution from the current breakpoint.
   */
  public void resume()
    throws DebugException
  {
    logger.debug("Resuming breakpoint");
    if (debugTarget != null) {
      debugTarget.resume();
    }
  }

  private void clear()
  {
    DebugPlugin.getDefault().removeDebugEventListener(listener);
  }

  public Map<String, Object> getStatus()
    throws DebugException
  {
    Map<String, Object> statusMap = new HashMap<String, Object>();

    statusMap.put("state", ViewUtils.EXPANDED_TREE_SYMBOL + getId() +
        " (" + state.getName() + ")");
    statusMap.put("threads", getThreadContext().get());
    statusMap.put("variables", getVariableContext().get());

    return statusMap;
  }

  public DebuggerState getState()
  {
    return this.state;
  }

  public void setState(DebuggerState state)
  {
    this.state = state;
  }

  public String getId()
  {
    return this.id;
  }

  public VariableContext getVariableContext()
  {
    return varCtx;
  }

  public ThreadContext getThreadContext()
  {
    return threadCtx;
  }

  public IDebugTarget getDebugTarget()
  {
    return debugTarget;
  }

  public VimClient getVimClient()
  {
    return vimClient;
  }
}
