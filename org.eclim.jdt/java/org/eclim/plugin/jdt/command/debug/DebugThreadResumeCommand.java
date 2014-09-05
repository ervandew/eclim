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

import org.eclim.logging.Logger;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.jdt.command.debug.context.DebuggerContext;
import org.eclim.plugin.jdt.command.debug.context.DebuggerContextManager;
import org.eclim.plugin.jdt.command.debug.context.ThreadContext;

import org.eclipse.jdt.debug.core.IJavaThread;

/**
 * Command to resume one or more threads.
 */
@Command(
  name = "java_debug_thread_resume",
  options =
    "OPTIONAL t thread_id ARG"
)
public class DebugThreadResumeCommand
  extends AbstractCommand
{
  private static final Logger logger =
    Logger.getLogger(DebugThreadResumeCommand.class);

  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    DebuggerContext ctx = DebuggerContextManager.getDefault();
    if (ctx == null) {
      return Services.getMessage("debugging.session.absent");
    }

    ThreadContext threadCtx = ctx.getThreadContext();
    String threadIdStr = commandLine.getValue(Options.THREAD_ID_OPTION);
    long threadId;

    if (threadIdStr == null) {
      ctx.resume();
      return Services.getMessage("debugging.session.resumed");
    } else {
      // Select the currently stepping thread if an empty thread ID is given.
      if (threadIdStr.isEmpty()) {
        IJavaThread steppingThread = (IJavaThread) threadCtx.getSteppingThread();
        if (steppingThread != null) {
          threadId = steppingThread.getThreadObject().getUniqueId();
        } else {
          return Services.getMessage("debugging.resume.thread.absent");
        }
      } else {
        threadId = Long.parseLong(threadIdStr);
      }

      if (logger.isDebugEnabled()) {
        logger.debug("Resuming thread ID: " + threadId);
      }
      threadCtx.resume(threadId);
      return Services.getMessage("debugging.thread.resumed");
    }
  }
}
