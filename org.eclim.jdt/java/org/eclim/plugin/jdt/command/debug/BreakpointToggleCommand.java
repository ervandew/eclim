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
import java.util.HashMap;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclipse.core.resources.IResource;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;

import org.eclipse.jdt.debug.core.JDIDebugModel;

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
 *   - If no breakpoint exists it will be created.
 *   - If the 'delete' option is supplied the breakpoint will be delete in the
 *     case where it would have been otherwise disabled.
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
    "OPTIONAL l line ARG," +
    "OPTIONAL d delete NOARG"
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

    // edge case when acting on a line in a file, create the breakpoint if none
    // exists
    if (fileName != null && lineNum != null &&
        enabled.size() == 0 && disabled.size() == 0)
    {
      fileName = commandLine.getValue(Options.FILE_OPTION);
      create(projectName, fileName, lineNum);
      return Services.getMessage("debugging.breakpoint.added");
    }

    String action = "enabled";
    if (enabled.size() == 0){
      for (IBreakpoint breakpoint : disabled){
        breakpoint.setEnabled(true);
      }
    }else{
      action = "disabled";
      // another edge case when acting on a line in a file, delete the
      // breakpoint if the 'delete' option was supplied
      if (fileName != null && lineNum != null &&
          enabled.size() == 1 && commandLine.hasOption("d"))
      {
        action = "removed";
        breakpointMgr.removeBreakpoint(enabled.get(0), true);
      }else{
        for (IBreakpoint breakpoint : enabled){
          breakpoint.setEnabled(false);
        }
      }
    }

    return Services.getMessage("debugging.breakpoint.toggled", action);
  }

  private void create(String projectName, String fileName, int line)
    throws Exception
  {
    ICompilationUnit compUnit = JavaUtils.getCompilationUnit(
        projectName, fileName);
    // TODO How to find out right type from this array?
    IType type = compUnit.getTypes()[0];
    IResource res = compUnit.getResource();
    HashMap<String, Object> attrMap = new HashMap<String, Object>();

    String typeName = type.getFullyQualifiedName();
    JDIDebugModel.createLineBreakpoint(
        res, typeName, line, -1, -1, 0, true, attrMap);
  }
}
