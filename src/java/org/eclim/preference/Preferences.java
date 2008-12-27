/**
 * Copyright (C) 2005 - 2009  Eric Van Dewoestine
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
package org.eclim.preference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclim.Services;

import org.eclim.logging.Logger;

import org.eclim.util.ProjectUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.IScopeContext;

/**
 * Class for handling preferences for eclim.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class Preferences
{
  private static final Logger logger = Logger.getLogger(Preferences.class);

  public static final String USERNAME_PREFERENCE = "org.eclim.user.name";
  public static final String USEREMAIL_PREFERENCE = "org.eclim.user.email";

  public static final String PROJECT_COPYRIGHT_PREFERENCE =
    "org.eclim.project.copyright";

  private static final String ECLIM_PREFIX = "org.eclim";
  private static final String NODE_NAME = "org.eclim";
  private static final String CORE = "core";

  private static Preferences instance = new Preferences();
  private static HashMap<String, OptionHandler> optionHandlers =
    new HashMap<String, OptionHandler>();

  private HashMap<String, Preference> preferences =
    new HashMap<String, Preference>();
  private HashMap<String, Option> options = new HashMap<String, Option>();

  private Preferences () {}

  /**
   * Gets the Preferences instance.
   *
   * @return The Preferences singleton.
   */
  public static Preferences getInstance()
  {
    return instance;
  }

  /**
   * Gets the preferences as a Map.
   *
   * @return The preferences in a Map.
   */
  public Map<String, String> getPreferencesAsMap()
    throws Exception
  {
    IEclipsePreferences preferences = getPreferences();
    HashMap<String, String> map = new HashMap<String, String>();
    for(String key : preferences.keys()){
      map.put(key, preferences.get(key, null));
    }

    return map;
  }

  /**
   * Gets the preferences as a Map.
   *
   * @param project The project.
   * @return The preferences in a Map.
   */
  public Map<String, String> getPreferencesAsMap(IProject project)
    throws Exception
  {
    IEclipsePreferences globalPrefs = getPreferences();
    IEclipsePreferences projectPrefs = getPreferences(project);

    String[] keys = globalPrefs.keys();
    HashMap<String, String> map = new HashMap<String, String>();
    for(String key : keys){
      map.put(key, globalPrefs.get(key, null));
    }

    keys = projectPrefs.keys();
    for(String key : keys){
      map.put(key, projectPrefs.get(key, null));
    }

    return map;
  }

  /**
   * Gets all the options and preferences as a Map.
   *
   * @return The options in a Map.
   */
  public Map<String, String> getOptionsAsMap()
    throws Exception
  {
    HashMap<String, String> allOptions = new HashMap<String, String>();
    for(OptionHandler handler : optionHandlers.values()){
      Map<String, String> options = handler.getOptionsAsMap();
      if (options != null){
        allOptions.putAll(options);
      }
    }

    Map<String, String> preferences = getPreferencesAsMap();
    for (Option option : options.values()){
      preferences.put(option.getName(), allOptions.get(option.getName()));
    }

    return preferences;
  }

  /**
   * Gets all the options and preferences as a Map.
   *
   * @param project The project.
   * @return The options in a Map.
   */
  public Map<String, String> getOptionsAsMap(IProject project)
    throws Exception
  {
    HashMap<String, String> allOptions = new HashMap<String, String>();
    for(OptionHandler handler : optionHandlers.values()){
      String nature = handler.getNature();
      if(CORE.equals(nature) || project.getNature(nature) != null){
        allOptions.putAll(handler.getOptionsAsMap(project));
      }
    }

    Map<String, String> preferences = getPreferencesAsMap(project);
    for (Option option : options.values()){
      preferences.put(option.getName(), allOptions.get(option.getName()));
    }

    return preferences;
  }

  /**
   * Gets all the global options and preferencs.
   *
   * @return The options.
   */
  public Option[] getOptions()
    throws Exception
  {
    return getOptions(null);
  }

  /**
   * Gets all the options and preferencs for the supplied project.
   *
   * @param project The project.
   * @return The options.
   */
  public Option[] getOptions(IProject project)
    throws Exception
  {
    ArrayList<OptionInstance> results = new ArrayList<OptionInstance>();
    Map<String, String> options = project == null ?
      getOptionsAsMap() : getOptionsAsMap(project);
    for(Object key : options.keySet()){
      String value = (String)options.get(key);
      Option option = (Option)this.options.get(key);
      if(option == null){
        option = (Option)this.preferences.get(key);
      }

      if(option != null && value != null){
        String nature = option.getNature();
        if (CORE.equals(nature) ||
            project == null ||
            project.getNature(nature) != null){
          OptionInstance instance = new OptionInstance(option, value);
          results.add(instance);
        }
      }
    }

    return (Option[])results.toArray(new Option[results.size()]);
  }

  /**
   * Sets the supplied preference.
   *
   * @param name The preference name.
   * @param value The preference value.
   */
  public void setPreference(String name, String value)
    throws Exception
  {
    IEclipsePreferences preferences = getPreferences();
    validatePreference(name, value);
    preferences.put(name, value);
    preferences.flush();
  }

  /**
   * Sets the supplied preference for the specified project.
   *
   * @param project The project.
   * @param name The preference name.
   * @param value The preference value.
   */
  public void setPreference(IProject project, String name, String value)
    throws Exception
  {
    IEclipsePreferences global = getPreferences();
    IEclipsePreferences preferences = getPreferences(project);

    // if project value is the same as the global, then remove it.
    if(value.equals(global.get(name, null))){
      removePreference(project, name);

    // if project value differs from global, then persist it.
    }else{
      validatePreference(name, value);
      preferences.put(name, value);
      preferences.flush();
    }
  }

  /**
   * Removes the supplied preference from the specified project.
   *
   * @param project The project.
   * @param name The preference name.
   */
  public void removePreference(IProject project, String name)
    throws Exception
  {
    IEclipsePreferences preferences = getPreferences(project);
    preferences.remove(name);
    preferences.flush();
  }

  /**
   * Sets the supplied option.
   *
   * @param name The preference name.
   * @param value The preference value.
   */
  public void setOption(String name, String value)
    throws Exception
  {
    if(name.startsWith(ECLIM_PREFIX)){
      setPreference(name, value);
    }else{
      validateOption(name, value);

      OptionHandler handler = null;
      for(Object k : optionHandlers.keySet()){
        String key = (String)k;
        if(name.startsWith(key)){
          handler = (OptionHandler)optionHandlers.get(key);
          break;
        }
      }

      if(handler != null){
        handler.setOption(name, value);
      }else{
        logger.warn("No handler found for option '{}'", name);
      }
    }
  }

  /**
   * Sets the supplied option for the specified project.
   *
   * @param project The project.
   * @param name The preference name.
   * @param value The preference value.
   */
  public void setOption(IProject project, String name, String value)
    throws Exception
  {
    if(name.startsWith(ECLIM_PREFIX)){
      setPreference(project.getProject(), name, value);
    }else{
      validateOption(name, value);

      OptionHandler handler = null;
      for(Object k : optionHandlers.keySet()){
        String key = (String)k;
        if(name.startsWith(key)){
          handler = (OptionHandler)optionHandlers.get(key);
          break;
        }
      }

      if(handler != null){
        handler.setOption(project, name, value);
      }else{
        logger.warn("No handler found for option '{}'", name);
      }
    }
  }

  /**
   * Gets the supplied preference for the specified project.
   *
   * @param project The project.
   * @param name The preference name.
   */
  public String getPreference(String project, String name)
    throws Exception
  {
    return getPreference(ProjectUtils.getProject(project, true), name);
  }

  /**
   * Gets the supplied preference for the specified project.
   *
   * @param project The project.
   * @param name The preference name.
   */
  public String getPreference(IProject project, String name)
    throws Exception
  {
    return (String)getPreferencesAsMap(project).get(name);
  }

  /**
   * Get the preferences instance.
   *
   * @return The preferences.
   */
  protected IEclipsePreferences getPreferences()
    throws Exception
  {
    IScopeContext context = new InstanceScope();
    IEclipsePreferences preferences = context.getNode(NODE_NAME);

    initializeDefaultPreferences(preferences);

    return preferences;
  }

  /**
   * Get the preferences instance for the supplied project under eclim.
   *
   * @param project The project.
   * @return The preferences.
   */
  protected IEclipsePreferences getPreferences(IProject project)
    throws Exception
  {
    IScopeContext context = new ProjectScope(project);
    IEclipsePreferences preferences = context.getNode(NODE_NAME);

    return preferences;
  }

  /**
   * Initializes the default preferences.
   *
   * @param preferences The eclipse preferences.
   */
  protected void initializeDefaultPreferences(IEclipsePreferences preferences)
    throws Exception
  {
    for(Preference preference : this.preferences.values()){
      if(preferences.get(preference.getName(), null) == null){
        preferences.put(preference.getName(), preference.getDefaultValue());
      }
    }
    preferences.flush();
  }

  /**
   * Validates that the supplied value is valid for the specified preference.
   *
   * @param name The name of the preference.
   * @param value The value of the preference.
   */
  public void validatePreference(String name, String value)
    throws Exception
  {
    Preference preference = (Preference)preferences.get(name);
    if(preference != null){
      if(preference.getName().equals(name)){
        if (preference.getPattern() == null ||
            preference.getPattern().matcher(value).matches()){
          return;
        }else{
          throw new IllegalArgumentException(
              Services.getMessage("preference.invalid",
                name, value, preference.getRegex()));
        }
      }
    }
    throw new IllegalArgumentException(
        Services.getMessage("preference.not.found", name));
  }

  /**
   * Validates that the supplied value is valid for the specified option.
   *
   * @param name The name of the option.
   * @param value The value of the option.
   */
  public void validateOption(String name, String value)
    throws Exception
  {
    Option option = (Option)options.get(name);
    if(option != null){
      if(option.getName().equals(name)){
        if (option.getPattern() == null ||
            option.getPattern().matcher(value).matches()){
          return;
        }else{
          throw new IllegalArgumentException(
              Services.getMessage("option.invalid",
                name, value, option.getRegex()));
        }
      }
    }
    throw new IllegalArgumentException(
        Services.getMessage("option.not.found", name));
  }

  /**
   * Adds the supplied OptionHandler to manage options with
   * the specified prefix.
   *
   * @param prefix The prefix.
   * @param handler The OptionHandler.
   * @return The OptionHandler.
   */
  public static OptionHandler addOptionHandler(
      String prefix, OptionHandler handler)
  {
    optionHandlers.put(prefix, handler);
    return handler;
  }

  /**
   * Adds a preference to be made available.
   *
   * @param preference The preference to add.
   */
  public void addPreference(Preference preference)
  {
    preferences.put(preference.getName(), preference);
  }

  /**
   * Adds a preference to be made available.
   *
   * @param option The option.
   */
  public void addOption(Option option)
  {
    options.put(option.getName(), option);
  }
}
