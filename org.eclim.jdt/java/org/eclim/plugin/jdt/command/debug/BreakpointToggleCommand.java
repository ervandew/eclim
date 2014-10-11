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

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclipse.core.resources.IResource;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;

/**
 * Command to enable or disable a specific breakpoint.
 *
 * There are three possible ways this command can be executed:
 * - If only a project is supplied, then only the breakpoints in that project
 *   will be enabled/disabled.
 * - If a file path is supplied, then only the breakpoints for that file will be
 *   enabled/disabled.
 * - If both a file name and a line number is supplied, then that specific
 *   breakpoint will be toggled.
 *
 * When toggling more than one breakpoint, the behavior is:
 * - if all breakpoints are disabled, enable them all
 * - if any breakpoints are enabled, disable them all
 */
@Command(
  name = "java_debug_breakpoint_toggle",
  options =
    "REQUIRED p project ARG," +
    "OPTIONAL f file ARG," +
    "OPTIONAL l line ARG"
)
public class BreakpointToggleCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    String fileName = commandLine.getValue(Options.FILE_OPTION);
    // currently relies on comparing absolute paths
    if (fileName != null){
      fileName = ProjectUtils.getFilePath(projectName, fileName);
    }
    Integer lineNum = commandLine.getIntValue(Options.LINE_OPTION);

    ArrayList<IBreakpoint> enabled = new ArrayList<IBreakpoint>();
    ArrayList<IBreakpoint> disabled = new ArrayList<IBreakpoint>();

    IBreakpointManager breakpointMgr = DebugPlugin.getDefault()
      .getBreakpointManager();
    IBreakpoint[] breakpoints = breakpointMgr.getBreakpoints();
    for (IBreakpoint breakpoint : breakpoints) {
      IResource resource = breakpoint.getMarker().getResource();
      String curProject = resource.getProject().getName();
      String curFileName = resource.getRawLocation().toOSString();
      if (!curProject.equals(projectName)){
        continue;
      }

      String projectRelPath = breakpoint.getMarker().getResource()
        .getProjectRelativePath().toString();
      if (fileName != null){
        if (fileName.equals(curFileName) || fileName.equals(projectRelPath)) {
          if (lineNum == null ||
              lineNum == -1 ||
              lineNum == ((ILineBreakpoint)breakpoint).getLineNumber())
          {
            if (breakpoint.isEnabled()){
              enabled.add(breakpoint);
            }else{
              disabled.add(breakpoint);
            }
          }
        }
      }else{
        if (breakpoint.isEnabled()){
          enabled.add(breakpoint);
        }else{
          disabled.add(breakpoint);
        }
      }
    }

    String action = "enabled";
    if (enabled.size() == 0){
      for (IBreakpoint breakpoint : disabled){
        breakpoint.setEnabled(true);
      }
    }else{
      action = "disabled";
      for (IBreakpoint breakpoint : enabled){
        breakpoint.setEnabled(false);
      }
    }

    return Services.getMessage("debugging.breakpoint.toggled", action);
  }
}
