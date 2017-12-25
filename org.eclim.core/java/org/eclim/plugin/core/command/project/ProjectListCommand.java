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
 * Command which lists all projects and their status.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "project_list",
  options = "OPTIONAL n nature ARG"
)
public class ProjectListCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
    String natureId = null;
    if(commandLine.hasOption(Options.NATURE_OPTION)){
      String alias = commandLine.getValue(Options.NATURE_OPTION);
      natureId = ProjectNatureFactory.getNatureForAlias(alias);
      ArrayList<IProject> filtered = new ArrayList<IProject>();
      for (IProject project : projects){
        if (project.isOpen() && project.hasNature(natureId)){
          filtered.add(project);
        }
      }
      projects = (IProject[])filtered.toArray(new IProject[filtered.size()]);
    }

    ArrayList<HashMap<String,Object>> results =
      new ArrayList<HashMap<String,Object>>();
    for(IProject project : projects){
      if(project.exists()){
        HashMap<String,Object> result = new HashMap<String,Object>();
        result.put("name", project.getName());
        result.put("path", ProjectUtils.getPath(project));
        result.put("open", project.isOpen());
        results.add(result);
      }
    }
    return results;
  }
}
