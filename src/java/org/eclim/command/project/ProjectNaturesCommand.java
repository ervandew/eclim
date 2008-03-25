/**
 * Copyright (C) 2005 - 2008  Eric Van Dewoestine
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
package org.eclim.command.project;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.project.ProjectNatureFactory;

import org.eclim.util.ProjectUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * Command which returns a list of project nature aliases associated with the
 * requested project, or a list of all projects if no project name specified.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class ProjectNaturesCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    String name = _commandLine.getValue(Options.PROJECT_OPTION);

    // list all projects.
    if(name == null){
      ArrayList<String> results = new ArrayList<String>();

      IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

      // find longest project name for padding.
      int length = 0;
      for(IProject project : projects){
        if (project.isOpen()){
          name = project.getName();
          if(name.length() > length){
            length = name.length();
          }
        }
      }

      for(IProject project : projects){
        if (project.isOpen()){
          String[] aliases = ProjectNatureFactory.getProjectNatureAliases(project);
          if (aliases.length == 0){
            aliases = new String[]{"none"};
          }
          StringBuffer info = new StringBuffer()
            .append(StringUtils.rightPad(project.getName(), length))
            .append(" - ")
            .append(StringUtils.join(aliases, ' '));
          results.add(info.toString());
        }
      }

      return StringUtils.join(
          (String[])results.toArray(new String[results.size()]), '\n');
    }

    // list for requested project.
    String[] aliases = ProjectNatureFactory.getProjectNatureAliases(
        ProjectUtils.getProject(name));
    if (aliases.length == 0){
      aliases = new String[]{"none"};
    }
    return name + " - " + StringUtils.join(aliases, ' ');
  }
}
