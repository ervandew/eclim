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
 * Command to start a debug session using a configuration.
 */
@Command(
  name = "java_debug_start",
  options =
    "REQUIRED n target_name ARG," +
    "REQUIRED h host ARG," +
    "REQUIRED p port ARG," +
    "REQUIRED v vim_instance_name ARG"
)
public class DebugStartCommand extends AbstractCommand
{
  private static final Logger logger = Logger.getLogger(DebugStartCommand.class);

  @Override
  public Object execute(CommandLine commandLine) throws Exception
  {
    if (logger.isInfoEnabled()) {
      logger.info("Command: " + commandLine);
    }

    String debugTargetName = commandLine.getValue(Options.NAME_OPTION);
    String host = commandLine.getValue(DebugOptions.HOST_OPTION);
    String port = commandLine.getValue(DebugOptions.PORT_OPTION);
    String vimInstanceId = commandLine.getValue(DebugOptions.VIM_INSTANCE_OPTION);

    if (host == null || host.equals("")) {
      throw new IllegalArgumentException("Invalid host");
    }

    if (port == null || port.equals("")) {
      throw new IllegalArgumentException("Invalid port");
    }

    try {
      Integer.parseInt(port);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid port: " + port);
    }

    DebuggerContext.getInstance().start(debugTargetName,
        host, port, vimInstanceId);
    return null;
  }
}
