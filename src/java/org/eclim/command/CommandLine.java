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
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class CommandLine
{
  private HashMap<String, String> options = new HashMap<String, String>();
  private String[] args;
  private String[] unrecognized;

  /**
   * Constructs a new instance from the supplied command line.
   *
   * @param commandLine The command line.
   * @param args The orginal command line args.
   */
  public CommandLine (
      org.apache.commons.cli.CommandLine commandLine, String[] args)
  {
    this.args = args;
    Option[] options = commandLine.getOptions();
    for (int ii = 0; ii < options.length; ii++){
      String value = commandLine.getOptionValue(options[ii].getOpt());
      this.options.put(options[ii].getOpt(), value);
    }
    unrecognized = commandLine.getArgs();
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
    String value = (String)options.get(name);
    // decoded special characters encoded by eclim#ExecuteEclim
    value = StringUtils.replace(value, "%2A", "*");
    value = StringUtils.replace(value, "%24", "$");
    return value;
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
