/**
 * Copyright (C) 2005 - 2009  Eric Van Dewoestine
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
package org.eclim.plugin.dltkruby.command.launching;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;

import org.eclim.logging.Logger;

import org.eclim.plugin.dltk.preference.DltkInterpreterTypeManager;

import org.eclim.util.CommandExecutor;

import org.eclipse.dltk.launching.IInterpreterInstallType;

/**
 * Command to add a ruby interpreter.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "ruby_add_interpreter",
  options =
    "REQUIRED n nature ARG," +
    "REQUIRED i interpreter ARG"
)
public class AddInterpreterCommand
  extends org.eclim.plugin.dltk.command.launching.AddInterpreterCommand
{
  private static final Logger logger =
    Logger.getLogger(AddInterpreterCommand.class);

  /**
   * {@inheritDoc}
   * @see org.eclim.plugin.dltk.command.launching.AddInterpreterCommand#getInterpreterInstallType(String,CommandLine)
   */
  @Override
  protected IInterpreterInstallType getInterpreterInstallType(
      String nature, CommandLine commandLine)
    throws Exception
  {
    String interpreterPath = commandLine.getValue("i");
    String type = "ruby";
    CommandExecutor process = CommandExecutor.execute(new String[]{
      interpreterPath, "--version"
    }, 5000);

    if(process.getReturnCode() == -1){
      process.destroy();
      logger.error("ruby command timed out.");
      throw new RuntimeException("ctags command timed out.");
    }else if(process.getReturnCode() > 0){
      logger.error("ruby command error: " + process.getErrorMessage());
      throw new RuntimeException("ruby error: " + process.getErrorMessage());
    }

    if (process.getResult().indexOf("jruby") != -1){
      type = "jruby";
    }

    return DltkInterpreterTypeManager.getInterpreterInstallType(type, nature);
  }
}
