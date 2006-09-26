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
package org.eclim.project;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;

import org.eclipse.core.resources.IProject;

/**
 * Defines methods for managing a project.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public interface ProjectManager
{
  /**
   * Creates a new project, or if a project already exists, updates it to add
   * the necessary nature(s) for the type of project being created.
   *
   * @param _project The project.
   * @param _commandLine The command line for the project create command.
   */
  public void create (IProject _project, CommandLine _commandLine)
    throws Exception;

  /**
   * Updates a project.
   *
   * @param _project The project.
   * @param _commandLine The command line for the project create command.
   *
   * @return Array of Error if any errors encountered during update.
   */
  public Error[] update (IProject _project, CommandLine _commandLine)
    throws Exception;

  /**
   * Removes the nature(s) from a project that this manager manages, or deletes
   * the project if no other natures exist for the project.
   *
   * @param _project The project.
   * @param _commandLine The command line for the project create command.
   */
  public void delete (IProject _project, CommandLine _commandLine)
    throws Exception;

  /**
   * Refreshes a project by synchronizing it against the files on disk.
   *
   * @param _project The project.
   * @param _commandLine The command line for the project create command.
   */
  public void refresh (IProject _project, CommandLine _commandLine)
    throws Exception;
}
