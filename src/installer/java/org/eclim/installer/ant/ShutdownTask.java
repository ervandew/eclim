/**
 * Copyright (c) 2005 - 2008
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class ShutdownTask
  extends Task
{
  private static final String ECLIM_HOME = "org.eclim_";
  private static final String ECLIM_LINUX = "bin/eclim";
  private static final String ECLIM_WINDOWS = "bin/eclim.bat";
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
        eclim = FilenameUtils.concat(eclimHome[0], eclim);
        eclim = FilenameUtils.concat(
            project.replaceProperties(ECLIPSE_PLUGINS), eclim);
        if(new File(eclim).exists()){
          CommandExecutor command = CommandExecutor.execute(
              new String[]{eclim, "-command", "shutdown"}, WAIT_TIME);

          if(command.getReturnCode() != 0){
            log("Error while attempting to shut down eclimd: " +
                command.getErrorMessage());
          }else{
            // FIXME: Add a WaitFor call?
            Thread.sleep(WAIT_TIME);
          }
        }
      }
    }catch(Exception e){
      log("Error while attempting to shut down eclimd: " +
          e.getClass().getName() + " - " + e.getMessage());
    }
  }
}
