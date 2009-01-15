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
package org.eclim.plugin.jdt.command.complete;

import org.apache.commons.lang.StringUtils;

/**
 * Represents a proposed java code completion result.
 *
 * @author Eric Van Dewoestine
 */
public class CodeCompleteResult
  extends org.eclim.command.complete.CodeCompleteResult
{
  private int type;
  private int replaceStart;
  private int replaceEnd;

  /**
   * Constructs a new instance.
   */
  public CodeCompleteResult (
      int type,
      String completion,
      String description,
      String shortDescription,
      int replaceStart,
      int replaceEnd)
  {
    super(completion, description, shortDescription);

    setDescription(StringUtils.replace(description, "\n", "<br/>"));

    this.type = type;
    this.replaceStart = replaceStart;
    this.replaceEnd = replaceEnd;
  }

  /**
   * Get completion element type..
   *
   * @return The completion element type.
   */
  public int getType()
  {
    return this.type;
  }

  /**
   * Get replaceStart.
   *
   * @return replaceStart as int.
   */
  public int getReplaceStart()
  {
    return this.replaceStart;
  }

  /**
   * Get replaceEnd.
   *
   * @return replaceEnd as int.
   */
  public int getReplaceEnd()
  {
    return this.replaceEnd;
  }
}
