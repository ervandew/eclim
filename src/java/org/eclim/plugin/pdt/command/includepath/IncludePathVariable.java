/**
 * Copyright (c) 2005 - 2007
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
package org.eclim.plugin.pdt.command.includepath;

/**
 * Represents a pdt include path variable.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class IncludePathVariable
  implements Comparable<IncludePathVariable>
{
  private String name;
  private String path;

  /**
   * Get name.
   *
   * @return name as String.
   */
  public String getName ()
  {
    return this.name;
  }

  /**
   * Set name.
   *
   * @param _name the value to set.
   */
  public void setName (String _name)
  {
    this.name = _name;
  }

  /**
   * Get path.
   *
   * @return path as String.
   */
  public String getPath ()
  {
    return this.path;
  }

  /**
   * Set path.
   *
   * @param _path the value to set.
   */
  public void setPath (String _path)
  {
    this.path = _path;
  }

  /**
   * {@inheritDoc}
   * @see Comparable#compareTo(Object)
   */
  public int compareTo (IncludePathVariable obj)
  {
    if (obj == this){
      return 0;
    }

    return this.getName().compareTo(obj.getName());
  }
}
