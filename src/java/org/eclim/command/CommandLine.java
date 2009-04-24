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
package org.eclim.command;

import java.util.HashMap;

import org.apache.commons.cli.Option;

import org.apache.commons.lang.StringUtils;

/**
 * Container for the supplied command line options.
 *
 * @author Eric Van Dewoestine
 */
public class CommandLine
{
  private HashMap<String, Object> options = new HashMap<String, Object>();
  private Command command;
  private String[] args;
  private String[] unrecognized;

  /**
   * Constructs a new instance from the supplied command line.
   *
   * @param command The command.
   * @param commandLine The command line.
   * @param args The orginal command line args.
   */
  public CommandLine (
      Command command,
      org.apache.commons.cli.CommandLine commandLine,
      String[] args)
  {
    this.command = command;
    this.args = args;
    Option[] options = commandLine.getOptions();
    for (Option option : options){
      if (option.hasArgs()){
        this.options.put(option.getOpt(),
            commandLine.getOptionValues(option.getOpt()));
      }else{ //if(option.hasArg() || option.hasOptionalArg()){
        this.options.put(option.getOpt(),
            commandLine.getOptionValue(option.getOpt()));
      }
    }
    unrecognized = commandLine.getArgs();
  }

  /**
   * The command to execute.
   *
   * @return The command.
   */
  public Command getCommand()
  {
    return this.command;
  }

  /**
   * Test to see if a command line option what supplied.
   *
   * @param name The name of theo option to test for.
   *
   * @return true if present, false otherwise.
   */
  public boolean hasOption(String name)
  {
    return options.containsKey(name);
  }

  /**
   * Get the value of an arg supplied with the command line option.
   *
   * @param name The name of the option to get the arg for.
   * @return The argument supplied to the option.
   */
  public String getValue(String name)
    throws Exception
  {
    String value = null;
    Object val = options.get(name);
    if(val != null){
      if (val.getClass().isArray()){
        value = ((String[])val)[0];
      }else{
        value = (String)val;
      }
      // decoded special characters encoded by eclim#ExecuteEclim
      value = StringUtils.replace(value, "%2A", "*");
      value = StringUtils.replace(value, "%24", "$");
      return value;
    }
    return null;
  }

  /**
   * Get the values of an arg supplied with the command line option.
   *
   * @param name The name of the option to get the arg for.
   * @return The arguments supplied to the option.
   */
  public String[] getValues(String name)
    throws Exception
  {
    String[] values = null;
    Object val = options.get(name);
    if(val != null){
      if (!val.getClass().isArray()){
        values = new String[]{(String)val};
      }else{
        values = (String[])val;
      }

      for (int ii = 0; ii < values.length; ii++){
        // decoded special characters encoded by eclim#ExecuteEclim
        values[ii] = StringUtils.replace(values[ii], "%2A", "*");
        values[ii] = StringUtils.replace(values[ii], "%24", "$");
      }

      return values;
    }
    return null;
  }

  /**
   * Gets a command line argument as an int.
   *
   * @param name The name of the option.
   * @return The option as an int value, or -1 if option not supplied.
   */
  public int getIntValue(String name)
    throws Exception
  {
    String arg = getValue(name);
    return arg != null ? Integer.parseInt(arg) : -1;
  }

  /**
   * Gets a command line argument as a long.
   *
   * @param name The name of the option.
   * @return The option as a long value, or -1 if option not supplied.
   */
  public long getLongValue(String name)
    throws Exception
  {
    String arg = getValue(name);
    return arg != null ? Long.parseLong(arg) : -1;
  }

  /**
   * Gets any unrecognized arguments provided by the user.
   *
   * @return Array of unrecognized args, or null if none.
   */
  public String[] getUnrecognizedArgs()
  {
    return unrecognized;
  }

  /**
   * Gets array of all arguments supplied.
   *
   * @return The original array of arguments.
   */
  public String[] getArgs()
  {
    return args;
  }

  /**
   * Adds another option to this command line.
   *
   * @param option The option.
   * @param value The option value.
   */
  public void addOption(String option, String value)
  {
    options.put(option, value);
  }
}
