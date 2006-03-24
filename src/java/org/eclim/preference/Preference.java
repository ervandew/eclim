/**
 * Copyright (c) 2004 - 2005
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
 * Represents a preference which is similar to an option, except under non
 * eclipse node keys, new preference keys may be created.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class Preference
  extends Option
{
  private String defaultValue;

  /**
   * Get defaultValue.
   *
   * @return defaultValue as String.
   */
  public String getDefaultValue ()
  {
    return this.defaultValue != null ? this.defaultValue : "";
  }

  /**
   * Set defaultValue.
   *
   * @param _defaultValue the value to set.
   */
  public void setDefaultValue (String _defaultValue)
  {
    this.defaultValue = _defaultValue;
  }
}
