/**
 * Copyright (c) 2005 - 2006
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
 * Represents a position within a file.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class Position
{
  private String filename;
  private int offset;
  private int length;

  /**
   * Constructs a new instance.
   *
   * @param _filename The file name.
   * @param _offset The character offset within the file.
   * @param _length The length of this position (number of characters from the
   *   offset).
   */
  public Position (String _filename, int _offset, int _length)
  {
    filename = _filename;
    offset = _offset;
    length = _length;
  }

  /**
   * Get filename.
   *
   * @return filename as String.
   */
  public String getFilename ()
  {
    return this.filename;
  }

  /**
   * Get offset.
   *
   * @return offset as int.
   */
  public int getOffset ()
  {
    return this.offset;
  }

  /**
   * Set offset.
   *
   * @param _offset offset as int.
   */
  public void setOffset (int _offset)
  {
    this.offset = _offset;
  }

  /**
   * Get length.
   *
   * @return length as int.
   */
  public int getLength ()
  {
    return this.length;
  }

  /**
   * Set length.
   *
   * @param _length length as int.
   */
  public void setLength (int _length)
  {
    this.length = _length;
  }
}
