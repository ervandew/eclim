/**
 * Copyright (C) 2005 - 2020  Eric Van Dewoestine
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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import org.eclim.util.StringUtils;

/**
 * Represents an error to be reported to the user.
 *
 * @author Eric Van Dewoestine
 */
public class Error
{
  private String message;
  private String filename;
  private int line;
  private int column;
  private int endLine;
  private int endColumn;
  private boolean warning;

  /**
   * Constructs a new instance from the supplied values.
   *
   * @param message The error message.
   * @param filename The file containing the error.
   * @param line The line where the error occured.
   * @param column The column where the error occured.
   */
  public Error (
      String message,
      String filename,
      int line,
      int column)
  {
    this(message, filename, line, column, -1, -1, false);
  }

  /**
   * Constructs a new instance from the supplied values.
   *
   * @param message The error message.
   * @param filename The file containing the error.
   * @param line The line where the error occured.
   * @param column The column where the error occured.
   * @param warning true if this error is just a warning, false otherwise.
   */
  public Error (
      String message,
      String filename,
      int line,
      int column,
      boolean warning)
  {
    this(message, filename, line, column, -1, -1, warning);
  }

  /**
   * Constructs a new instance from the supplied values.
   *
   * @param message The error message.
   * @param filename The file containing the error.
   * @param line The line where the error occured.
   * @param column The column where the error occured.
   * @param endLine The line where the error ends.
   * @param endColumn The column where the error ends.
   * @param warning true if this error is just a warning, false otherwise.
   */
  public Error (
      String message,
      String filename,
      int line,
      int column,
      int endLine,
      int endColumn,
      boolean warning)
  {
    this.message = message != null ? message : StringUtils.EMPTY;
    this.filename = filename;
    this.line = line > 0 ? line : 1;
    this.column = column > 0 ? column : 1;
    this.endLine = endLine;
    this.endColumn = endColumn;
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
  public int getLine()
  {
    return line;
  }

  /**
   * Gets the column for this instance.
   *
   * @return The column.
   */
  public int getColumn()
  {
    return this.column;
  }

  /**
   * Gets the endLine for this instance.
   *
   * @return The endLine.
   */
  public int getEndLine()
  {
    return this.endLine;
  }

  /**
   * Gets the endColumn for this instance.
   *
   * @return The endColumn.
   */
  public int getEndColumn()
  {
    return this.endColumn;
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
      .append(getLine(), error.getLine())
      .append(getColumn(), error.getColumn())
      .append(getEndLine(), error.getEndLine())
      .append(getEndColumn(), error.getEndColumn())
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
      .append(line)
      .append(column)
      .append(endLine)
      .append(endColumn)
      .append(message)
      .toHashCode();
  }
}
