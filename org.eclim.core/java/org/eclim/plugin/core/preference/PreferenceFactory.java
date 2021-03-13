/**
 * Copyright (C) 2005 - 2021  Eric Van Dewoestine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.eclim.plugin.core.preference;

import java.util.HashMap;
import java.util.Map;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * Factory used to create Preference and Option instances (typically via spring)
 * that are then added to the Preferences instance as available preferences /
 * options.
 *
 * @author Eric Van Dewoestine
 */
public class PreferenceFactory
{
  private static final Pattern JSON_ARRAY =
    Pattern.compile("^JSON\\[(.*)\\]$");
  private static final Pattern JSON_OBJECT =
    Pattern.compile("^JSON\\{\\}$");

  /**
   * Adds options via the supplied options string that contains new line
   * separated options in the form <code>name defaultValue regex</code>.
   *
   * @param nature The project nature the options belong to.
   * @param optionsString The options string.
   *
   * @return The Preferences instance the options were added to.
   */
  public static Preferences addOptions(String nature, String optionsString)
  {
    Preferences preferences = Preferences.getInstance();
    String[] strings = StringUtils.split(optionsString, '\n');
    for(int ii = 0; ii < strings.length; ii++){
      if(strings[ii].trim().length() > 0){
        String[] attrs = parseOptionAttributes(strings[ii]);
        Option option = new Option();
        option.setNature(nature);
        option.setPath(attrs[0]);
        option.setName(attrs[1]);
        if (attrs[2] != null && !attrs[2].trim().equals(StringUtils.EMPTY)){
          option.setValidator(new RegexValidator(attrs[2]));
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
   * @param nature The project nature the preferences belong to.
   * @param preferencesString The preferences string.
   *
   * @return The Preferences instance the preferences were added to.
   */
  public static Preferences addPreferences(
      String nature, String preferencesString)
  {
    Preferences preferences = Preferences.getInstance();
    String[] strings = StringUtils.split(preferencesString, '\n');
    for(int ii = 0; ii < strings.length; ii++){
      if(strings[ii].trim().length() > 0){
        String[] attrs = parsePreferenceAttributes(strings[ii]);
        Preference preference = new Preference();
        preference.setNature(nature);
        preference.setPath(attrs[0]);
        preference.setName(attrs[1]);
        String defaultValue = attrs[2];
        if (attrs[3] != null && !attrs[3].trim().equals(StringUtils.EMPTY)){
          Matcher jsonArrayMatcher = JSON_ARRAY.matcher(attrs[3]);
          Matcher jsonObjectMatcher = JSON_OBJECT.matcher(attrs[3]);
          if (jsonArrayMatcher.matches()){
            String pattern = jsonArrayMatcher.group(1);
            preference.setValidator(new JsonValidator(
                  String[].class,
                  pattern.length() != 0 ? new RegexValidator(pattern) : null));

            // escape chars in json require additional escaping at the storage
            // level, but don't force plugin writers to deal with this burden
            defaultValue = defaultValue.replace("\\", "\\\\");
          }else if (jsonObjectMatcher.matches()){
            Map<String, String> map = new HashMap<String, String>();
            preference.setValidator(new JsonValidator(map.getClass(), null));
          }else{
            preference.setValidator(new RegexValidator(attrs[3]));
          }
        }
        preference.setDefaultValue(defaultValue);

        preferences.addPreference(preference);
      }
    }

    return preferences;
  }

  /**
   * Breaks the supplied attribute string into an array of attributes.
   * <ul>
   *   <li>index 0: name</li>
   *   <li>index 1: default value</li>
   *   <li>index 2: validation regex</li>
   * </ul>
   *
   * @param attrString The attributes string.
   * @return Array of attributes.
   */
  private static String[] parsePreferenceAttributes(String attrString)
  {
    attrString = attrString.trim();

    String[] attrs = new String[4];

    int index = attrString.indexOf(' ');
    attrs[0] = attrString.substring(0, index);

    attrString = attrString.substring(index + 1);
    index = attrString.indexOf(' ');
    if(index == -1){
      attrs[1] = attrString;
    }else{
      attrs[1] = attrString.substring(0, index);

      attrString = attrString.substring(index + 1);

      index = attrString.indexOf(' ');
      if(index != -1){
        attrs[2] = attrString.substring(0, index);
        attrs[3] = attrString.substring(index + 1);
      }else{
        attrs[2] = attrString;
      }
    }

    return attrs;
  }

  /**
   * Breaks the supplied attribute string into an array of attributes.
   * <ul>
   *   <li>index 0: name</li>
   *   <li>index 1: validation regex</li>
   * </ul>
   *
   * @param attrString The attributes string.
   * @return Array of attributes.
   */
  private static String[] parseOptionAttributes(String attrString)
  {
    attrString = attrString.trim();

    String[] attrs = new String[3];

    int index = attrString.indexOf(' ');
    attrs[0] = attrString.substring(0, index);

    attrString = attrString.substring(index + 1);
    index = attrString.indexOf(' ');
    if(index != -1){
      attrs[1] = attrString.substring(0, index);
      attrs[2] = attrString.substring(index + 1);
    }else{
      attrs[1] = attrString;
    }

    return attrs;
  }
}
