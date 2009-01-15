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
package org.eclim.command.project;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

import org.eclim.Services;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.project.ProjectManagement;
import org.eclim.project.ProjectManager;
import org.eclim.project.ProjectNatureFactory;

import org.eclim.util.CollectionUtils;
import org.eclim.util.ProjectUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;

import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Command to remove one or more natures from a project.
 *
 * @author Eric Van Dewoestine
 */
public class ProjectNatureRemoveCommand
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
    String[] aliases = StringUtils.split(
        commandLine.getValue(Options.NATURE_OPTION), ',');

    IProjectDescription desc = project.getDescription();
    String[] natureIds = desc.getNatureIds();
    ArrayList<String> modified = new ArrayList<String>();
    ArrayList<String> removed = new ArrayList<String>();
    CollectionUtils.addAll(modified, natureIds);
    for(String alias : aliases){
      String natureId = ProjectNatureFactory.getNatureForAlias(alias);
      if (natureId != null && modified.contains(natureId)){
        modified.remove(natureId);
        removed.add(natureId);
      }
    }

    desc.setNatureIds((String[])modified.toArray(new String[modified.size()]));
    project.setDescription(desc, new NullProgressMonitor());

    for (String nature : removed){
      ProjectManager manager = ProjectManagement.getProjectManager(nature);
      if (manager != null) {
        manager.delete(project, commandLine);
      }
    }

    return Services.getMessage("project.nature.removed");
  }
}
