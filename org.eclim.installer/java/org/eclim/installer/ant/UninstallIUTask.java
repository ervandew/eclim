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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import org.apache.commons.io.IOUtils;

import org.apache.commons.lang.StringUtils;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import org.apache.tools.ant.taskdefs.Move;

import org.eclim.installer.step.EclipseInfo;

import org.eclim.installer.step.command.Command;
import org.eclim.installer.step.command.OutputHandler;
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
  implements OutputHandler
{
  private static final Logger logger =
    LoggerFactory.getLogger(UninstallIUTask.class);

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

    // remove temp formic repositories (update sites)
    // FIXME: not working correctly. See org.eclim.installer.eclipse.Application
    /*if (iu.equals("org.eclim.installer")){
      runCommand(
          new Command(this, new String[]{"-removeRepos"}),
          "removing temp repositories");
    }*/

    // uninstall the feature
    runCommand(new UninstallCommand(
        this, repository, iu, "org.eclipse.equinox.p2.director"),
        "uninstalling " + iu);

    // http://wiki.eclipse.org/Equinox/p2/FAQ
    // run p2 gc to fully remove feature artifacts
    runCommand(new Command(
        this,
        new String[]{"-profile", info.getProfileName()},
        "org.eclipse.equinox.p2.garbagecollector.application"),
        "invoking p2 gc");

    // FIXME: hack since the -removeRepos method isn't working.
    if (iu.equals("org.eclim.installer")){
      String prefs =
        info.getLocalPath() +
        "/p2/org.eclipse.equinox.p2.engine/profileRegistry/" +
        info.getProfileName() +
        ".profile/.data/.settings/org.eclipse.equinox.p2.metadata.repository.prefs";
      logger.info("Removing temp repositories from p2 prefs: " + prefs);
      File prefFile = new File(prefs);
      File prefFileNew = new File(prefs + ".tmp");
      if (!prefFile.exists()){
        logger.warn("P2 repository prefs file doesn't exist.");
      }else{
        BufferedReader in = null;
        BufferedWriter out = null;
        try{
          in = new BufferedReader(new FileReader(prefFile));
          out = new BufferedWriter(new FileWriter(prefFileNew));
          String line = null;
          while((line = in.readLine()) != null){
            if (line.indexOf("formic_") != -1){
              continue;
            }
            out.write(line);
            out.newLine();
          }
          IOUtils.closeQuietly(in);
          IOUtils.closeQuietly(out);

          Move move = new Move();
          move.setTaskName("move");
          move.setProject(getProject());
          move.setFile(prefFileNew);
          move.setTofile(prefFile);
          move.setFailOnError(false);
          move.setForce(true);
          move.setOverwrite(true);
          move.execute();
        }catch(Exception e){
          logger.error("Error removing temp repositories.", e);
        }finally{
          IOUtils.closeQuietly(in);
          IOUtils.closeQuietly(out);
        }
      }
    }
  }

  private void runCommand(Command command, String message)
  {
    log(StringUtils.capitalize(message) + "...");
    try{
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
      if (failonerror){
        throw new BuildException(e);
      }else{
        logger.error("Error " + message, e);
        return;
      }
    }finally{
      if (command != null){
        command.destroy();
      }
    }
  }

  public void process(String line)
  {
    logger.info(line);
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
