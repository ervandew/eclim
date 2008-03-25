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
package org.eclim.command;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import org.eclim.util.StringUtils;

/**
 * Represents an error to be reported to the user.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class Error
{
  private String message;
  private String filename;
  private int lineNumber;
  private int columnNumber;
  private int endLineNumber;
  private int endColumnNumber;
  private boolean warning;

  /**
   * Constructs a new instance from the supplied values.
   *
   * @param _message The error message.
   * @param _filename The file containing the error.
   * @param _lineNumber The line where the error occured.
   * @param _columnNumber The column where the error occured.
   * @param _warning true if this error is just a warning, false otherwise.
   */
  public Error (
      String _message,
      String _filename,
      int _lineNumber,
      int _columnNumber,
      boolean _warning)
  {
    this(_message, _filename, _lineNumber, _columnNumber, -1, -1, _warning);
  }

  /**
   * Constructs a new instance from the supplied values.
   *
   * @param _message The error message.
   * @param _filename The file containing the error.
   * @param _lineNumber The line where the error occured.
   * @param _columnNumber The column where the error occured.
   * @param _endLineNumber The line where the error ends.
   * @param _endColumnNumber The column where the error ends.
   * @param _warning true if this error is just a warning, false otherwise.
   */
  public Error (
      String _message,
      String _filename,
      int _lineNumber,
      int _columnNumber,
      int _endLineNumber,
      int _endColumnNumber,
      boolean _warning)
  {
    this.message = _message;
    this.filename = _filename;
    this.lineNumber = _lineNumber > 0 ? _lineNumber : 1;
    this.columnNumber = _columnNumber > 0 ? _columnNumber : 1;
    this.endLineNumber = _endLineNumber;
    this.endColumnNumber = _endColumnNumber;
    this.warning = _warning;
  }

  /**
   * Gets the error message.
   *
   * @return The error message.
   */
  public String getMessage ()
  {
    return message != null ? message : StringUtils.EMPTY;
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
   * Gets the columnNumber for this instance.
   *
   * @return The columnNumber.
   */
  public int getColumnNumber ()
  {
    return this.columnNumber;
  }

  /**
   * Gets the endLineNumber for this instance.
   *
   * @return The endLineNumber.
   */
  public int getEndLineNumber ()
  {
    return this.endLineNumber;
  }

  /**
   * Gets the endColumnNumber for this instance.
   *
   * @return The endColumnNumber.
   */
  public int getEndColumnNumber ()
  {
    return this.endColumnNumber;
  }

  /**
   * Checks if this error is just a warning.
   *
   * @return true if a warning, false otherwise.
   */
  public boolean isWarning ()
  {
    return warning;
  }

  /**
   * Determines if this object is equal to the supplied object.
   *
   * @param _other The object to test equality with.
   * @return true if the objects are equal, false otherwise.
   */
  public boolean equals (Object _other)
  {
    if (!(_other instanceof Error)) {
      return false;
    }
    if (this == _other) {
      return true;
    }
    Error error = (Error)_other;
    boolean equal = new EqualsBuilder()
      .append(getFilename(), error.getFilename())
      .append(getLineNumber(), error.getLineNumber())
      .append(getColumnNumber(), error.getColumnNumber())
      .append(getEndLineNumber(), error.getEndLineNumber())
      .append(getEndColumnNumber(), error.getEndColumnNumber())
      .append(getMessage(), error.getMessage())
      .isEquals();

    return equal;
  }

  /**
   * Gets the hash code for this object.
   *
   * @return The hash code for this object.
   */
  public int hashCode ()
  {
    return new HashCodeBuilder(17, 37)
      .append(filename)
      .append(lineNumber)
      .append(columnNumber)
      .append(endLineNumber)
      .append(endColumnNumber)
      .append(message)
      .toHashCode();
  }
}
