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

import java.io.File;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Command to move a project.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "project_move",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED d dir ARG"
)
public class ProjectMoveCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    String newDir = commandLine.getValue(Options.DIR_OPTION);

    // moving into an existing directory
    if (new File(newDir).exists()){
      newDir = newDir + '/' + projectName;
    }

    IProject project = ProjectUtils.getProject(projectName);
    if(project.exists()){
      IProjectDescription desc = project.getDescription();

      IWorkspace workspace = ResourcesPlugin.getWorkspace();
      IPath workspaceLocation = workspace.getRoot().getRawLocation();
      IPath location = new Path(newDir);
      // if the new location overlaps the workspace, then set location to null
      // to force eclipse to move the project to the default location in the
      // workspace. Also, take the last segment as the new project name.
      if (location.toOSString().toLowerCase().startsWith(
            workspaceLocation.toOSString().toLowerCase()))
      {
        String name = location.removeFirstSegments(
            location.matchingFirstSegments(workspaceLocation)).toString();
        // hack for windows... manually remove drive letter
        name = name.replaceFirst("^[A-Z]:", "");
        name = name.replaceFirst("/$", "");

        location = null;
        desc.setName(name);
      }
      desc.setLocation(location);
      project.move(desc, true, null);
      // get a new handle on the possibly rename project
      project = ProjectUtils.getProject(desc.getName());
      return Services.getMessage(
          "project.moved", projectName, ProjectUtils.getPath(project));
    }
    return Services.getMessage("project.not.found", projectName);
  }
}
