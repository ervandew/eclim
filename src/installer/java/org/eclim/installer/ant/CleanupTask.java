/**
 * Copyright (C) 2005 - 2010  Eric Van Dewoestine
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
package org.eclim.installer.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import org.eclim.installer.step.command.Command;
import org.eclim.installer.step.command.UninstallCommand;

import org.formic.Installer;

public class CleanupTask
  extends Task
{
  /**
   * Executes this task.
   */
  public void execute()
    throws BuildException
  {
    Command command = null;
    try{
      command = new UninstallCommand(
          null,
          "file://" + Installer.getProject().getProperty("basedir") + "/update",
          "org.eclim.installer",
          "org.eclipse.equinox.p2.director");
      command.start();
      command.join();
      if(command.getReturnCode() != 0){
        Installer.getProject().log(
            "error: " + command.getErrorMessage() +
            " out: " + command.getOutput(), Project.MSG_WARN);
      }
    }catch(Exception e){
      throw new BuildException(e);
    }finally{
      if (command != null){
        command.destroy();
      }
    }
  }
}
