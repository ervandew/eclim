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
package org.eclim.installer.step.command;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;

import org.apache.commons.lang.StringUtils;

import org.eclim.installer.step.EclipseUtils;

import org.formic.Installer;

/**
 * Abstract class encapsulating logic to execute an eclipse feature command.
 *
 * @author Eric Van Dewoestine
 */
public abstract class Command
  extends Thread
{
  private Process process;
  private int returnCode;
  private String errorMessage;
  private String output;
  private String[] cmd;
  private OutputHandler handler;

  public Command(OutputHandler handler, String[] cmd)
    throws Exception
  {
    this(handler, cmd, "org.eclim.installer.application");
  }

  public Command(OutputHandler handler, String[] cmd, String application)
    throws Exception
  {
    this.handler = handler;
    this.cmd = new String[cmd.length + 5];

    String eclipse = EclipseUtils.findEclipse();
    if (eclipse == null){
      throw new RuntimeException(
        "Could not find eclipse executable for eclipse home: " +
        Installer.getProject().getProperty("eclipse.home"));
    }

    this.cmd[0] = eclipse;
    this.cmd[1] = "-nosplash";
    this.cmd[2] = "-clean";
    this.cmd[3] = "-application";
    this.cmd[4] = application;

    System.arraycopy(cmd, 0, this.cmd, 5, cmd.length);
  }

  public void run()
  {
    try{
      System.out.println(this);
      Runtime runtime = Runtime.getRuntime();
      process = runtime.exec(cmd);

      final ByteArrayOutputStream err = new ByteArrayOutputStream();

      Thread outThread = new Thread(){
        public void run(){
          StringBuffer buffer = new StringBuffer();
          try{
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            String line = null;
            while((line = reader.readLine()) != null){
              if (buffer.length() != 0){
                buffer.append('\n');
              }
              buffer.append(line);

              if (handler != null){
                handler.process(line);
              }
            }
          }catch(Exception e){
            e.printStackTrace();
            errorMessage = e.getMessage();
            returnCode = 1000;
            process.destroy();
          }finally{
            output = buffer.toString();
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
  public int getReturnCode()
  {
    return this.returnCode;
  }

  /**
   * Gets the errorMessage for this instance.
   *
   * @return The errorMessage.
   */
  public String getErrorMessage()
  {
    return this.errorMessage;
  }

  /**
   * Gets the standard out content from the command.
   *
   * @return The standard out content.
   */
  public String getOutput()
  {
    return this.output;
  }

  /**
   * Destroy this process.
   */
  public void destroy()
  {
    if(process != null){
      process.destroy();
    }
  }

  public String toString()
  {
    return StringUtils.join(cmd, ' ');
  }
}
