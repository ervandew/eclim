/**
 * Copyright (c) 2005 - 2008
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
