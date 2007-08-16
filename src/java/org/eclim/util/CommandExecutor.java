/**
 * Copyright (c) 2005 - 2006
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
package org.eclim.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Runs an external process.
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
  public static CommandExecutor execute (String[] cmd)
    throws Exception
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
  public static CommandExecutor execute (String[] cmd, long timeout)
    throws Exception
  {
    CommandExecutor executor = new CommandExecutor(cmd);

    Thread thread = new Thread(executor);
    thread.start();

    if(timeout > 0){
      thread.join(timeout);
    }else{
      thread.join();
    }

    return executor;
  }

  /**
   * Run the thread.
   */
  public void run ()
  {
    try{
      Runtime runtime = Runtime.getRuntime();
      process = runtime.exec(cmd);

      final ByteArrayOutputStream out = new ByteArrayOutputStream();
      final ByteArrayOutputStream err = new ByteArrayOutputStream();

      Thread outThread = new Thread(){
        public void run (){
          try{
            IOUtils.copy(process.getInputStream(), out);
          }catch(IOException ioe){
            ioe.printStackTrace();
          }
        }
      };
      outThread.start();

      Thread errThread = new Thread(){
        public void run (){
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
  public void destroy ()
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
  public String getResult ()
  {
    return result;
  }

  /**
   * Get the return code from the process.
   *
   * @return The return code.
   */
  public int getReturnCode ()
  {
    return returnCode;
  }

  /**
   * Gets the error message from the command if there was one.
   *
   * @return The possibly empty error message.
   */
  public String getErrorMessage ()
  {
    return error;
  }
}
