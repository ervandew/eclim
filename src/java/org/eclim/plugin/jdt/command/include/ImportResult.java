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
package org.eclim.plugin.jdt.command.include;

/**
 * Represents an import request result.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ImportResult
{
  private String element;
  private int type;

  /**
   * Constructs a new instance.
   *
   * @param _element The resulting element.
   * @param _type The element type.
   */
  public ImportResult (String _element, int _type)
  {
    element = _element;
    type = _type;
  }

  /**
   * Get element.
   *
   * @return element as String.
   */
  public String getElement ()
  {
    return this.element;
  }

  /**
   * Get type.
   *
   * @return type as int.
   */
  public int getType ()
  {
    return this.type;
  }
}
