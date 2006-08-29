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
package org.eclim.util.vim;

import org.eclim.util.file.FileUtils;
import org.eclim.util.file.Position;

/**
 * Utility functions for vim filters.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
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
}
