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

import java.util.ArrayList;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.util.file.Position;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;

/**
 * Command to manage existing breakpoints.
 */
@Command(
  name = "java_debug_breakpoint",
  options =
    "REQUIRED a action ARG"
)
public class BreakpointCommand
  extends AbstractCommand
{
  private static final String GET_ALL = "get_all";
  private static final String DELETE_ALL = "delete_all";

  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String action = commandLine.getValue(Options.ACTION_OPTION);
    if (action.equalsIgnoreCase(GET_ALL)) {
      IBreakpoint[] breakpoints = getAllBreakpoints();
      ArrayList<Position> positions = new ArrayList<Position>();
      if (breakpoints != null) {
        for (IBreakpoint breakpoint : breakpoints) {
          positions.add(createPosition((ILineBreakpoint) breakpoint));
        }
      }

      return positions;
    } else if (action.equalsIgnoreCase(DELETE_ALL)) {
      deleteAllBreakpoints();
    } else {
      throw new RuntimeException("Invalid breakpoint action: " + action);
    }

    return null;
  }

  private IBreakpoint[] getAllBreakpoints()
    throws CoreException
  {
    return DebugPlugin.getDefault().getBreakpointManager().getBreakpoints();
  }

  private void deleteAllBreakpoints()
    throws CoreException
  {
    IBreakpointManager breakpointMgr = DebugPlugin.getDefault()
      .getBreakpointManager();
    breakpointMgr.removeBreakpoints(breakpointMgr.getBreakpoints(), true);
  }

  private Position createPosition(ILineBreakpoint breakpoint)
    throws CoreException
  {
    String fileName = breakpoint.getMarker().getResource().getRawLocation()
      .toOSString();
    return Position.fromLineColumn(fileName, "", breakpoint.getLineNumber(), 1);
  }
}
