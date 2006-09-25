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
package org.eclim.plugin.jdt.command.complete;

import org.apache.commons.lang.StringUtils;

/**
 * Represents a proposed java code completion result.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
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
      int _type,
      String _completion,
      String _description,
      String _shortDescription,
      int _replaceStart,
      int _replaceEnd)
  {
    super(_completion, _description, _shortDescription);

    setDescription(StringUtils.replace(_description, "\n", "<br/>"));

    type = _type;
    replaceStart = _replaceStart;
    replaceEnd = _replaceEnd;
  }

  /**
   * Get completion element type..
   *
   * @return The completion element type.
   */
  public int getType ()
  {
    return this.type;
  }

  /**
   * Get replaceStart.
   *
   * @return replaceStart as int.
   */
  public int getReplaceStart ()
  {
    return this.replaceStart;
  }

  /**
   * Get replaceEnd.
   *
   * @return replaceEnd as int.
   */
  public int getReplaceEnd ()
  {
    return this.replaceEnd;
  }
}
