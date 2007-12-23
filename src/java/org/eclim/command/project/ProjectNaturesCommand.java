/**
 * Copyright (c) 2005 - 2007
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
