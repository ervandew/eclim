/**
 * Copyright (C) 2005 - 2009  Eric Van Dewoestine
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

import java.util.HashMap;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclipse.core.internal.resources.LinkDescription;
import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.internal.resources.ProjectDescription;
import org.eclipse.core.internal.resources.ProjectInfo;
import org.eclipse.core.internal.resources.ResourceInfo;

import org.eclipse.core.internal.utils.FileUtil;

import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.IPath;

/**
 * Command which obtains a list of projects and project paths for use by clients
 * to determine which project a file belongs to.
 *
 * @author Eric Van Dewoestine
 */
@Command(name = "projects")
public class ProjectsCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   * @see org.eclim.command.Command#execute(CommandLine)
   */
  public String execute(CommandLine commandLine)
    throws Exception
  {
    StringBuffer result = new StringBuffer();
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    IPathVariableManager manager = workspace.getPathVariableManager();
    IProject[] projects = workspace.getRoot().getProjects();
    for (IProject project : projects){
      if(result.length() > 0){
        result.append('\n');
      }
      String projectPath = ProjectUtils.getPath(project);
      result.append(project.getName()).append(':').append(projectPath);

      if (new File(projectPath).exists()){
        // don't open/close projects due to the impact on performance
        /*boolean close = false;
        if (!project.isOpen()){
          project.open(IResource.NONE, null);
          close = true;
        }*/

        ResourceInfo info = ((Project)project).getResourceInfo(false, false);
        ProjectDescription desc = ((ProjectInfo)info).getDescription();
        if (desc != null){
          @SuppressWarnings("unchecked")
          HashMap<IPath, LinkDescription> links =
            (HashMap<IPath, LinkDescription>)desc.getLinks();
          if (links != null){
            for (IPath path : links.keySet()){
              LinkDescription link = links.get(path);
              IPath linkPath = FileUtil.toPath(link.getLocationURI());
              result
                .append(',')
                .append(path)
                .append(':')
                .append(manager.resolvePath(linkPath));
            }
          }
        }

        /*if (close){
          project.close(null);
        }*/
      }
    }

    return result.toString();
  }
}
