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
package org.eclim.plugin.core.util;

import org.eclim.util.file.FileUtils;
import org.eclim.util.file.Position;

/**
 * Utility functions for vim filters.
 *
 * @author Eric Van Dewoestine
 */
public class VimUtils
{
  public static final String DEFAULT_LINE_COL = "1 col 1";

  /**
   * Converts the given offset into a vim compatible line / column string.
   *
   * @param filename The absolute path to the file.
   * @param offset The offset in the file.
   * @return The vim compatable string.
   */
  public static String translateLineColumn(String filename, int offset)
  {
    if(offset >= 0){
      int[] pos = FileUtils.offsetToLineColumn(filename, offset);
      if(pos != null){
        return pos[0] + " col " + pos[1];
      }
    }
    return "1 col 1";
  }

  /**
   * Converts the position into a vim compatible line / column string.
   *
   * @param position The position instance.
   * @return The vim compatable string.
   */
  public static String translateLineColumn(Position position)
  {
    if(position.getOffset() != -1){
      int[] pos = FileUtils.offsetToLineColumn(
          position.getFilename(), position.getOffset());
      if(pos != null){
        return pos[0] + " col " + pos[1];
      }
    }
    return "1 col 1";
  }
}
