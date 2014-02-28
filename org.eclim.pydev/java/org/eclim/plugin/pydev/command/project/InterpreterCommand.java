/**
 * Copyright (C) 2013 Eric Van Dewoestine
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
package org.eclim.plugin.pydev.command.project;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclipse.core.resources.IProject;

import org.python.pydev.core.IInterpreterInfo;

import org.python.pydev.plugin.nature.PythonNature;

/**
 * Command to get the python interpreter for the specified project.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "python_interpreter",
  options = "REQUIRED p project ARG"
)
public class InterpreterCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    IProject project = ProjectUtils.getProject(projectName);
    PythonNature nature = PythonNature.getPythonNature(project);
    IInterpreterInfo interpreter = nature.getProjectInterpreter();
    if (interpreter != null){
      return interpreter.getExecutableOrJar();
    }
    return null;
  }
}
