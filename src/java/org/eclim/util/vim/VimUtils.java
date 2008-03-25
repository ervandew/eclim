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
package org.eclim.util.vim;

import org.eclim.util.file.FileUtils;
import org.eclim.util.file.Location;
import org.eclim.util.file.Position;

/**
 * Utility functions for vim filters.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class VimUtils
{
  public static final String DEFAULT_LINE_COL = "1 col 1";

  /**
   * Converts the position into a vim compatible line / column string.
   *
   * @param _position The position instance.
   * @return The vim compatable string.
   */
  public static String translateLineColumn (Position _position)
    throws Exception
  {
    if(_position.getOffset() != -1){
      int[] position = FileUtils.offsetToLineColumn(
          _position.getFilename(), _position.getOffset());
      if(position != null){
        return position[0] + " col " + position[1];
      }
    }
    return "1 col 1";
  }

  /**
   * Converts the location into a vim compatible line / column string.
   *
   * @param _location The location instance.
   * @return The vim compatable string.
   */
  public static String translateLineColumn (Location _location)
    throws Exception
  {
    if(_location != null){
      return _location.getLine() + " col " + _location.getColumn();
    }
    return "1 col 1";
  }
}
