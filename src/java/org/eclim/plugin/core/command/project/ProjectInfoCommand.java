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

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.project.ProjectNatureFactory;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.util.StringUtils;

import org.eclipse.core.resources.IProject;

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
  /**
   * {@inheritDoc}
   */
  public String execute(CommandLine commandLine)
    throws Exception
  {
    String name = commandLine.getValue(Options.PROJECT_OPTION);

    IProject project = ProjectUtils.getProject(name);
    if(project.exists()){
      StringBuffer info = new StringBuffer();
      info.append("Name: ").append(name).append('\n');
      info.append("Path: ").append(ProjectUtils.getPath(project)).append('\n');
      info.append("Natures: ");
      String[] aliases = ProjectNatureFactory.getProjectNatureAliases(project);
      if (aliases.length == 0){
        aliases = new String[]{"none"};
      }
      info.append(StringUtils.join(aliases, ' ')).append('\n');

      info.append("Depends On: ");
      IProject[] depends = project.getReferencedProjects();
      if (depends.length == 0){
        info.append("None").append('\n');
      }else{
        String[] names = new String[depends.length];
        for (int ii = 0; ii < depends.length; ii++){
          names[ii] = depends[ii].getName();
        }
        info.append(StringUtils.join(names, ' ')).append('\n');
      }

      info.append("Referenced By: ");
      IProject[] references = project.getReferencingProjects();
      if (references.length == 0){
        info.append("None").append('\n');
      }else{
        String[] names = new String[references.length];
        for (int ii = 0; ii < references.length; ii++){
          names[ii] = references[ii].getName();
        }
        info.append(StringUtils.join(names, ' ')).append('\n');
      }

      return info.toString();
    }
    return Services.getMessage("project.not.found", name);
  }
}
