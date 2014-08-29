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

import org.eclim.plugin.jdt.command.debug.context.DebuggerContext;
import org.eclim.plugin.jdt.command.debug.context.DebuggerContextManager;

/**
 * Command to suspend one or more threads.
 */
@Command(
  name = "java_debug_thread_suspend",
  options =
    "OPTIONAL t thread_id ARG"
)
public class DebugThreadSuspendCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    DebuggerContext ctx = DebuggerContextManager.getDefault();
    if (ctx == null) {
      return Services.getMessage("debugging.session.absent");
    }

    String threadId = commandLine.getValue(Options.THREAD_ID_OPTION);
    if (threadId == null) {
      ctx.suspend();
      return Services.getMessage("debugging.session.suspended");
    } else {
      ctx.getThreadContext().suspend(Long.parseLong(threadId));
      return Services.getMessage("debugging.thread.suspended");
    }
  }
}
