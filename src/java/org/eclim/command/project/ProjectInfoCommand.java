/**
 * Copyright (c) 2005 - 2006
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

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.eclim.Services;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.preference.Option;

import org.eclim.util.ProjectUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * Command to obtain project info.
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
  public Object execute (CommandLine _commandLine)
    throws IOException
  {
    try{
      String name = _commandLine.getValue(Options.NAME_OPTION);
      List results = new ArrayList();

      // list all projects.
      if(name == null){
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        String open = Services.getMessage("project.status.open");
        String closed = Services.getMessage("project.status.closed");

        // pad status string
        int pad = Math.max(open.length(), closed.length());
        closed = StringUtils.rightPad(closed, pad);
        open = StringUtils.rightPad(open, pad);

        // find longest project name for padding.
        int length = 0;
        for (int ii = 0; ii < projects.length; ii++){
          name = projects[ii].getName();
          if(name.length() > length){
            length = name.length();
          }
        }

        for(int ii = 0; ii < projects.length; ii++){
          if(projects[ii].exists()){
            StringBuffer info = new StringBuffer()
              .append(StringUtils.rightPad(projects[ii].getName(), length))
              .append(" - ")
              .append(projects[ii].isOpen() ? open : closed)
              .append(" - ")
              .append(ProjectUtils.getPath(projects[ii]));
            results.add(info.toString());
          }
        }

      // retrieve project settings.
      }else{
        IProject project = ProjectUtils.getProject(name, true);
        String setting = _commandLine.getValue(Options.SETTING_OPTION);
        Option[] options = getPreferences().getOptions(project);

        // only retrieving the requested setting.
        if(setting != null){
          for(int ii = 0; ii < options.length; ii++){
            if(options[ii].getName().equals(setting)){
              results.add(options[ii]);
              break;
            }
          }

        // retrieve all settings.
        }else{
          results.addAll(Arrays.asList(options));
        }
      }
     return filter(_commandLine, results);
    }catch(Throwable t){
      return t;
    }
  }
}
