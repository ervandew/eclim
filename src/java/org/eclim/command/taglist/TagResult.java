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
package org.eclim.command.taglist;

import org.apache.commons.lang.StringUtils;

/**
 * Represents a result from processing a file for tags.
 *
 * @author Eric Van Dewoestine
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
  public String getName()
  {
    return name;
  }

  /**
   * Sets the name of the tag.
   *
   * @param name The tag name.
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * Gets the name of the processed file.
   *
   * @return The file name.
   */
  public String getFile()
  {
    return file;
  }

  /**
   * Sets the name of the processed file.
   *
   * @param file The file name.
   */
  public void setFile(String file)
  {
    this.file = file;
  }

  /**
   * Gets the pattern used to locate the tag within the file.
   *
   * @return The pattern.
   */
  public String getPattern()
  {
    return pattern;
  }

  /**
   * Sets the pattern used to locate the tag within the file.
   *
   * @param pattern The pattern.
   */
  public void setPattern(String pattern)
  {
    if(pattern != null){
      // limit pattern to one line (don't span lines)
      if (pattern.indexOf('\n') != -1){
        String[] lines = StringUtils.split(pattern, '\n');
        for (int ii = 0; ii < lines.length; ii++){
          if(lines[ii].indexOf(getName()) != -1){
            pattern = lines[ii];
            setLine(getLine() + ii);
            break;
          }
        }
      }

      // escape newlines, '\' and '/'
      pattern = StringUtils.replace(pattern, "\\", "\\\\");
      pattern = StringUtils.replace(pattern, "/", "\\/");
      // remove ctrl-Ms
      pattern = StringUtils.replace(pattern, "\r", "");
    }
    this.pattern = pattern;
  }

  /**
   * Gets the character that identifies the kind of tag.
   *
   * @return The tag kind.
   */
  public char getKind()
  {
    return kind;
  }

  /**
   * Sets the character that identifies the kind of tag.
   *
   * @param kind The tag kind.
   */
  public void setKind(char kind)
  {
    this.kind = kind;
  }

  /**
   * Gets the line number where the tag was found.
   *
   * @return The line number.
   */
  public int getLine()
  {
    return line;
  }

  /**
   * Sets the line number where the tag was found.
   *
   * @param line The line number.
   */
  public void setLine(int line)
  {
    this.line = line;
  }

  /**
   * {@inheritDoc}
   * @see Comparable#compareTo(Object)
   */
  public int compareTo(TagResult obj)
  {
    if(obj == this){
      return 0;
    }
    return this.getName().compareTo(obj.getName());
  }
}
