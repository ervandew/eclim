/**
 * Copyright (c) 2004 - 2005
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

import java.util.Map;

import org.eclim.Services;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * Command to delete a project.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ProjectDeleteCommand
  extends AbstractCommand
{
  private Map projectManagers;

  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
    throws IOException
  {
    try{
      String name = _commandLine.getValue(Options.NAME_OPTION);
      // FIXME: need to use project managers.
      IProject project =
        ResourcesPlugin.getWorkspace().getRoot().getProject(name);
      if(project.exists()){
        project.delete(false/*deleteContent*/, true/*force*/, null/*monitor*/);
        return Services.getMessage("project.deleted", name);
      }
      return Services.getMessage("project.not.found", name);
    }catch(Throwable t){
      return t;
    }
  }

  /**
   * Sets the map of project managers.
   * <p/>
   * Key   - project nature
   * Value - project manager instance.
   *
   * @param _projectManagers
   */
  public void setProjectManagers (Map _projectManagers)
  {
    projectManagers = _projectManagers;
  }
}
