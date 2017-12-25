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

import java.util.HashMap;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.project.ProjectNatureFactory;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * Command to get project info.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "project_info",
  options = "REQUIRED p project ARG"
)
public class ProjectInfoCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String name = commandLine.getValue(Options.PROJECT_OPTION);

    IProject project = ProjectUtils.getProject(name);
    if(project.exists()){
      String workspace = ResourcesPlugin
        .getWorkspace().getRoot().getRawLocation().toOSString().replace('\\', '/');

      HashMap<String,Object> info = new HashMap<String,Object>();
      info.put("name", name);
      info.put("path", ProjectUtils.getPath(project));
      info.put("workspace", workspace);
      info.put("open", project.isOpen());
      if (project.isOpen()){
        String[] aliases = ProjectNatureFactory.getProjectNatureAliases(project);
        if (aliases.length == 0){
          aliases = new String[]{"none"};
        }
        info.put("natures", aliases);

        IProject[] depends = project.getReferencedProjects();
        if (depends.length > 0){
          String[] names = new String[depends.length];
          for (int ii = 0; ii < depends.length; ii++){
            names[ii] = depends[ii].getName();
          }
          info.put("depends", names);
        }

        IProject[] references = project.getReferencingProjects();
        if (references.length > 0){
          String[] names = new String[references.length];
          for (int ii = 0; ii < references.length; ii++){
            names[ii] = references[ii].getName();
          }
          info.put("references", names);
        }
      }

      return info;
    }
    return Services.getMessage("project.not.found", name);
  }
}
