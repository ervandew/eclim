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
package org.eclim.preference;

/**
 * Represents a category for grouping options.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class Category
{
  private String name;
  private String description;

  /**
   * Gets the category name.
   *
   * @return The name.
   */
  public String getName ()
  {
    return name;
  }

  /**
   * Sets the category name.
   *
   * @param _name The name.
   */
  public void setName (String _name)
  {
    name = _name;
  }

  /**
   * Gets the category description.
   *
   * @return The description.
   */
  public String getDescription ()
  {
    return description;
  }

  /**
   * Sets the category description.
   *
   * @param _description The description.
   */
  public void setDescription (String _description)
  {
    description = _description;
  }
}
