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

import java.io.FileInputStream;

import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import org.apache.commons.lang.StringUtils;

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
  private static final String COMMAND = "-command";

  private static String workspace;

  /**
   * Executes eclim using the supplied arguments.
   * <p/>
   * The "-command" argument will be prepended to the argument array you supply.
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
   * <p/>
   * The "-command" argument will be prepended to the argument array you supply.
   *
   * @param _args The arguments to pass to eclim.
   * @param _timeout Timeout in milliseconds.
   * @return The result of the command execution as a string.
   */
  public static String execute (String[] _args, long _timeout)
  {
    String[] args = new String[_args.length + 2];
    System.arraycopy(_args, 0, args, 2, _args.length);
    args[0] = ECLIM;
    args[1] = COMMAND;

    System.out.println("Command: " + StringUtils.join(args, ' '));

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
      System.out.println("OUT: " + process.getResult());
      System.out.println("ERR: " + process.getErrorMessage());
      throw new RuntimeException("Command failed: " + process.getReturnCode());
    }

    String result = process.getResult();

    // strip off trailing newline char and return
    return result.substring(0, result.length() - 1);
  }

  /**
   * Determines if a project with the supplied name exists.
   *
   * @return true if the project exists, false otherwise.
   */
  public static boolean projectExists (String _name)
  {
    String list = Eclim.execute(new String[]{"project_info"});

    return Pattern.compile(_name + "\\s+-").matcher(list).find();
  }

  /**
   * Gets the path to the current workspace.
   *
   * @return The workspace path.
   */
  public static String getWorkspace ()
  {
    if(workspace == null){
      workspace = execute(new String[]{"workspace_dir"});
    }
    return workspace;
  }

  /**
   * Constructs a full path for the given project relative file.
   *
   * @param _project The name of the project the file belongs to.
   * @param _file The project relative file path.
   * @return The absolute path to the file.
   */
  public static String resolveFile (String _project, String _file)
  {
    return new StringBuffer()
      .append(getWorkspace()).append('/')
      .append(_project).append('/')
      .append(_file)
      .toString();
  }

  /**
   * Reads the project relative file into a string which is then returned.
   *
   * @param _project The name of the project the file belongs to.
   * @param _file The project relative file path.
   * @return The file contents as a string.
   */
  public static String fileToString (String _project, String _file)
  {
    String file = resolveFile(_project, _file);
    FileInputStream fin = null;
    try{
      fin = new FileInputStream(file);
      return IOUtils.toString(fin);
    }catch(Exception e){
      throw new RuntimeException(e);
    }finally{
      IOUtils.closeQuietly(fin);
    }
  }
}
