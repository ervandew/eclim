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

import java.util.ArrayList;
import java.util.HashMap;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.project.ProjectNatureFactory;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * Command which returns a list of project nature aliases associated with the
 * requested project, or a list of all projects if no project name specified.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "project_natures",
  options = "OPTIONAL p project ARG"
)
public class ProjectNaturesCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String name = commandLine.getValue(Options.PROJECT_OPTION);

    ArrayList<HashMap<String,Object>> results =
      new ArrayList<HashMap<String,Object>>();

    // list all projects.
    if(name == null){
      IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

      for(IProject project : projects){
        if (project.isOpen()){
          String[] aliases = ProjectNatureFactory.getProjectNatureAliases(project);
          if (aliases.length == 0){
            aliases = new String[]{"none"};
          }
          HashMap<String,Object> result = new HashMap<String,Object>();
          result.put("name", project.getName());
          result.put("natures", aliases);
          results.add(result);
        }
      }
    }else{
      // list for requested project.
      String[] aliases = ProjectNatureFactory.getProjectNatureAliases(
          ProjectUtils.getProject(name));
      if (aliases.length == 0){
        aliases = new String[]{"none"};
      }
      HashMap<String,Object> result = new HashMap<String,Object>();
      result.put("name", name);
      result.put("natures", aliases);
      results.add(result);
    }

    return results;
  }
}
