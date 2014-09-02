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

import org.eclim.plugin.jdt.command.debug.context.DebuggerContext;

import org.eclipse.debug.core.model.ISourceLocator;

import org.eclipse.jdt.debug.core.IJavaStackFrame;

import org.eclipse.jdt.internal.core.CompilationUnit;

import org.eclipse.jdt.internal.debug.core.model.JDIDebugElement;

/**
 * Abstract debug event handler.
 */
public abstract class DebugEventHandler
{
  protected abstract void handle(
      DebuggerContext ctx,
      JDIDebugElement element,
      int kind,
      int detail)
    throws Exception;

  /**
   * Returns the file name associated with the given stack frame.
   *
   * @param ctx debugger context
   * @param stackFrame stack frame
   * @return file name if applicable; null otherwise
   */
  protected String getFileNameInFrame(DebuggerContext ctx,
      IJavaStackFrame stackFrame)
  {
    ISourceLocator srcLocator = ctx.getDebugTarget().getLaunch()
      .getSourceLocator();
    Object src = srcLocator.getSourceElement(stackFrame);
    String fileName = null;
    if (src instanceof CompilationUnit) {
      fileName = (((CompilationUnit) src).getResource()
          .getRawLocation().toOSString());
    }

    return fileName;
  }
}
