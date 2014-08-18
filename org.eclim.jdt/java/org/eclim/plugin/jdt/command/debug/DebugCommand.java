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

/**
 * Command to manage an existing debug session.
 */
@Command(
  name = "java_debug_control",
  options =
    "REQUIRED a action ARG"
)
public class DebugCommand
  extends AbstractCommand
{
  private static final String ACTION_STOP = "stop";
  private static final String ACTION_TERMINATE = "terminate";
  private static final String ACTION_SUSPEND = "suspend";
  private static final String ACTION_RESUME = "resume";

  @Override
  public Object execute(CommandLine commandLine) throws Exception
  {
    String action = commandLine.getValue(Options.ACTION_OPTION);
    DebuggerContext ctx = DebuggerContextManager.getDefault();
    if (ctx == null) {
      return Services.getMessage("debugging.session.absent");
    }

    if (action.equals(ACTION_STOP)) {
      ctx.stop();
      DebuggerContextManager.remove(ctx.getId());
      return Services.getMessage("debugging.session.stopped");

    } else if (action.equals(ACTION_TERMINATE)) {
      ctx.terminate();
      DebuggerContextManager.remove(ctx.getId());
      return Services.getMessage("debugging.session.stopped");

    } else if (action.equals(ACTION_SUSPEND)) {
      ctx.suspend();

    } else if (action.equals(ACTION_RESUME)) {
      ctx.resume();
    } else {
      throw new IllegalArgumentException("action: " + action);
    }

    return null;
  }
}
