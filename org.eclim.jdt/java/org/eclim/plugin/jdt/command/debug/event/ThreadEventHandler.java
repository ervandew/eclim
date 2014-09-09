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
import org.eclim.plugin.jdt.command.debug.context.DebuggerState;

import org.eclipse.debug.core.DebugEvent;

import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;

import org.eclipse.jdt.internal.debug.core.model.JDIDebugElement;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;

/**
 * Event handler for a thread.
 */
public class ThreadEventHandler extends DebugEventHandler
{
  private static final Logger logger =
    Logger.getLogger(ThreadEventHandler.class);

  protected void handle(
      DebuggerContext ctx,
      JDIDebugElement element,
      int kind,
      int detail)
    throws Exception
  {
    JDIThread thread = (JDIThread) element;

    if (logger.isDebugEnabled()) {
      logger.debug("Handling thread event : " + thread.getName() + " : " +
          ((IJavaThread) thread).getThreadObject().getUniqueId() +  " " +
          kind + " " + detail);
    }

    if (kind == DebugEvent.SUSPEND) {
      if ((detail == DebugEvent.STEP_END) ||
          (detail == DebugEvent.BREAKPOINT))
      {
        IJavaStackFrame topStackFrame = (IJavaStackFrame) thread.getTopStackFrame();
        String fileName = getFileNameInFrame(ctx, topStackFrame);
        int lineNum = topStackFrame.getLineNumber();

        if (logger.isDebugEnabled()) {
          if (detail == DebugEvent.BREAKPOINT) {
            logger.debug("Breakpoint hit: " + fileName + " at " + lineNum);
          }
        }

        ctx.getThreadContext().update(thread, thread.getStackFrames());

        // Do not update variables when suspended in a class file.
        // This causes the variable set to explode causing OOM.
        if (fileName != null) {
          ctx.getVimClient().jumpToFilePosition(fileName, lineNum);
        }

        // Call refresh after jumping to file. Otherwise, it causes the sign to
        // not get placed for some reason.
        ctx.getVimClient().refreshDebugStatus();
      } else if (detail == DebugEvent.CLIENT_REQUEST) {
        ctx.getThreadContext().update(thread, thread.getStackFrames());
        ctx.getVimClient().refreshDebugStatus();
      }
    } else if (kind == DebugEvent.CREATE) {
      ctx.getThreadContext().update(thread, null);

      // Refresh status only after debug target has been created.
      // This is to avoid refreshing for each and every thread that gets
      // created before the debug target has finished initialization.
      // Once its created, we do want to refresh for each thread creation.
      if (!ctx.getState().equals(DebuggerState.CONNECTING)) {
        ctx.getVimClient().refreshDebugStatus();
      }
    } else if (kind == DebugEvent.TERMINATE) {
      ctx.getThreadContext().remove(thread);
      ctx.getVimClient().refreshDebugStatus();
    } else if (kind == DebugEvent.RESUME) {
      ctx.getThreadContext().update(thread, null);
      ctx.getVariableContext().removeVariables();
      ctx.getVimClient().refreshDebugStatus();
    }
  }
}
