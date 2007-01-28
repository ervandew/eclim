/**
 * Copyright (c) 2005 - 2007
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
package org.eclim.installer.step.command;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import org.apache.commons.lang.StringUtils;

import org.apache.tools.ant.taskdefs.condition.Os;

import org.formic.Installer;

/**
 * Abstract class encapsulating logic to execute an eclipse feature command.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public abstract class Command
  extends Thread
{
  private Process process;
  private int returnCode;
  private String errorMessage;
  private String[] cmd;
  private OutputHandler handler;

  public Command (OutputHandler handler, String[] cmd)
  {
    this.handler = handler;
    this.cmd = new String[cmd.length + 1];

    this.cmd[0] = Installer.getProject().replaceProperties(
        "${eclipse.home}/plugins/org.eclim.installer/bin/install");
    if (Os.isFamily("windows")){
      this.cmd[0] += ".bat";
    }

    System.arraycopy(cmd, 0, this.cmd, 1, cmd.length);
  }

  public void run ()
  {
    try{
      System.out.println(this);
      Runtime runtime = Runtime.getRuntime();
      process = runtime.exec(cmd);

      final ByteArrayOutputStream err = new ByteArrayOutputStream();

      Thread outThread = new Thread(){
        public void run (){
          try{
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            String line = null;
            while((line = reader.readLine()) != null){
              handler.process(line);
            }
          }catch(Exception e){
            e.printStackTrace();
            errorMessage = e.getMessage();
            returnCode = 1000;
            process.destroy();
          }
        }
      };
      outThread.start();

      Thread errThread = new Thread(){
        public void run (){
          try{
            IOUtils.copy(process.getErrorStream(), err);
          }catch(Exception e){
            e.printStackTrace();
          }
        }
      };
      errThread.start();

      returnCode = process.waitFor();
      outThread.join();
      errThread.join();

      if(errorMessage == null){
        errorMessage = err.toString();
      }
    }catch(Exception e){
      returnCode = 12;
      errorMessage = e.getMessage();
      e.printStackTrace();
    }
  }

  /**
   * Gets the returnCode for this instance.
   *
   * @return The returnCode.
   */
  public int getReturnCode ()
  {
    return this.returnCode;
  }

  /**
   * Gets the errorMessage for this instance.
   *
   * @return The errorMessage.
   */
  public String getErrorMessage ()
  {
    return this.errorMessage;
  }

  /**
   * Destroy this process.
   */
  public void destroy ()
  {
    if(process != null){
      process.destroy();
    }
  }

  public String toString ()
  {
    return StringUtils.join(cmd, ' ');
  }
}
