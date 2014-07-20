/**
 * Copyright (C) 2005 - 2014  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.debug;

import java.util.HashMap;
import java.util.Map;

import org.eclim.logging.Logger;

import org.eclim.plugin.core.util.VimClient;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;

import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IVariable;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import org.eclipse.jdt.internal.launching.SocketAttachConnector;

import org.eclipse.jdt.launching.sourcelookup.JavaSourceLocator;

/**
 * Maintains the state of currently active debug session and exposes methods to
 * interact it.
 *
 * <p>
 * This class exposes a singleton instance of the context that can be
 * reinitialized for each session. This is to allow maitaining the state of a
 * single session across several VIM invocations. At this time, there can only
 * be one debug session active at any point of time.
 */
public class DebuggerContext
{
  private static final Logger logger = Logger.getLogger(DebuggerContext.class);

  private static final String KEY_HOSTNAME = "hostname";
  private static final String KEY_PORT = "port";

  /**
   * Singleton instance of the context.
   */
  private static final DebuggerContext context = new DebuggerContext();

  /**
   * Debug target for currently active session. This is reinitialized by
   * <code>createDebugTarget</code>.
   */
  private IDebugTarget debugTarget;

  private VimClient vimClient;

  private IVariable[] vars;

  private DebuggerContext()
  {
    DebugPlugin.getDefault().addDebugEventListener(new DebugEventSetListener());
  }

  public static DebuggerContext getInstance()
  {
    return context;
  }

  public IDebugTarget getDebugTarget()
  {
    return debugTarget;
  }

  public VimClient getVimClient()
  {
    return vimClient;
  }

  /**
   * Starts the debug session by creating the debug target with given parameters.
   */
  public void start(String debugTargetName, String host,
      String port, String vimInstanceId) throws CoreException
  {

    if (logger.isInfoEnabled()) {
      logger.info("Creating debug target: " + debugTargetName + " at " +
          host + ":" + port + " . VIM instance: " + vimInstanceId);
    }

    ILaunchConfiguration config = null;

    // TODO This is a hack to create source locator. Using all projects in the
    // workspace.
    IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
    IJavaProject[] jProjects = new IJavaProject[projects.length];
    for (int i = 0; i < projects.length; i++) {
      jProjects[i] = JavaCore.create(projects[i]);
    }
    // TODO Switch to newer approach of source code lookup
    ISourceLocator srcLocator = new JavaSourceLocator(jProjects, true);

    ILaunch launch = new Launch(config, ILaunchManager.DEBUG_MODE, srcLocator);
    IProgressMonitor monitor = null;
    Map<String, String> args = new HashMap<String, String>();
    args.put(KEY_HOSTNAME, host);
    args.put(KEY_PORT, port);

    SocketAttachConnector connector = new SocketAttachConnector();
    try {
      connector.connect(args, monitor, launch);
    } catch (CoreException e) {
      throw new RuntimeException("Debug VM not available at " + host + ":" +
          port + ". Check hostname and port number.");
    }

    this.debugTarget = launch.getDebugTarget();
    this.vimClient = new VimClient(vimInstanceId);
  }

  /**
   * Suspends the debug session.
   */
  public void suspend() throws DebugException
  {
    if (logger.isInfoEnabled()) {
      logger.info("Suspending debug session");
    }
    debugTarget.suspend();
  }

  /**
   * Terminates the debug session.
   */
  public void stop() throws DebugException
  {
    if (logger.isInfoEnabled()) {
      logger.info("Stopping debug session");
    }
    debugTarget.terminate();

    debugTarget = null;
    vimClient = null;
  }

  /**
   * Resumes execution from the current breakpoint.
   */
  public void resume() throws DebugException
  {
    if (logger.isInfoEnabled()) {
      logger.info("Resuming breakpoint");
    }
    debugTarget.resume();
  }

  public void setVariables(IVariable[] vars)
  {
    this.vars = vars;
  }

  public IVariable[] getVariables()
  {
    return this.vars;
  }
}
