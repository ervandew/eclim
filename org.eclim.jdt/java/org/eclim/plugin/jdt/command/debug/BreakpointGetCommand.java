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

import org.eclipse.core.resources.IResource;

import org.eclipse.debug.core.DebugPlugin;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;

/**
 * Command to retrieve breakpoints.
 *
 * If a file is given, then only the breakpoints on that file will be returned.
 * Otherwise, all breakpoints in the project workspace will be returned.
 */
@Command(
  name = "java_debug_breakpoint_get",
  options =
    "OPTIONAL f file ARG"
)
public class BreakpointGetCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String fileName = commandLine.getValue(Options.FILE_OPTION);
    ArrayList<Position> positions = new ArrayList<Position>();

    IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager()
      .getBreakpoints();

    if (breakpoints != null) {
      for (IBreakpoint breakpoint : breakpoints) {
        IResource resource = breakpoint.getMarker().getResource();
        String curFileName = resource.getRawLocation().toOSString();
        String projectName = resource.getProject().getName();

        if (fileName == null || fileName.equals(curFileName)) {
          Position position = Position.fromLineColumn(
              curFileName,
              projectName,
              ((ILineBreakpoint) breakpoint).getLineNumber(),
              1);

          if (breakpoint.isEnabled()) {
            position.setMetaInfo("e");
          }

          positions.add(position);
        }
      }
    }

    return positions;
  }
}
