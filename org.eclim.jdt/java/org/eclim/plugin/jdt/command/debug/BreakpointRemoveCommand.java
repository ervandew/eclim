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

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;

/**
 * Command to remove breakpoints.
 *
 * There are 3 options:
 * - If a file is given, then only the breakpoints on that file will be removed.
 * - If both file and line number is given, then that specific breakpoint will be
 * removed.
 * - Otherwise, all breakpoints in the workspace will be removed.
 */
@Command(
  name = "java_debug_breakpoint_remove",
  options =
    "OPTIONAL f file ARG," +
    "OPTIONAL l line_num ARG"
)
public class BreakpointRemoveCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String fileName = commandLine.getValue(Options.FILE_OPTION);
    Integer lineNum = commandLine.getIntValue(Options.LINE_OPTION);

    IBreakpointManager breakpointMgr = DebugPlugin.getDefault()
      .getBreakpointManager();

    IBreakpoint[] breakpoints = breakpointMgr.getBreakpoints();

    if (fileName == null) {
      breakpointMgr.removeBreakpoints(breakpoints, true);
    } else {
      for (IBreakpoint breakpoint : breakpoints) {
        String curFileName = breakpoint.getMarker().getResource()
          .getRawLocation().toOSString();

        if (fileName.equals(curFileName)) {
          if (lineNum == null ||
              lineNum == -1 ||
              lineNum == ((ILineBreakpoint) breakpoint).getLineNumber())
          {

            breakpointMgr.removeBreakpoint(breakpoint, true);
          }
        }
      }
    }

    return Services.getMessage("debugging.breakpoint.removed");
  }
}
