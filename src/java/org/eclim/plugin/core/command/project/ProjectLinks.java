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

import java.text.Collator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclipse.core.internal.resources.LinkDescription;
import org.eclipse.core.internal.resources.ProjectDescription;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.IPath;

/**
 * Gets a list of linked resources for this project.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "project_links",
  options = "REQUIRED p project ARG"
)
public class ProjectLinks
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public String execute(CommandLine commandLine)
    throws Exception
  {
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);

    IProject project = ProjectUtils.getProject(projectName);
    IPath descPath = project.getLocation().append(".project");

    StringBuffer result = new StringBuffer();
    if (descPath.toFile().exists()){
      IWorkspace workspace = ResourcesPlugin.getWorkspace();
      ProjectDescription desc = (ProjectDescription)
        workspace.loadProjectDescription(descPath);

      Map<IPath,LinkDescription> links = desc.getLinks();

      if (links != null){
        ArrayList<String> sorted = new ArrayList<String>(links.size());
        HashMap<String, LinkDescription> paths =
          new HashMap<String, LinkDescription>(links.size());
        for (IPath path : (Set<IPath>)links.keySet()){
          sorted.add(path.toString());
          paths.put(path.toString(), links.get(path));
        }

        Collections.sort(sorted, Collator.getInstance());

        for (String path : sorted){
          if(result.length() > 0){
            result.append('\n');
          }
          LinkDescription link = paths.get(path);
          result.append(path)
            .append(" -> ")
            .append(link.getLocationURI().getPath());
        }
      }
    }

    return result.toString();
  }
}
