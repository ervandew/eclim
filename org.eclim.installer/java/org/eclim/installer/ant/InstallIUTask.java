/**
 * Copyright (C) 2012  Eric Van Dewoestine
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
import org.apache.tools.ant.Task;

import org.eclim.installer.step.command.Command;
import org.eclim.installer.step.command.InstallCommand;

/**
 * Ant task to install an eclipse install unit.
 *
 * @author Eric Van Dewoestine
 */
public class InstallIUTask
  extends Task
{
  private String repository;
  private String iu;

  public void execute()
    throws BuildException
  {
    if (iu == null || repository == null){
      throw new BuildException("Attributes 'iu' and 'repository' must be set.");
    }

    Command command = null;
    try{
      log("Installing " + iu + "...");

      command = new InstallCommand(
          null, repository, iu, "org.eclipse.equinox.p2.director");
      command.start();
      command.join();
      if(command.getReturnCode() != 0){
        throw new BuildException(
            "error: " + command.getErrorMessage() +
            " out: " + command.getResult());
      }
    }catch(BuildException e){
      throw e;
    }catch(Exception e){
      throw new BuildException(e);
    }finally{
      if (command != null){
        command.destroy();
      }
    }
  }

  public void setRepository(String repository)
  {
    this.repository = repository;
  }

  public void setIU(String iu)
  {
    this.iu = iu;
  }
}
