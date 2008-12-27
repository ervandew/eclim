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
   * @param message The error message.
   * @param filename The file containing the error.
   * @param lineNumber The line where the error occured.
   * @param columnNumber The column where the error occured.
   * @param warning true if this error is just a warning, false otherwise.
   */
  public Error (
      String message,
      String filename,
      int lineNumber,
      int columnNumber,
      boolean warning)
  {
    this(message, filename, lineNumber, columnNumber, -1, -1, warning);
  }

  /**
   * Constructs a new instance from the supplied values.
   *
   * @param message The error message.
   * @param filename The file containing the error.
   * @param lineNumber The line where the error occured.
   * @param columnNumber The column where the error occured.
   * @param endLineNumber The line where the error ends.
   * @param endColumnNumber The column where the error ends.
   * @param warning true if this error is just a warning, false otherwise.
   */
  public Error (
      String message,
      String filename,
      int lineNumber,
      int columnNumber,
      int endLineNumber,
      int endColumnNumber,
      boolean warning)
  {
    this.message = message;
    this.filename = filename;
    this.lineNumber = lineNumber > 0 ? lineNumber : 1;
    this.columnNumber = columnNumber > 0 ? columnNumber : 1;
    this.endLineNumber = endLineNumber;
    this.endColumnNumber = endColumnNumber;
    this.warning = warning;
  }

  /**
   * Gets the error message.
   *
   * @return The error message.
   */
  public String getMessage()
  {
    return message != null ? message : StringUtils.EMPTY;
  }

  /**
   * Gets the file name.
   *
   * @return The file name.
   */
  public String getFilename()
  {
    return filename;
  }

  /**
   * Gets the line number.
   *
   * @return The line number.
   */
  public int getLineNumber()
  {
    return lineNumber;
  }

  /**
   * Gets the columnNumber for this instance.
   *
   * @return The columnNumber.
   */
  public int getColumnNumber()
  {
    return this.columnNumber;
  }

  /**
   * Gets the endLineNumber for this instance.
   *
   * @return The endLineNumber.
   */
  public int getEndLineNumber()
  {
    return this.endLineNumber;
  }

  /**
   * Gets the endColumnNumber for this instance.
   *
   * @return The endColumnNumber.
   */
  public int getEndColumnNumber()
  {
    return this.endColumnNumber;
  }

  /**
   * Checks if this error is just a warning.
   *
   * @return true if a warning, false otherwise.
   */
  public boolean isWarning()
  {
    return warning;
  }

  /**
   * Determines if this object is equal to the supplied object.
   *
   * @param other The object to test equality with.
   * @return true if the objects are equal, false otherwise.
   */
  public boolean equals(Object other)
  {
    if (!(other instanceof Error)) {
      return false;
    }
    if (this == other) {
      return true;
    }
    Error error = (Error)other;
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
  public int hashCode()
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
