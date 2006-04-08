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
package org.eclim.command.filter;

import org.eclim.command.OutputFilter;

import org.eclim.util.file.Position;

import org.eclim.util.vim.VimUtils;

/**
 * Filter for Position.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class PositionFilter
  implements OutputFilter
{
  /**
   * {@inheritDoc}
   */
  public String filter (Object _result)
  {
    //Position position = (Position)_result;
    return "";
  }
}
