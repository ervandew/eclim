/**
 * Copyright (C) 2014  Eric Van Dewoestine
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

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.util.ProjectUtils;

/**
 * Command to start a debug session.
 */
@Command(
  name = "java_debug_start",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED h host ARG," +
    "REQUIRED n port ARG," +
    "REQUIRED v vim_instance_name ARG"
)
public class DebugStartCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    String vimInstanceId = commandLine.getValue(Options.VIM_INSTANCE_OPTION);
    String host = commandLine.getValue(Options.HOST_OPTION);
    int port = commandLine.getIntValue(Options.PORT_NUMBER_OPTION);

    DebuggerContext ctx = new DebuggerContext(
        ProjectUtils.getProject(projectName), host, port, vimInstanceId);

    DebuggerContextManager.add(ctx);
    // Start context only after adding to context manager. There are other
    // classes that lookup the context once the context is started and events
    // are fired.
    ctx.start();

    return Services.getMessage("debugging.session.started");
  }
}
