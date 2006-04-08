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

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.project.ProjectManagement;
import org.eclim.project.ProjectManager;

/**
 * Command to update a project.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ProjectUpdateCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
    throws IOException
  {
    try{
      String projectName = _commandLine.getValue(Options.NAME_OPTION);

      // FIXME: need to get the proper project manager depending on the project
      // nature (not necessary until we actually have projects with natures
      // other than java).
      ProjectManager manager = ProjectManagement.getProjectManager(
          "org.eclipse.jdt.core.javanature");
      return filter(_commandLine,
          manager.update(projectName, _commandLine));
    }catch(Throwable t){
      return t;
    }
  }
}
