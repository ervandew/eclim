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
package org.eclim.plugin.jdt.command.debug.event;

import org.eclim.logging.Logger;

import org.eclim.plugin.jdt.command.debug.context.DebuggerContext;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;

import org.eclipse.debug.core.model.IThread;

import org.eclipse.jdt.debug.core.IJavaStackFrame;

import org.eclipse.jdt.internal.debug.core.model.JDIDebugElement;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;

/**
 * Event handler for debug target.
 */
public class DebugTargetEventHandler extends DebugEventHandler
{
  private static final Logger logger =
    Logger.getLogger(DebugTargetEventHandler.class);

  protected void handle(
      DebuggerContext ctx,
      JDIDebugElement element,
      int kind,
      int detail)
    throws Exception
  {
    JDIDebugTarget debugTarget = (JDIDebugTarget) element;

    if (logger.isDebugEnabled()) {
      logger.debug("Handling debug target event : " +
          kind + " " + detail);
    }

    if (kind == DebugEvent.SUSPEND) {
      if (detail == DebugEvent.CLIENT_REQUEST) {
        IThread[] threads = debugTarget.getThreads();
        if (threads == null) {
          return;
        }

        for (IThread thread : threads) {
          IJavaStackFrame topStackFrame = (IJavaStackFrame) thread
            .getTopStackFrame();

          ctx.getThreadContext().update(thread, thread.getStackFrames());

          String fileName = getFileNameInFrame(ctx, topStackFrame);

          // Do not update variables when suspended in a class file.
          // This causes the variable set to explode causing OOM.
          if (fileName != null) {
            // Protect against variable information unavailable for native
            // methods
            try {
              ctx.getVariableContext().update(
                  thread,
                  topStackFrame
                  .getVariables());
            } catch (DebugException e) {}
          }
        }

        ctx.getVimClient().refreshDebugStatus();
      }
    } else if (kind == DebugEvent.RESUME) {
      if (detail == DebugEvent.CLIENT_REQUEST) {
        ctx.getThreadContext().removeStackFrames();
        ctx.getVariableContext().removeVariables();
        ctx.getVimClient().refreshDebugStatus();
      }
    }
  }
}
