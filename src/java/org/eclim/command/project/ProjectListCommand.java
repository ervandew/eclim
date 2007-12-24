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

import org.eclim.Services;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.project.ProjectNatureFactory;

import org.eclim.util.ProjectUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * Command which lists all projects and their status.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class ProjectListCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    ArrayList<String> results = new ArrayList<String>();

    IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
    String natureId = null;
    if(_commandLine.hasOption(Options.NATURE_OPTION)){
      String alias = _commandLine.getValue(Options.NATURE_OPTION);
      natureId = ProjectNatureFactory.getNatureForAlias(alias);
      ArrayList<IProject> filtered = new ArrayList<IProject>();
      for (IProject project : projects){
        if (project.isOpen() && project.hasNature(natureId)){
          filtered.add(project);
        }
      }
      projects = (IProject[])filtered.toArray(new IProject[filtered.size()]);
    }

    String open = Services.getMessage("project.status.open");
    String closed = Services.getMessage("project.status.closed");

    // pad status string
    int pad = Math.max(open.length(), closed.length());
    closed = StringUtils.rightPad(closed, pad);
    open = StringUtils.rightPad(open, pad);

    // find longest project name for padding.
    int length = 0;
    for (int ii = 0; ii < projects.length; ii++){
      String name = projects[ii].getName();
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
    return StringUtils.join(results, '\n');
  }
}
