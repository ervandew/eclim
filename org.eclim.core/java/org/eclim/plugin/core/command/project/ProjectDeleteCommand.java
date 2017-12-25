/**
 * Copyright (C) 2005 - 2017  Eric Van Dewoestine
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
package org.eclim.plugin.core.command.project;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.project.ProjectManagement;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclipse.core.resources.IProject;

/**
 * Command to delete a project.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "project_delete",
  options = "REQUIRED p project ARG"
)
public class ProjectDeleteCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String name = commandLine.getValue(Options.PROJECT_OPTION);

    IProject project = ProjectUtils.getProject(name);
    if (!project.exists()){
      return Services.getMessage("project.not.found", name);
    }
    ProjectManagement.delete(project, commandLine);

    return Services.getMessage("project.deleted", name);
  }
}
