/**
 * Copyright (c) 2004 - 2006
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
package org.eclim.command.taglist;

/**
 * Represents a result from processing a file for tags.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class TagResult
{
  private String name;
  private String file;
  private String pattern;
  private char kind;
  private int line;

  /**
   * Gets the name of the tag.
   *
   * @return The tag name.
   */
  public String getName ()
  {
    return name;
  }

  /**
   * Sets the name of the tag.
   *
   * @param _name The tag name.
   */
  public void setName (String _name)
  {
    name = _name;
  }

  /**
   * Gets the name of the processed file.
   *
   * @return The file name.
   */
  public String getFile ()
  {
    return file;
  }

  /**
   * Sets the name of the processed file.
   *
   * @param _file The file name.
   */
  public void setFile (String _file)
  {
    file = _file;
  }

  /**
   * Gets the pattern used to locate the tag within the file.
   *
   * @return The pattern.
   */
  public String getPattern ()
  {
    return pattern;
  }

  /**
   * Sets the pattern used to locate the tag within the file.
   *
   * @param _pattern The pattern.
   */
  public void setPattern (String _pattern)
  {
    pattern = _pattern;
  }

  /**
   * Gets the character that identifies the kind of tag.
   *
   * @return The tag kind.
   */
  public char getKind ()
  {
    return kind;
  }

  /**
   * Sets the character that identifies the kind of tag.
   *
   * @param _kind The tag kind.
   */
  public void setKind (char _kind)
  {
    kind = _kind;
  }

  /**
   * Gets the line number where the tag was found.
   *
   * @return The line number.
   */
  public int getLine ()
  {
    return line;
  }

  /**
   * Sets the line number where the tag was found.
   *
   * @param _line The line number.
   */
  public void setLine (int _line)
  {
    line = _line;
  }
}
