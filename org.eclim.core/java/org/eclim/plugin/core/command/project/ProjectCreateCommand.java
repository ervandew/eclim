/**
 * Copyright (C) 2005 - 2014  Eric Van Dewoestine
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
package org.eclim.plugin.core.command.project;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.logging.Logger;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.project.ProjectManagement;

import org.eclim.util.file.FileUtils;

/**
 * Command to create a project.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "project_create",
  options =
    "REQUIRED f folder ARG," +
    "OPTIONAL p name ARG," +
    "REQUIRED n natures ARG," +
    "OPTIONAL d depends ARG," +
    "OPTIONAL a args ANY"
)
public class ProjectCreateCommand
  extends AbstractCommand
{
  private static final Logger logger =
    Logger.getLogger(ProjectCreateCommand.class);

  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String folder = commandLine.getValue(Options.FOLDER_OPTION);
    if(folder.endsWith("/") || folder.endsWith("\\")){
      folder = folder.substring(0, folder.length() - 1);
    }

    String name = commandLine.hasOption(Options.PROJECT_OPTION) ?
      commandLine.getValue(Options.PROJECT_OPTION) :
      FileUtils.getBaseName(folder).replace(' ', '_');
    logger.debug("Creating project '{}' at folder '{}'", name, folder);

    ProjectManagement.create(name, folder, commandLine);

    return Services.getMessage("project.created", name);
  }
}
