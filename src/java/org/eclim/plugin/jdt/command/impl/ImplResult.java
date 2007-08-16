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
package org.eclim.plugin.jdt.command.impl;

import java.util.List;

/**
 * Container for impl command result.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ImplResult
{
  private String type;
  private List<ImplType> superTypes;

  /**
   * Default constructor.
   */
  public ImplResult (String _type, List<ImplType> _superTypes)
  {
    type = _type;
    superTypes = _superTypes;
  }

  /**
   * Gets the type these results are for.
   *
   * @return The type.
   */
  public String getType ()
  {
    return type;
  }

  /**
   * Gets the super type results.
   *
   * @return The results.
   */
  public List<ImplType> getSuperTypes ()
  {
    return superTypes;
  }
}
