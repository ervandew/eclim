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

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.logging.Logger;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclipse.debug.core.DebugException;

import org.eclipse.debug.core.model.IThread;

/**
 * Command to step through the debug execution flow.
 */
@Command(
  name = "java_debug_step",
  options =
    "REQUIRED a action ARG,"
)
public class StepCommand
  extends AbstractCommand
{
  private static final Logger logger = Logger.getLogger(StepCommand.class);

  private static final String ACTION_INTO = "into";
  private static final String ACTION_OVER = "over";
  private static final String ACTION_RETURN = "return";

  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    IThread steppingThread = getSteppingThread();
    if (steppingThread == null) {
      logger.debug("No thread avaiable for stepping");
      return null;
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

  /**
   * Returns the thread that needs to be stepped through.
   *
   * TODO Should determine based on user selection. Now, it assumes only one
   * thread can be in the suspended state and returns it.
   */
  private IThread getSteppingThread()
    throws DebugException
  {
    DebuggerContext ctx = DebuggerContextManager.getDefault();
    IThread[] threads = ctx.getDebugTarget().getThreads();
    for (IThread thread : threads) {
      if (thread.canStepInto()) {
        logger.debug("Stepping thread = " + thread.getName());
        return thread;
      }
    }

    return null;
  }
}
