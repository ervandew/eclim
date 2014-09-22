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
import org.eclim.plugin.jdt.command.debug.context.ThreadContext;

import org.eclipse.jdt.debug.core.IJavaThread;

/**
 * Command to step through the debug execution flow.
 */
@Command(
  name = "java_debug_step",
  options =
    "REQUIRED a action ARG," +
    "OPTIONAL t thread_id ARG,"
)
public class StepCommand
  extends AbstractCommand
{
  private static final String ACTION_INTO = "into";
  private static final String ACTION_OVER = "over";
  private static final String ACTION_RETURN = "return";

  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    DebuggerContext ctx = DebuggerContextManager.getDefault();
    ThreadContext threadCtx = ctx.getThreadContext();

    String threadId = commandLine.getValue(Options.THREAD_ID_OPTION);
    IJavaThread steppingThread = null;
    if (threadId == null) {
      steppingThread = threadCtx.getSteppingThread();
    } else {
      steppingThread = threadCtx.getThread(Long.parseLong(threadId));
    }

    if (steppingThread == null) {
      return Services.getMessage("debugging.stepping.thread.absent");
    } else if (!steppingThread.isSuspended()) {
      return Services.getMessage("debugging.stepping.thread.not.suspended");
    }

    if (steppingThread != null) {
      // Set this thread to be the stepping thread
      threadCtx.setSteppingThread(steppingThread);
    }

    String action = commandLine.getValue(Options.ACTION_OPTION);

    if (action.equals(ACTION_INTO)) {
      steppingThread.stepInto();

    } else if (action.equals(ACTION_OVER)) {
      steppingThread.stepOver();

    } else if (action.equals(ACTION_RETURN)) {
      steppingThread.stepReturn();
    }

    return null;
  }
}
