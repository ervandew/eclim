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
package org.eclim.util.file;

import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * Represents a position within a file as denoted by an offset and length.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class Position
{
  private String filename;
  private int offset;
  private int length;
  private String message;

  /**
   * Constructs a new instance.
   *
   * @param filename The file name.
   * @param offset The character offset within the file.
   * @param length The length of this position (number of characters from the
   *   offset).
   */
  public Position (String filename, int offset, int length)
  {
    this.filename = filename;
    this.offset = offset;
    this.length = length;
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
   * Set offset.
   *
   * @param offset offset as int.
   */
  public void setOffset(int offset)
  {
    this.offset = offset;
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
   * Set length.
   *
   * @param length length as int.
   */
  public void setLength(int length)
  {
    this.length = length;
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
   * {@inheritDoc}
   * @see Object#equals(Object)
   */
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
      .append(message, rhs.getMessage())
      .isEquals();
  }
}
