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
package org.eclim;

import java.io.FileInputStream;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import org.apache.tools.ant.taskdefs.condition.Os;

import org.eclim.util.CommandExecutor;
import org.eclim.util.IOUtils;

/**
 * Utility class to executing eclim commands and returning the results.
 *
 * @author Eric Van Dewoestine
 */
public class Eclim
{
  public static final String TEST_PROJECT = "eclim_unit_test";

  private static final String ECLIM =
    System.getProperty("eclipse.home") + "/eclim";
  private static final String PORT = System.getProperty("eclimd.port");
  private static final String COMMAND = "-command";

  private static String workspace;

  /**
   * Executes eclim using the supplied arguments.
   * <p/>
   * The "-command" argument will be prepended to the argument array you supply.
   *
   * @param args The arguments to pass to eclim.
   * @return The result of the command execution as a string.
   */
  public static String execute(String[] args)
  {
    return execute(args, -1);
  }

  /**
   * Executes eclim using the supplied arguments.
   * <p/>
   * The "-command" argument will be prepended to the argument array you supply.
   *
   * @param args The arguments to pass to eclim.
   * @param timeout Timeout in milliseconds.
   * @return The result of the command execution as a string.
   */
  public static String execute(String[] args, long timeout)
  {
    String[] arguments = null;
    if (Os.isFamily(Os.FAMILY_WINDOWS)){
      String eclimCmd = ECLIM + ".cmd";
      String drive = eclimCmd.substring(0, 2);
      arguments = new String[3];
      arguments[0] = "cmd.exe";
      arguments[1] = "/c";
      arguments[2] = drive + " && \"" + eclimCmd + "\" --nailgun-port " +
        PORT + " " + COMMAND + " \"" + StringUtils.join(args, "\" \"") + "\"";
    }else{
      arguments = new String[args.length + 4];
      System.arraycopy(args, 0, arguments, 4, args.length);
      arguments[0] = ECLIM;
      arguments[1] = "--nailgun-port";
      arguments[2] = PORT;
      arguments[3] = COMMAND;
    }

    System.out.println("Command: " + StringUtils.join(arguments, ' '));

    CommandExecutor process = null;
    try{
      process = CommandExecutor.execute(arguments, timeout);
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
    result = result.substring(0, result.length() - 1);
    if (Os.isFamily(Os.FAMILY_WINDOWS)){
      result = result.replaceAll("\\r$", "");
    }
    return result;
  }

  /**
   * Determines if a project with the supplied name exists.
   *
   * @return true if the project exists, false otherwise.
   */
  public static boolean projectExists(String name)
  {
    String list = Eclim.execute(new String[]{"project_list"});

    return Pattern.compile(name + "\\s+-").matcher(list).find();
  }

  /**
   * Gets the path to the current workspace.
   *
   * @return The workspace path.
   */
  public static String getWorkspace()
  {
    if(workspace == null){
      workspace = execute(new String[]{"workspace_dir"}).replace('\\', '/');
    }
    return workspace;
  }

  /**
   * Gets the path to the specified project.
   *
   * @param name The project name.
   * @return The project path.
   */
  public static String getProjectPath(String name)
  {
    String[] results = StringUtils.split(
        execute(new String[]{"project_list"}), '\n');
    Pattern pattern =
      Pattern.compile("^" + name + "\\b\\s+- \\w+\\s+- (.*)$");
    for (String result : results) {
      Matcher matcher = pattern.matcher(result);
      if (matcher.matches()){
        return matcher.group(1);
      }
    }
    return null;
  }

  /**
   * Constructs a full path for the given project relative file.
   *
   * @param file The project relative file path.
   * @return The absolute path to the file.
   */
  public static String resolveFile(String file)
  {
    return new StringBuffer()
      .append(getWorkspace()).append('/')
      .append(TEST_PROJECT).append('/')
      .append(file)
      .toString();
  }

  /**
   * Constructs a full path for the given project relative file.
   *
   * @param project The name of the project the file belongs to.
   * @param file The project relative file path.
   * @return The absolute path to the file.
   */
  public static String resolveFile(String project, String file)
  {
    return new StringBuffer()
      .append(getWorkspace()).append('/')
      .append(project).append('/')
      .append(file)
      .toString();
  }

  /**
   * Reads the project relative file into a string which is then returned.
   *
   * @param project The name of the project the file belongs to.
   * @param file The project relative file path.
   * @return The file contents as a string.
   */
  public static String fileToString(String project, String file)
  {
    String path = resolveFile(project, file);
    FileInputStream fin = null;
    try{
      fin = new FileInputStream(path);
      return IOUtils.toString(fin);
    }catch(Exception e){
      throw new RuntimeException(e);
    }finally{
      IOUtils.closeQuietly(fin);
    }
  }
}
