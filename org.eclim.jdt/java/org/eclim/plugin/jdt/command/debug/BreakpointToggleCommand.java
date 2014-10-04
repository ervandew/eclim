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

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclipse.core.resources.IResource;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;

import org.eclipse.debug.core.model.IBreakpoint;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;

import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;

/**
 * Command to enable or disable a specific breakpoint.
 */
@Command(
  name = "java_debug_breakpoint_toggle",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "REQUIRED l line_num ARG"
)
public class BreakpointToggleCommand
  extends AbstractCommand
{
  private static final String BKPOINT_ENABLED = "1";
  private static final String BKPOINT_DISABLED = "0";
  private static final String BKPOINT_NOT_EXISTS = "-1";

  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    String fileName = commandLine.getValue(Options.FILE_OPTION);
    int lineNum = commandLine.getIntValue(Options.LINE_OPTION);

    // TODO For some projects, querying for compilation unit does not
    // work. So for now, iterating through all breakpoints to find match.
    // If it does not work, then lookup using project and file.
    IBreakpointManager breakpointMgr = DebugPlugin.getDefault()
      .getBreakpointManager();
    IBreakpoint[] breakpoints = breakpointMgr.getBreakpoints();
    for (IBreakpoint breakpoint : breakpoints) {
      String curFileName = breakpoint.getMarker().getResource()
        .getRawLocation().toOSString();

      if (fileName.equals(curFileName)) {
        if (breakpoint.isEnabled()) {
          breakpoint.setEnabled(false);
          return BKPOINT_DISABLED;
        } else {
          breakpoint.setEnabled(true);
          return BKPOINT_ENABLED;
        }
      }
    }

    return toggleBreakpoint(projectName, fileName, lineNum);
  }

  private String toggleBreakpoint(
      String projectName, String fileName, int lineNum)
    throws Exception
  {

    ICompilationUnit compUnit = JavaUtils.getCompilationUnit(projectName, fileName);
    // TODO How to find out right type from this array?
    IType type = compUnit.getTypes()[0];
    IResource res = compUnit.getResource();

    String typeName = type.getFullyQualifiedName();
    IJavaLineBreakpoint breakpoint = JDIDebugModel.lineBreakpointExists(
        res, typeName, lineNum);

    if (breakpoint == null) {
      return BKPOINT_NOT_EXISTS;
    } else if (breakpoint.isEnabled()) {
      breakpoint.setEnabled(false);
      return BKPOINT_DISABLED;
    } else {
      breakpoint.setEnabled(true);
      return BKPOINT_ENABLED;
    }
  }
}
