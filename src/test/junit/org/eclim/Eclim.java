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
package org.eclim;

import org.eclim.util.CommandExecutor;

/**
 * Utility class to executing eclim commands and returning the results.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class Eclim
{
  private static final String ECLIM =
    System.getProperty("eclim.home") + "/bin/eclim";

  /**
   * Executes eclim using the supplied arguments.
   *
   * @param _args The arguments to pass to eclim.
   * @return The result of the command execution as a string.
   */
  public static String execute (String[] _args)
  {
    return execute(_args, -1);
  }

  /**
   * Executes eclim using the supplied arguments.
   *
   * @param _args The arguments to pass to eclim.
   * @param _timeout Timeout in milliseconds.
   * @return The result of the command execution as a string.
   */
  public static String execute (String[] _args, long _timeout)
  {
    String[] args = new String[_args.length + 1];
    System.arraycopy(_args, 0, args, 1, _args.length);
    args[0] = ECLIM;

    CommandExecutor process = null;
    try{
      process = CommandExecutor.execute(args, _timeout);
    }catch(Exception e){
      throw new RuntimeException(e);
    }

    if(process.getReturnCode() == -1){
      process.destroy();
      throw new RuntimeException("Command timed out.");
    }

    if(process.getReturnCode() != 0){
      System.out.println("ERR: " + process.getErrorMessage());
      System.out.println("OUT: " + process.getResult());
      throw new RuntimeException("Command failed: " + process.getReturnCode());
    }

    String result = process.getResult();

    // strip off trailing newline char and return
    return result.substring(0, result.length() - 1);
  }
}
