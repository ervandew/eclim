/**
 * Copyright (C) 2005 - 2017  Eric Van Dewoestine
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
package org.eclim.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Runs an external process.
 *
 * @author Eric Van Dewoestine
 */
public class CommandExecutor
  implements Runnable
{
  private int returnCode = -1;
  private String[] cmd;
  private String result;
  private String error;
  private Process process;

  /**
   * Construct a new instance.
   */
  private CommandExecutor (String[] cmd)
  {
    this.cmd = cmd;
  }

  /**
   * Execute the supplied command.
   *
   * @param cmd The command to execute.
   * @return The CommandExecutor instance containing the ending state of the
   * process.
   */
  public static CommandExecutor execute(String[] cmd)
  {
    return execute(cmd, -1);
  }

  /**
   * Execute the supplied command.
   *
   * @param cmd The command to execute.
   * @param timeout Timeout in milliseconds.
   * @return The CommandExecutor instance containing the ending state of the
   * process.
   */
  public static CommandExecutor execute(String[] cmd, long timeout)
  {
    CommandExecutor executor = new CommandExecutor(cmd);

    Thread thread = new Thread(executor);
    thread.start();

    try{
      if(timeout > 0){
        thread.join(timeout);
      }else{
        thread.join();
      }
    }catch(InterruptedException ie){
      throw new RuntimeException(ie);
    }

    return executor;
  }

  /**
   * Run the thread.
   */
  public void run()
  {
    try{
      Runtime runtime = Runtime.getRuntime();
      process = runtime.exec(cmd);

      final ByteArrayOutputStream out = new ByteArrayOutputStream();
      final ByteArrayOutputStream err = new ByteArrayOutputStream();

      Thread outThread = new Thread(){
        public void run()
        {
          try{
            IOUtils.copy(process.getInputStream(), out);
          }catch(IOException ioe){
            ioe.printStackTrace();
          }
        }
      };
      outThread.start();

      Thread errThread = new Thread(){
        public void run()
        {
          try{
            IOUtils.copy(process.getErrorStream(), err);
          }catch(IOException ioe){
            ioe.printStackTrace();
          }
        }
      };
      errThread.start();

      returnCode = process.waitFor();
      outThread.join(1000);
      errThread.join(1000);

      result = out.toString();
      error = err.toString();
    }catch(Exception e){
      returnCode = 12;
      error = e.getMessage();
      e.printStackTrace();
    }
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

  /**
   * Gets the output of the command.
   *
   * @return The command result.
   */
  public String getResult()
  {
    return result;
  }

  /**
   * Get the return code from the process.
   *
   * @return The return code.
   */
  public int getReturnCode()
  {
    return returnCode;
  }

  /**
   * Gets the error message from the command if there was one.
   *
   * @return The possibly empty error message.
   */
  public String getErrorMessage()
  {
    return error;
  }
}
