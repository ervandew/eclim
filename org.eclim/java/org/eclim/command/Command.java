/**
 * Copyright (C) 2005 - 2017  Eric Van Dewoestine
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
package org.eclim.command;

import org.eclim.command.CommandLine;

import com.martiansoftware.nailgun.NGContext;

/**
 * Defines a command that can be exected.
 *
 * @author Eric Van Dewoestine
 */
public interface Command
{
  /**
   * Executes the command with the supplied options.
   *
   * @param commandLine The commandLine options supplied.
   * @return The result.
   * @throws Exception If the command encounters an unexpected exception.
   */
  public Object execute(CommandLine commandLine)
    throws Exception;

  /**
   * Perform any post command execution cleanup.
   *
   * Intended for super classes to provide generic cleanup facilities for child
   * classes.
   *
   * @param commandLine The commandLine options supplied.
   */
  public void cleanup(CommandLine commandLine);

  /**
   * Gets the context for this instance.
   *
   * @return The context.
   */
  public NGContext getContext();

  /**
   * Sets the context for this instance.
   *
   * @param context The context.
   */
  public void setContext(NGContext context);
}
