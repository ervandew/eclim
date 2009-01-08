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
package org.eclim.installer.step.command;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;

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

  public Command (OutputHandler handler, String[] cmd, String to)
  {
    this.handler = handler;
    this.cmd = new String[cmd.length + 1];

    this.cmd[0] = Installer.getProject().replaceProperties(
        "${eclim.plugins}/org.eclim.installer_${eclim.version}/bin/install");
    if (Os.isFamily("windows")){
      this.cmd[0] += ".bat";
    }
    //this.cmd = new String[cmd.length + (to != null ? 3 : 1)];
    //this.cmd[this.cmd.length - 2] = "-to";
    //this.cmd[this.cmd.length - 1] = to;

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
              if (handler != null){
                handler.process(line);
              }
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
