/**
 * Copyright (C) 2014 Daniel Leong
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

import org.eclipse.debug.core.DebugException;

/**
 * Command to terminate a running launch. If no launch id
 *  is provided, all current launches are terminated.
 *
 * @author Daniel Leong
 */
@Command(
  name = "project_run_terminate",
  options = "OPTIONAL l launchid ARG"
)
public class ProjectRunTerminateCommand
  extends AbstractCommand
{
  private static final Logger logger =
    Logger.getLogger(ProjectRunTerminateCommand.class);

  @Override
  public Object execute(CommandLine commandLine)
      throws Exception
  {
    final String launchId = commandLine.getValue(Options.LAUNCH_ID_OPTION);
    try {
      if (launchId != null) {
        if (EclimLaunchManager.terminate(launchId)) {
          logger.info("Terminated {}", launchId);
          return Services.getMessage("project.execute.terminated", launchId);
        } else {
          return Services.getMessage("project.execute.terminate.nosuch", launchId);
        }
      } else {
        logger.info("Terminating all running procs");
        EclimLaunchManager.terminateAll();
        return Services.getMessage("project.execute.terminated.all");
      }
    } catch (final DebugException e) {
      logger.error("Unable to terminate launch!", e);
      return Services.getMessage("project.execute.terminate.failed", launchId);
    }
  }
}
