/**
 * Copyright (C) 2005 - 2014  Eric Van Dewoestine
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

/**
 * Class for defining debugger related eclim command line options.
 */
public class DebugOptions
{
  /**
   * Debugger action such as start, stop, resume.
   */
  public static final String ACTION_OPTION = "a";

  /**
   * Hostname or IP of remote VM to connect.
   */
  public static final String HOST_OPTION = "h";

  /**
   * Port number of remote VM to connect.
   */
  public static final String PORT_OPTION = "p";

  /**
   * Line number on which to set breakpoint.
   */
  public static final String LINE_NUM_OPTION = "l";

  /**
   * Delete all breakpoints.
   */
  public static final String DELETE_ALL_BKPOINTS_OPTION = "d";

  /**
   * VIM server instance name.
   */
  public static final String VIM_INSTANCE_OPTION = "v";
}
