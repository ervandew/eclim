/**
 * Copyright (C) 2005 - 2017  Eric Van Dewoestine
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

import java.text.Collator;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * Represents a position within a file as denoted by an offset and length.
 *
 * @author Eric Van Dewoestine
 */
public class Position
  implements Comparable<Position>
{
  private String filename;
  private int offset = 0;
  private int length = 0;
  private int line = 1;
  private int column = 1;
  private String message;

  private Position (
      String filename, String message,
      int offset, int length,
      int line, int column)
  {
    this.filename = filename;
    this.message = message != null ? message : StringUtils.EMPTY;
    this.offset = offset;
    this.length = length;
    this.line = line;
    this.column = column;
  }

  /**
   * Construct a new Position given a filename, message, offset, and length.
   *
   * @param filename The file name.
   * @param message The message for the element at this postion.
   * @param offset The character offset within the file.
   * @param length The length of this position (number of characters from the
   *   offset).
   * @return The Position instance.
   */
  public static Position fromOffset(
      String filename, String message, int offset, int length)
  {
    int line = 1;
    int column = 1;
    try{
      int[] pos = FileUtils.offsetToLineColumn(filename, offset);
      if(pos != null){
        line = pos[0];
        column = pos[1];
      }
    }catch(Exception e){
      throw new RuntimeException(e);
    }
    return new Position(filename, message, offset, length, line, column);
  }

  /**
   * Construct a new Position given a filename, message, line, and column.
   *
   * @param filename The file name.
   * @param message The message for the element at this postion.
   * @param line The line number within the file.
   * @param column The column number within the file.
   * @return The Position instance.
   */
  public static Position fromLineColumn(
      String filename, String message, int line, int column)
  {
    return new Position(filename, message, 0, 0, line, column);
  }

  /**
   * Get filename.
   *
   * @return filename as String.
   */
  public String getFilename()
  {
    return this.filename;
  }

  /**
   * Get offset.
   *
   * @return offset as int.
   */
  public int getOffset()
  {
    return this.offset;
  }

  /**
   * Get length.
   *
   * @return length as int.
   */
  public int getLength()
  {
    return this.length;
  }

  /**
   * Gets the line for this instance.
   *
   * @return The line.
   */
  public int getLine()
  {
    return this.line;
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
   * Sets the message for this instance.
   *
   * @param message The message.
   */
  public void setMessage(String message)
  {
    this.message = message;
  }

  /**
   * Gets the message for this instance.
   *
   * @return The message.
   */
  public String getMessage()
  {
    return this.message;
  }

  @Override
  public boolean equals(Object obj)
  {
    if(!(obj instanceof Position)){
      return false;
    }

    if(this == obj){
      return true;
    }

    Position rhs = (Position)obj;
    return new EqualsBuilder()
      .append(filename, rhs.getFilename())
      .append(offset, rhs.getOffset())
      .append(length, rhs.getLength())
      .append(line, rhs.getLine())
      .append(column, rhs.getColumn())
      .isEquals();
  }

  @Override
  public int compareTo(Position o)
  {
    if (!getFilename().equals(o.getFilename())){
      return Collator.getInstance(Locale.US)
        .compare(getFilename(), o.getFilename());
    }
    if (getLine() != o.getLine()){
      return getLine() - o.getLine();
    }
    return getColumn() - o.getColumn();
  }
}
