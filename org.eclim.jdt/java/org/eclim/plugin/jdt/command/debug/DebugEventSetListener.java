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
package org.eclim.plugin.jdt.command.debug;

import org.eclim.logging.Logger;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.IDebugEventSetListener;

import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IThread;

import org.eclipse.jdt.debug.core.IJavaStackFrame;

import org.eclipse.jdt.internal.core.CompilationUnit;

import org.eclipse.jdt.internal.debug.core.model.JDIThread;

/**
 * Handler for events triggered during a debug session.
 * Events include thread suspension due to breakpoint or stepping action.
 */
public class DebugEventSetListener
  implements IDebugEventSetListener
{
  private static final Logger logger = Logger.getLogger(
      DebugEventSetListener.class);

  public void handleDebugEvents(DebugEvent[] events)
  {
    for (DebugEvent event : events) {
      Object src = event.getSource();
      int kind = event.getKind();
      int detail = event.getDetail();

      if (logger.isDebugEnabled()) {
        logger.debug("Got event from src: " + src.getClass().getName() +
            " " + kind + " " + detail);
      }

      DebuggerContext ctx = DebuggerContextManager.getDefault();
      if (ctx == null) {
        if (logger.isDebugEnabled()) {
          logger.debug("No debugging session present");
        }
        return;
      }

      try {
        if (src.getClass().equals(JDIThread.class)) {
          handleThreadEvent(ctx, (JDIThread) src, kind, detail);
        } else if (src.getClass().equals(IDebugTarget.class)) {
          handleDebugTargetEvent(ctx, (IDebugTarget) src, kind, detail);
        }
      } catch (Exception e) {
        logger.error("Listener failed", e);
        throw new RuntimeException(e);
      }
    }
  }

  private void handleThreadEvent(
      DebuggerContext ctx,
      JDIThread thread,
      int kind,
      int detail)
    throws Exception
  {
    if (kind == DebugEvent.SUSPEND) {
      if ((detail == DebugEvent.STEP_END) ||
          (detail == DebugEvent.BREAKPOINT))
      {
        IJavaStackFrame topStackFrame = (IJavaStackFrame) thread.getTopStackFrame();
        ISourceLocator srcLocator = ctx.getDebugTarget().getLaunch()
          .getSourceLocator();
        Object src = srcLocator.getSourceElement(topStackFrame);
        if (!(src instanceof CompilationUnit)) {
          return;
        }

        String fileName = (((CompilationUnit) src).getResource()
            .getRawLocation().toOSString());

        int lineNum = topStackFrame.getLineNumber();

        if (logger.isDebugEnabled()) {
          if (detail == DebugEvent.BREAKPOINT) {
            logger.debug("Breakpoint hit: " + fileName + " at " + lineNum);
          }
        }

        ctx.getThreadContext().update(thread, thread.getStackFrames());
        ctx.getVariableContext().update(thread, topStackFrame.getVariables());
        ctx.getVimClient().jumpToFilePosition(fileName, lineNum);

        // Call refresh after jumping to file. Otherwise, it causes the sign to
        // not get placed for some reason.
        ctx.getVimClient().refreshDebugStatus();
      } else if (detail == DebugEvent.CLIENT_REQUEST) {
        ctx.getThreadContext().update(thread, thread.getStackFrames());

        // Protect against variable information unavailable for native methods
        try {
          ctx.getVariableContext().update(thread,
              thread.getTopStackFrame().getVariables());
        } catch (DebugException e) {}
      }
    } else if (kind == DebugEvent.CREATE) {
      ctx.getThreadContext().update(thread, null);
      ctx.getVimClient().refreshDebugStatus();
    } else if (kind == DebugEvent.TERMINATE) {
      ctx.getThreadContext().remove(thread);
      ctx.getVimClient().refreshDebugStatus();
    } else if (kind == DebugEvent.RESUME) {
      if (detail == DebugEvent.CLIENT_REQUEST) {
        ctx.getThreadContext().update(thread, null);
        ctx.getVariableContext().update(thread, null);
        ctx.getVimClient().refreshDebugStatus();
      }
    }
  }

  private void handleDebugTargetEvent(
      DebuggerContext ctx,
      IDebugTarget debugTarget,
      int kind,
      int detail)
    throws Exception
  {
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

          // Protect against variable information unavailable for native methods
          try {
            ctx.getVariableContext().update(
                thread,
                topStackFrame
                .getVariables());
          } catch (DebugException e) {}
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
