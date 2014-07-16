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
public class DebugOptions {
  /**
   * Debugger action such as start, stop, resume.
   */
  public static final String ACTION_OPTION = "a";

  /**
   * Used to connect to remote VM.
   * The value should be of the form hostname:port or IP:port.
   */
  public static final String CONNECTION_OPTION = "c";

  /**
   * Line number on which to set breakpoint.
   */
  public static final String LINE_NUM_OPTION = "l";
}
