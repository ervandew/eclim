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
package org.eclim.command.taglist;

import org.apache.commons.lang.StringUtils;

/**
 * Represents a result from processing a file for tags.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class TagResult
  implements Comparable<TagResult>
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

    if(pattern != null){
      // escape newlines, '\' and '/'
      pattern = StringUtils.replace(pattern, "\\", "\\\\");
      pattern = StringUtils.replace(pattern, "/", "\\/");
      pattern = StringUtils.replace(pattern, "\n", "\\n");
      // remove ctrl-Ms
      pattern = StringUtils.replace(pattern, "\r", "");
    }
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

  /**
   * {@inheritDoc}
   * @see Comparable#compareTo(Object)
   */
  public int compareTo (TagResult obj)
  {
    if(obj == this){
      return 0;
    }
    return this.getName().compareTo(obj.getName());
  }
}
