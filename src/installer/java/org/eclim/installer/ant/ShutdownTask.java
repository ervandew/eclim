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

import java.io.File;
import java.io.FilenameFilter;

import org.apache.commons.io.FilenameUtils;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import org.apache.tools.ant.taskdefs.condition.Os;

import org.formic.util.CommandExecutor;

/**
 * Task for shutting down eclimd.
 *
 * @author Eric Van Dewoestine
 */
public class ShutdownTask
  extends Task
{
  private static final String ECLIM_HOME = "org.eclim_";
  private static final String ECLIM_LINUX = "eclim";
  private static final String ECLIM_WINDOWS = "eclim.bat";
  private static final String ECLIPSE_PLUGINS = "${eclipse.home}/plugins";

  private static final long WAIT_TIME = 3000;

  /**
   * Executes this task.
   */
  public void execute ()
    throws BuildException
  {
    Project project = getProject();
    try{
      String[] eclimHome = new File(project.replaceProperties(ECLIPSE_PLUGINS))
        .list(new FilenameFilter(){
          public boolean accept (File dir, String name){
            return name.startsWith(ECLIM_HOME);
          }
        });

      if(eclimHome.length > 0){
        String eclim = Os.isFamily("windows") ? ECLIM_WINDOWS : ECLIM_LINUX;
        String eclimPath = FilenameUtils.concat(
            project.replaceProperties("${eclipse.home}"), eclim);

        // try plugin bin dir if eclipse home path doesn't exist
        if (!new File(eclimPath).exists()){
          eclimPath = FilenameUtils.concat(eclimHome[0], "bin");
          eclimPath = FilenameUtils.concat(eclimPath, eclim);
          eclimPath = FilenameUtils.concat(
              project.replaceProperties(ECLIPSE_PLUGINS), eclimPath);
        }

        if(new File(eclimPath).exists()){
          CommandExecutor command = CommandExecutor.execute(
              new String[]{eclimPath, "-command", "shutdown"}, WAIT_TIME);

          if(command.getReturnCode() != 0){
            String message = command.getErrorMessage();
            if (message == null || message.trim().equals("")){
              message = command.getResult();
            }
            log("Error attempting to shut down eclimd: " + message);
          }else{
            // FIXME: Add a WaitFor call?
            Thread.sleep(WAIT_TIME);
          }
        }
      }
    }catch(Exception e){
      log("Error attempting to shut down eclimd: " +
          e.getClass().getName() + " - " + e.getMessage());
    }
  }
}
