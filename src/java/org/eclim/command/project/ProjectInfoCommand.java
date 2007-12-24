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

import org.eclim.Services;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.project.ProjectNatureFactory;

import org.eclim.util.ProjectUtils;
import org.eclim.util.StringUtils;

import org.eclipse.core.resources.IProject;

/**
 * Command to get project info.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ProjectInfoCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    String name = _commandLine.getValue(Options.PROJECT_OPTION);

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
