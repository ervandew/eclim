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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclim.logging.Logger;

import org.eclim.plugin.core.util.VimClient;

import org.eclim.plugin.jdt.command.debug.event.DebugEventSetListener;

import org.eclim.plugin.jdt.command.debug.ui.ThreadView;
import org.eclim.plugin.jdt.command.debug.ui.VariableView;
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
  private final ThreadView threadView;
  private final VariableView varView;

  /**
   * Debug target for currently active session.
   */
  private IDebugTarget debugTarget;

  private VimClient vimClient;

  private IDebugEventSetListener listener = new DebugEventSetListener();

  /**
   * Starts the debug session by creating the debug target with given parameters.
   *
   * @param project The IProject.
   * @param host The host to bind the debugging session to.
   * @param port The port to bind the debugging session to.
   * @param vimInstanceId The id of the vim instance to communicate with.
   */
  public DebuggerContext(
      IProject project,
      String host,
      int port,
      String vimInstanceId)
  {
    this.project = project;
    this.host = host;
    this.port = port;
    this.id = project.getName() + " - " + host + ":" + port;
    this.vimClient = new VimClient(vimInstanceId);

    this.threadCtx = new ThreadContext();
    this.threadView = new ThreadView(threadCtx);
    this.varView = new VariableView(threadCtx);
  }

  public void start()
  {
    logger.info(
        "Starting debug session at " + host + ":" + port +
        " for vim instance: " + vimClient.getId());

    // Setup listener before connecting to VM so that events are not missed
    DebugPlugin.getDefault().addDebugEventListener(listener);

    ILaunchConfiguration config = null;
    ISourceLocator srcLocator = null;

    ArrayList<IProject> projects = new ArrayList<IProject>();
    ArrayList<IJavaProject> javaProjects = new ArrayList<IJavaProject>();
    projects.add(project);

    try{
      CollectionUtils.addAll(projects, project.getReferencedProjects());
      CollectionUtils.addAll(projects, project.getReferencingProjects());

      for (IProject p : projects) {
        if (p.exists() && p.hasNature(JavaCore.NATURE_ID)){
          javaProjects.add(JavaUtils.getJavaProject(p));
        }
      }
      srcLocator = new JavaSourceLocator(
          javaProjects.toArray(new IJavaProject[javaProjects.size()]), true);
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }

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
   *
   * @throws DebugException on failure.
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
   *
   * @throws DebugException on failure.
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
   *
   * @throws DebugException on failure.
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
   *
   * @throws DebugException on failure.
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

    statusMap.put("project", project.getName());
    statusMap.put("state", ViewUtils.EXPANDED_NODE_SYMBOL + getId() +
        " (" + state.getName() + ")");
    statusMap.put("threads", threadView.get());
    statusMap.put("variables", varView.get());

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

  public ThreadContext getThreadContext()
  {
    return threadCtx;
  }

  public ThreadView getThreadView()
  {
    return threadView;
  }

  public VariableView getVariableView()
  {
    return varView;
  }

  public IDebugTarget getDebugTarget()
  {
    return debugTarget;
  }

  public void jumpToFilePosition(String fileName, int lineNum)
    throws Exception
  {
    vimClient.remoteFunctionCall(
        "eclim#java#debug#GoToFile",
        fileName, String.valueOf(lineNum));
  }

  public void refreshDebugStatus()
    throws Exception
  {
    vimClient.remoteSend(":JavaDebugStatus");
  }

  public void signalSessionTermination()
    throws Exception
  {
    vimClient.remoteFunctionCall("eclim#java#debug#SessionTerminated");
  }

  public void updateThreadView(long threadId, String kind, List<String> results)
    throws Exception
  {
    vimClient.remoteFunctionCall(
        "eclim#java#debug#ThreadViewUpdate",
        String.valueOf(threadId), kind, concatenateList(results));
  }

  public void updateVariableView(List<String> results)
    throws Exception
  {
    vimClient.remoteFunctionCall(
        "eclim#java#debug#VariableViewUpdate",
        concatenateList(results));

    // Hack to force VIM to execute the previous remote send command.
    // In some cases, VIM seems to buffer the command and not execute
    // until the next remote command is sent.
    vimClient.remoteSend(":echo ' '");
  }

  /**
   * Returns a string by concatenating all the given entries using <eol> as
   * delimiter.
   */
  private String concatenateList(List<String> entries)
  {
    if (entries == null || entries.isEmpty()) {
      // Return an empty string since the remote VIM command expects an arg
      return "\\ ";
    }

    StringBuilder sb = new StringBuilder();
    for (String entry : entries){
      sb.append(entry.replaceAll(" ", "\\\\ "));
      sb.append("<eol>");
    }

    return sb.toString();
  }
}
