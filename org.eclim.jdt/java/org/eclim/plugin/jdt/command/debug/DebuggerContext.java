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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclim.logging.Logger;

import org.eclim.util.CommandExecutor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;

import org.eclipse.debug.core.model.IDebugTarget;

import org.eclipse.jdt.core.dom.Message;

import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpointListener;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.JDIDebugModel;

import org.eclipse.jdt.internal.launching.SocketAttachConnector;

/**
 * Maintains the state of currently active debug session.
 * This class exposes a singleton instance of the context that can be
 * reinitialized for each session. This is to allow maitaining the state of a
 * single session across several VIM invocations.
 */
public class DebuggerContext {
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

  /**
   * VIM server instance to connect to send commands.
   *
   * TODO Pass this as argument from VIM and set in createDebugTarget?
   */
  private String vimInstanceId = "default";

  private DebuggerContext() {
    JDIDebugModel.addJavaBreakpointListener(new BreakpointListener());
  }

  public static DebuggerContext getInstance() {
    return context;
  }

  /**
   * Starts the debug session by creating the debug target with given parameters.
   */
  public void createDebugTarget(String debugTargetName, String host,
      String port) throws CoreException {

    if (logger.isInfoEnabled()) {
      logger.info("Creating debug target " + debugTargetName + " at "
          + host + ":" + port);
    }

    // TODO Figure out how to build launch configuration
    //ILaunchConfiguration config = DebugPlugin.getDefault().getLaunchManager()
    //  .getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_SOCKET_ATTACH_VM_CONNECTOR);
    ILaunchConfiguration config = null;

    ILaunch launch = new Launch(config, ILaunchManager.DEBUG_MODE, null);
    IProgressMonitor monitor = null;
    Map<String, String> args = new HashMap<String, String>();
    args.put(KEY_HOSTNAME, host);
    args.put(KEY_PORT, port);

    SocketAttachConnector connector = new SocketAttachConnector();
    connector.connect(args, monitor, launch);

    this.debugTarget = launch.getDebugTarget();
  }

  // TODO
  public void stop() {

  }

  /**
   * Resumes execution from the current breakpoint.
   */
  public void resume() throws DebugException {
    if (logger.isInfoEnabled()) {
      logger.info("Resuming breakpoint");
    }
    debugTarget.resume();
  }

  /**
   * Listener to respond to breakpoint related events.
   */
  private class BreakpointListener implements IJavaBreakpointListener {
    @Override
    public void addingBreakpoint(IJavaDebugTarget target,
        IJavaBreakpoint breakpoint) {
    }

    @Override
    public void breakpointHasCompilationErrors(IJavaLineBreakpoint breakpoint,
        Message[] msg) {
    }

    @Override
    public void breakpointHasRuntimeException(IJavaLineBreakpoint breakpoint,
        DebugException ex) {
    }

    @Override
    public int breakpointHit(IJavaThread thread, IJavaBreakpoint breakpoint) {
      try {
        String fileName = breakpoint.getMarker().getResource().getRawLocation()
          .toOSString();
        int lineNum = ((IJavaLineBreakpoint) breakpoint).getLineNumber();
        if (logger.isInfoEnabled()) {
          logger.info("Breakpoint hit: " + breakpoint.getTypeName() + " at "
              + lineNum);
        }

        // TODO Is there a better way to do this?
        String[] cmd = {
          "vim",
          "--servername",
          vimInstanceId,
          "--remote-tab",
          "+" + lineNum,
          fileName
        };

        if (logger.isInfoEnabled()) {
          logger.info("Executing external cmd: " + Arrays.asList(cmd));
        }

        CommandExecutor.execute(cmd, 60);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      return IJavaBreakpointListener.SUSPEND;
    }

    @Override
    public void breakpointInstalled(IJavaDebugTarget target,
        IJavaBreakpoint breakpoint) {

      try {
        int lineNum = ((IJavaLineBreakpoint) breakpoint).getLineNumber();

        if (logger.isInfoEnabled()) {
          logger.info("Breakpoint installed: " + breakpoint.getTypeName()
              + " at " + lineNum);
        }
      } catch (CoreException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void breakpointRemoved(IJavaDebugTarget target,
        IJavaBreakpoint breakpoint) {

      try {
        int lineNum = ((IJavaLineBreakpoint) breakpoint).getLineNumber();

        if (logger.isInfoEnabled()) {
          logger.info("Breakpoint removed: " + breakpoint.getTypeName()
              + " at " + lineNum);
        }
      } catch (CoreException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public int installingBreakpoint(IJavaDebugTarget target,
        IJavaBreakpoint breakpoint, IJavaType type) {

      return 0;
    }
  }
}
