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
 * Represents an instance of an Option with a value.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class OptionInstance
  extends Option
{
  private String value;

  /**
   * Constructs a new instance.
   *
   * @param _option The option.
   * @param _value The option value.
   */
  public OptionInstance (Option _option, String _value)
  {
    this.value = _value;
    setPath(_option.getPath());
    setName(_option.getName());
  }

  /**
   * {@inheritDoc}
   */
  public void setName (String _name)
  {
    super.setName(_name);
    super.setDescription(_name);
  }

  /**
   * Get value.
   *
   * @return value as String.
   */
  public String getValue ()
  {
    return this.value;
  }

  /**
   * Set value.
   *
   * @param _value the value to set.
   */
  public void setValue (String _value)
  {
    this.value = _value;
  }
}
