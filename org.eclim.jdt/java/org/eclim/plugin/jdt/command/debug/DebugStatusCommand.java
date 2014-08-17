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

import java.util.HashMap;
import java.util.Map;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclipse.debug.core.model.IDebugTarget;

/**
 * Command to retrieve status of all active debug sessions.
 */
@Command(
  name = "java_debug_status",
  options = ""
)
public class DebugStatusCommand
  extends AbstractCommand
{
  private static final String CONNECTED = "Connected";
  private static final String DISCONNECTED = "Disconnected";
  private static final String SUSPENDED = "Suspended";
  private static final String TERMINATED = "Terminated";

  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {

    Map<String, Object> statusMap = new HashMap<String, Object>();

    DebuggerContext ctx = DebuggerContext.getInstance();
    IDebugTarget debugTarget = ctx.getDebugTarget();

    String status = CONNECTED;
    if (debugTarget.isDisconnected()) {
      status = DISCONNECTED;
    } else if (debugTarget.isSuspended()) {
      status = SUSPENDED;
    } else if (debugTarget.isTerminated()) {
      status = TERMINATED;
    }

    statusMap.put("status", ctx.getName() + " (" + status + ")");
    statusMap.put("threads", ctx.getThreadContext().get());
    statusMap.put("variables", ctx.getVariableContext().get());

    return statusMap;
  }
}
