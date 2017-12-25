/**
 * Copyright (C) 2005 - 2017  Eric Van Dewoestine
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
package org.eclim.plugin.core.project;

import java.util.List;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

/**
 * Defines methods for managing a project.
 *
 * @author Eric Van Dewoestine
 */
public interface ProjectManager
{
  /**
   * Creates a new project, or if a project already exists, updates it to add
   * the necessary nature(s) for the type of project being created.
   *
   * @param project The project.
   * @param commandLine The command line for the project create command.
   */
  public void create(IProject project, CommandLine commandLine);

  /**
   * Updates a project.
   *
   * @param project The project.
   * @param commandLine The command line for the project create command.
   *
   * @return Array of Error if any errors encountered during update.
   */
  public List<Error> update(IProject project, CommandLine commandLine);

  /**
   * Removes the nature(s) from a project that this manager manages, or deletes
   * the project if no other natures exist for the project.
   *
   * @param project The project.
   * @param commandLine The command line for the project create command.
   */
  public void delete(IProject project, CommandLine commandLine);

  /**
   * Refreshes a project by synchronizing it against the files on disk.
   *
   * @param project The project.
   * @param commandLine The command line for the project create command.
   */
  public void refresh(IProject project, CommandLine commandLine);

  /**
   * Refreshes a project file.
   *
   * @param project The project.
   * @param file The file to refresh.
   */
  public void refresh(IProject project, IFile file);
}
