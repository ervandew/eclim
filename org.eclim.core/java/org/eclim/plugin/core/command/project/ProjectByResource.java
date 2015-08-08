/**
 * Copyright (C) 2005 - 2015  Eric Van Dewoestine
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

import java.net.URI;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * Gets the project that the file at the supplied absolute path belongs to.
 * This command honors project links.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "project_by_resource",
  options = "REQUIRED f file ARG"
)
public class ProjectByResource
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String path = commandLine.getValue(Options.FILE_OPTION);

    // can't use URLEncoder on the full file since the colon in 'C:' gets
    // encoded as well.
    //URI uri = new URI("file://" + URLEncoder.encode(path, "UTF-8"));
    URI uri = new URI("file://" + path.replaceAll(" ", "%20"));
    IFile[] files = ResourcesPlugin
      .getWorkspace().getRoot().findFilesForLocationURI(uri);

    IProject project = null;
    if (files.length > 0){
      // loop over all the files and return the project for the longest path
      // (handles the case where projects are nested, returning the most nested
      // parent project of the file)
      for (IFile file : files){
        IProject fproject = file.getProject();
        if (project == null ||
            ProjectUtils.getPath(fproject).length() >
            ProjectUtils.getPath(project).length())
        {
          project = fproject;
        }
      }
    }
    return project != null ? project.getName() : null;
  }
}
