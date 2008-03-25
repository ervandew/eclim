/**
 * Copyright (C) 2005 - 2008  Eric Van Dewoestine
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
package org.eclim.util.file;

/**
 * File location as denoted by line and column.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class Location
{
  private String filename;
  private int line;
  private int column;
  private String message;

  /**
   * Constructs a new instance.
   *
   * @param filename The filename for this instance.
   * @param line The line for this instance.
   * @param column The column for this instance.
   */
  public Location (String filename, int line, int column)
  {
    this(filename, line, column, null);
  }

  /**
   * Constructs a new instance.
   *
   * @param filename The filename for this instance.
   * @param line The line for this instance.
   * @param column The column for this instance.
   * @param message The message for this instance.
   */
  public Location (String filename, int line, int column, String message)
  {
    this.filename = filename;
    this.line = line;
    this.column = column;
    this.message = message;
  }

  /**
   * Gets the filename for this instance.
   *
   * @return The filename.
   */
  public String getFilename ()
  {
    return this.filename;
  }

  /**
   * Gets the line for this instance.
   *
   * @return The line.
   */
  public int getLine ()
  {
    return this.line;
  }

  /**
   * Gets the column for this instance.
   *
   * @return The column.
   */
  public int getColumn ()
  {
    return this.column;
  }

  /**
   * Gets the message for this instance.
   *
   * @return The message.
   */
  public String getMessage ()
  {
    return this.message;
  }
}
