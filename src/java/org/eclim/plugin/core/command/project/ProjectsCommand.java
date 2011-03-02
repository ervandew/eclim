/**
 * Copyright (C) 2005 - 2011  Eric Van Dewoestine
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

import org.eclim.plugin.core.project.ProjectNatureFactory;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclipse.core.filesystem.IFileStore;

import org.eclipse.core.internal.resources.LinkDescription;
import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.internal.resources.ProjectDescription;
import org.eclipse.core.internal.resources.ProjectInfo;
import org.eclipse.core.internal.resources.ResourceInfo;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.IPath;

import org.eclipse.ui.internal.ide.dialogs.IDEResourceInfoUtils;

import com.google.gson.Gson;

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
    Gson gson = new Gson();
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    IProject[] projects = workspace.getRoot().getProjects();
    for (IProject project : projects){
      HashMap<String,Object> info = new HashMap<String,Object>();
      info.put("name", project.getName());

      if (project.isOpen()){
        String[] aliases = ProjectNatureFactory.getProjectNatureAliases(project);
        if (aliases.length == 0){
          aliases = new String[]{"none"};
        }
        info.put("natures", aliases);
      }else{
        info.put("natures", new String[0]);
      }

      String projectPath = ProjectUtils.getPath(project);
      info.put("path", projectPath);

      HashMap<String,String> links = new HashMap<String,String>();
      if (new File(projectPath).exists()){
        // don't open/close projects due to the impact on performance
        /*boolean close = false;
        if (!project.isOpen()){
          project.open(IResource.NONE, null);
          close = true;
        }*/

        ResourceInfo pinfo = ((Project)project).getResourceInfo(false, false);
        ProjectDescription desc = ((ProjectInfo)pinfo).getDescription();
        if (desc != null){
          @SuppressWarnings("unchecked")
          HashMap<IPath, LinkDescription> linfo =
            (HashMap<IPath, LinkDescription>)desc.getLinks();
          if (linfo != null){
            for (IPath path : linfo.keySet()){
              LinkDescription link = linfo.get(path);
              IResource member = project.findMember(link.getProjectRelativePath());
              IFileStore store = IDEResourceInfoUtils.getFileStore(
                  member.getLocationURI());
              String resolvedPath = store != null ?
                store.toString() : link.getLocationURI().getPath();
              links.put(path.toString(), resolvedPath);
            }
          }
        }

        /*if (close){
          project.close(null);
        }*/
      }
      info.put("links", links);

      if(result.length() > 0){
        result.append('\n');
      }
      result.append(gson.toJson(info));
    }

    return result.toString();
  }
}
