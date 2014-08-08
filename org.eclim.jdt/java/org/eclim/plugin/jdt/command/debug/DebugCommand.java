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

import org.eclim.logging.Logger;

import org.eclim.plugin.core.command.AbstractCommand;

/**
 * Command to manage an existing debug session.
 */
@Command(
  name = "java_debug_control",
  options =
    "REQUIRED a action ARG"
)
public class DebugCommand extends AbstractCommand
{
  private static final Logger logger = Logger.getLogger(DebugCommand.class);

  private static final String ACTION_STOP = "stop";

  private static final String ACTION_TERMINATE = "terminate";

  private static final String ACTION_SUSPEND = "suspend";

  private static final String ACTION_RESUME = "resume";

  @Override
  public Object execute(CommandLine commandLine) throws Exception
  {
    if (logger.isInfoEnabled()) {
      logger.info("Command: " + commandLine);
    }

    String action = commandLine.getValue(DebugOptions.ACTION_OPTION);
    DebuggerContext debuggerContext = DebuggerContext.getInstance();

    if (action.equals(ACTION_STOP)) {
      debuggerContext.stop();

    } else if (action.equals(ACTION_TERMINATE)) {
      debuggerContext.terminate();

    } else if (action.equals(ACTION_SUSPEND)) {
      debuggerContext.suspend();

    } else if (action.equals(ACTION_RESUME)) {
      debuggerContext.resume();
    } else {
      throw new IllegalArgumentException("action: " + action);
    }

    return null;
  }
}
