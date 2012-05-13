/**
 * Copyright (C) 2005 - 2012  Eric Van Dewoestine
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

import org.eclim.installer.step.EclipseInfo;

import org.eclim.installer.step.command.Command;
import org.eclim.installer.step.command.UninstallCommand;

import org.formic.Installer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ant task to remove an eclipse install unit.
 *
 * @author Eric Van Dewoestine
 */
public class UninstallIUTask
  extends Task
{
  private static final Logger logger = LoggerFactory.getLogger(UninstallIUTask.class);

  private String repository;
  private String iu;
  private boolean failonerror = true;

  /**
   * Executes this task.
   */
  public void execute()
    throws BuildException
  {
    if (iu == null){
      throw new BuildException("Attribute 'iu' must be set.");
    }

    EclipseInfo info = (EclipseInfo)
      Installer.getContext().getValue("eclipse.info");

    Command command = null;
    try{
      log("Uninstalling " + iu + "...");

      command = new UninstallCommand(
          null, repository, iu, "org.eclipse.equinox.p2.director");
      command.start();
      command.join();
      if(command.getReturnCode() != 0){
        logger.warn(
            "error: " + command.getErrorMessage() +
            " out: " + command.getResult());
        if (failonerror){
          throw new BuildException(
              "error: " + command.getErrorMessage() +
              " out: " + command.getResult());
        }
      }
    }catch(Exception e){
      throw new BuildException(e);
    }finally{
      if (command != null){
        command.destroy();
        command = null;
      }
    }

    try{
      // http://wiki.eclipse.org/Equinox/p2/FAQ
      log("Invoking p2 gc...");

      command = new Command(
          null,
          new String[]{"-profile", info.getProfileName()},
          "org.eclipse.equinox.p2.garbagecollector.application");
      command.start();
      command.join();
      if(command.getReturnCode() != 0){
        logger.warn(
            "error: " + command.getErrorMessage() +
            " out: " + command.getResult());
        if (failonerror){
          throw new BuildException(
              "error: " + command.getErrorMessage() +
              " out: " + command.getResult());
        }
      }
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

  public void setFailonerror(boolean failonerror)
  {
    this.failonerror = failonerror;
  }
}
