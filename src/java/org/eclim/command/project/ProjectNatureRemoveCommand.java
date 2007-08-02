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

import org.apache.commons.collections.CollectionUtils;

import org.apache.commons.lang.StringUtils;

import org.eclim.Services;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.project.ProjectNatureFactory;

import org.eclim.util.ProjectUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;

import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Command to remove one or more natures from a project.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class ProjectNatureRemoveCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
  {
    try{
      String name = _commandLine.getValue(Options.PROJECT_OPTION);
      IProject project = ProjectUtils.getProject(name);
      String[] aliases = StringUtils.split(
          _commandLine.getValue(Options.NATURE_OPTION), ',');

      IProjectDescription desc = project.getDescription();
      String[] natureIds = desc.getNatureIds();
      ArrayList<String> modified = new ArrayList<String>();
      CollectionUtils.addAll(modified, natureIds);
      for(String alias : aliases){
        modified.remove(ProjectNatureFactory.getNatureForAlias(alias));
      }

      desc.setNatureIds((String[])modified.toArray(new String[modified.size()]));
      project.setDescription(desc, new NullProgressMonitor());

      return Services.getMessage("project.nature.removed");
    }catch(Throwable t){
      return t;
    }
  }
}
