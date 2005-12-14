/**
 * Copyright (c) 2004 - 2005
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
package org.eclim.command;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.Option;

/**
 * Container for the supplied command line options.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class CommandLine
{
  private Map options = new HashMap();
  private String[] unrecognized;

  /**
   * Constructs a new instance from the supplied command line.
   *
   * @param _commandLine The command line.
   */
  public CommandLine (org.apache.commons.cli.CommandLine _commandLine)
  {
    Option[] options = _commandLine.getOptions();
    for (int ii = 0; ii < options.length; ii++){
      String value = _commandLine.getOptionValue(options[ii].getOpt());
      this.options.put(options[ii].getOpt(), value);
    }
    unrecognized = _commandLine.getArgs();
  }

  /**
   * Test to see if a command line option what supplied.
   *
   * @param _name The name of theo option to test for.
   *
   * @return true if present, false otherwise.
   */
  public boolean hasOption (String _name)
  {
    return options.containsKey(_name);
  }

  /**
   * Get the value of an arg supplied with the command line option.
   *
   * @param _name The name of the option to get the arg for.
   * @return The argument supplied to the option.
   */
  public String getValue (String _name)
  {
    return (String)options.get(_name);
  }

  /**
   * Gets a command line argument as an int.
   *
   * @param _name The name of the option.
   * @return The option as an int value, or -1 if option not supplied.
   */
  public int getIntValue (String _name)
  {
    String arg = (String)options.get(_name);
    return arg != null ? Integer.parseInt(arg) : -1;
  }

  /**
   * Gets any unrecognized arguments provided by the user.
   *
   * @return Array of unrecognized args, or null if none.
   */
  public String[] getUnrecognizedArgs ()
  {
    return unrecognized;
  }
}
