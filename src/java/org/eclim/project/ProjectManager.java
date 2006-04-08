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
   * @param _name The project name.
   * @param _folder The project folder.
   * @param _commandLine The command line for the project create command.
   *
   * @return Project creation response.
   */
  public Object create (String _name, String _folder, CommandLine _commandLine)
    throws Exception;

  /**
   * Updates a project.
   *
   * @param _name The project name.
   * @param _commandLine The command line for the project create command.
   *
   * @return Project update response.
   */
  public Object update (String _name, CommandLine _commandLine)
    throws Exception;

  /**
   * Removes the nature(s) from a project that this manager manages, or deletes
   * the project if no other natures exist for the project.
   *
   * @param _name The project name.
   * @param _commandLine The command line for the project create command.
   *
   * @return Project deletion response.
   */
  public Object delete (String _name, CommandLine _commandLine)
    throws Exception;
}
