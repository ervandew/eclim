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

import org.apache.commons.lang.StringUtils;

/**
 * Factory used to create Preference and Option instances (typically via spring)
 * that are then added to the Preferences instance as available preferences /
 * options.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class PreferenceFactory
{
  /**
   * Adds options via the supplied options string that contains new line
   * separated options in the form <code>name defaultValue regex</code>.
   *
   * @param _nature The project nature the options belong to.
   * @param _optionsString The options string.
   *
   * @return The Preferences instance the options were added to.
   */
  public static Preferences addOptions (String _nature, String _optionsString)
  {
    Preferences preferences = Preferences.getInstance();
    String[] strings = StringUtils.split(_optionsString, '\n');
    for(int ii = 0; ii < strings.length; ii++){
      if(strings[ii].trim().length() > 0){
        String[] attrs = parseOptionAttributes(strings[ii]);
        Option option = new Option();
        option.setNature(_nature);
        option.setPath(attrs[0]);
        option.setName(attrs[1]);
        if (attrs.length > 2){
          option.setRegex(attrs[2]);
        }

        preferences.addOption(option);
      }
    }

    return preferences;
  }

  /**
   * Adds preferences via the supplied preferences string that contains new line
   * separated preferences in the form <code>name defaultValue regex</code>.
   *
   * @param _nature The project nature the preferences belong to.
   * @param _preferencesString The preferences string.
   *
   * @return The Preferences instance the preferences were added to.
   */
  public static Preferences addPreferences (
      String _nature, String _preferencesString)
  {
    Preferences preferences = Preferences.getInstance();
    String[] strings = StringUtils.split(_preferencesString, '\n');
    for(int ii = 0; ii < strings.length; ii++){
      if(strings[ii].trim().length() > 0){
        String[] attrs = parsePreferenceAttributes(strings[ii]);
        Preference preference = new Preference();
        preference.setNature(_nature);
        preference.setPath(attrs[0]);
        preference.setName(attrs[1]);
        preference.setDefaultValue(attrs[2]);
        preference.setRegex(attrs[3]);

        preferences.addPreference(preference);
      }
    }

    return preferences;
  }

  /**
   * Breaks the supplied attribute string into an array of attributes.
   * <p/>
   * <ul>
   *   <li>index 0: name</li>
   *   <li>index 1: default value</li>
   *   <li>index 2: validation regex</li>
   * </ul>
   *
   * @param _attrString The attributes string.
   * @return Array of attributes.
   */
  private static String[] parsePreferenceAttributes (String _attrString)
  {
    _attrString = _attrString.trim();

    String[] attrs = new String[4];

    int index = _attrString.indexOf(' ');
    attrs[0] = _attrString.substring(0, index);

    _attrString = _attrString.substring(index + 1);
    index = _attrString.indexOf(' ');
    if(index == -1){
      attrs[1] = _attrString;
    }else{
      attrs[1] = _attrString.substring(0, index);

      _attrString = _attrString.substring(index + 1);

      index = _attrString.indexOf(' ');
      if(index != -1){
        attrs[2] = _attrString.substring(0, index);
        attrs[3] = _attrString.substring(index + 1);
      }else{
        attrs[2] = _attrString;
      }
    }

    return attrs;
  }

  /**
   * Breaks the supplied attribute string into an array of attributes.
   * <p/>
   * <ul>
   *   <li>index 0: name</li>
   *   <li>index 1: validation regex</li>
   * </ul>
   *
   * @param _attrString The attributes string.
   * @return Array of attributes.
   */
  private static String[] parseOptionAttributes (String _attrString)
  {
    _attrString = _attrString.trim();

    String[] attrs = new String[3];

    int index = _attrString.indexOf(' ');
    attrs[0] = _attrString.substring(0, index);

    _attrString = _attrString.substring(index + 1);
    index = _attrString.indexOf(' ');
    if(index != -1){
      attrs[1] = _attrString.substring(0, index);
      attrs[2] = _attrString.substring(index + 1);
    }else{
      attrs[1] = _attrString;
    }

    return attrs;
  }
}
