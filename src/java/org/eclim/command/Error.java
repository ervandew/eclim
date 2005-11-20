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

/**
 * Represents an error to be reported to the user.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class Error
{
  private String message;
  private String filename;
  private int lineNumber;
  private int columnNumber;

  /**
   * Constructs a new instance from the supplied values.
   *
   * @param _message The error message.
   * @param _filename The file containing the error.
   * @param _lineNumber The line where the error occured.
   * @param _columnNumber The column where the error occured.
   */
  public Error (
      String _message, String _filename, int _lineNumber, int _columnNumber)
  {
    this.message = _message;
    this.filename = _filename;
    this.lineNumber = _lineNumber;
    this.columnNumber = _columnNumber;
  }

  /**
   * Gets the error message.
   *
   * @return The error message.
   */
  public String getMessage ()
  {
    return message;
  }

  /**
   * Gets the file name.
   *
   * @return The file name.
   */
  public String getFilename ()
  {
    return filename;
  }

  /**
   * Gets the line number.
   *
   * @return The line number.
   */
  public int getLineNumber ()
  {
    return lineNumber;
  }

  /**
   * Gets the column number.
   *
   * @return The column number.
   */
  public int getColumnNumber ()
  {
    return columnNumber;
  }
}
