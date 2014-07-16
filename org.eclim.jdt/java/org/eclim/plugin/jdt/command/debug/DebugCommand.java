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
package org.eclim.plugin.jdt.command.debug;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.DebugOptions;
import org.eclim.command.Options;

import org.eclim.logging.Logger;

import org.eclim.plugin.core.command.AbstractCommand;

/**
 * Command to handle start, stop, resume debug request.
 */
@Command(
  name = "java_debug",
  options =
    "REQUIRED a action ARG," +
    "REQUIRED n target_name ARG," +
    "REQUIRED c connection ARG"
)
public class DebugCommand extends AbstractCommand {
  private static final Logger logger = Logger.getLogger(DebugCommand.class);

  private static final String ACTION_START = "start";

  private static final String ACTION_STOP = "stop";

  private static final String ACTION_RESUME = "resume";

  @Override
  public Object execute(CommandLine commandLine) throws Exception {
    if (logger.isInfoEnabled()) {
      logger.info("Command: " + commandLine);
    }

    String debugTargetName = commandLine.getValue(Options.NAME_OPTION);
    String conn = commandLine.getValue(DebugOptions.CONNECTION_OPTION);
    String[] connParts = conn.split(":");

    String action = commandLine.getValue(DebugOptions.ACTION_OPTION);
    if (action.equals(ACTION_START)) {
      DebuggerContext.getInstance().createDebugTarget(debugTargetName,
          connParts[0], connParts[1]);

    } else if (action.equals(ACTION_STOP)) {
      // TODO

    } else if (action.equals(ACTION_RESUME)) {
      DebuggerContext.getInstance().resume();
    }

    return null;
  }
}
