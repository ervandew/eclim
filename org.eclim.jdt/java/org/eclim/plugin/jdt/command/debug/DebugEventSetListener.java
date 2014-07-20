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

import org.eclim.logging.Logger;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.IDebugEventSetListener;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ISourceLocator;

import org.eclipse.jdt.core.ICompilationUnit;

import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaStackFrame;

import org.eclipse.jdt.internal.core.CompilationUnit;

import org.eclipse.jdt.internal.debug.core.model.JDIThread;

/**
 * Handler for events triggered during a debug session.
 * Events include thread suspension due to breakpoint or stepping action.
 */
public class DebugEventSetListener implements IDebugEventSetListener
{
  private static final Logger logger = Logger.getLogger(
      DebugEventSetListener.class);

  public void handleDebugEvents(DebugEvent[] events)
  {
    for (DebugEvent event : events) {
      Object src = event.getSource();
      int kind = event.getKind();
      int detail = event.getDetail();

      if (logger.isInfoEnabled()) {
        logger.info("Got event from src: " + src.getClass().getName() +
            " " + kind + " " + detail);
      }

      try {
        if (src.getClass().equals(JDIThread.class)) {
          handleThreadEvent((JDIThread) src, kind, detail);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void handleThreadEvent(JDIThread thread, int kind, int detail)
    throws Exception
  {

    if (kind == DebugEvent.SUSPEND) {
      if ((detail == DebugEvent.STEP_END) ||
          (detail == DebugEvent.BREAKPOINT))
      {

        IJavaStackFrame topStackFrame = (IJavaStackFrame) thread.getTopStackFrame();
        ISourceLocator srcLocator = DebuggerContext.getInstance().getDebugTarget()
          .getLaunch().getSourceLocator();
        Object src = srcLocator.getSourceElement(topStackFrame);
        String fileName = (((CompilationUnit )src).getResource()
            .getRawLocation().toOSString());

        int lineNum = topStackFrame.getLineNumber();

        DebuggerContext.getInstance().setVariables(topStackFrame.getVariables());
        DebuggerContext.getInstance().getVimClient().jumpToFilePosition(fileName,
            lineNum);

      } else if (detail == DebugEvent.BREAKPOINT) {
        IBreakpoint[] allBreakpoints = thread.getBreakpoints();
        IJavaLineBreakpoint breakpoint = (IJavaLineBreakpoint) allBreakpoints[0];
        String fileName = breakpoint.getMarker().getResource().getRawLocation()
          .toOSString();
        int lineNum = ((IJavaLineBreakpoint) breakpoint).getLineNumber();
        if (logger.isInfoEnabled()) {
          logger.info("Breakpoint hit: " + breakpoint.getTypeName() + " at " +
              lineNum);
        }

        DebuggerContext.getInstance().getVimClient().jumpToFilePosition(fileName,
            lineNum);
      }
    }
  }
}
