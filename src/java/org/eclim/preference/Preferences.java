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
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
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
  private static HashMap<String,OptionHandler> optionHandlers =
    new HashMap<String,OptionHandler>();

  private HashMap<String,Preference> preferences = new HashMap<String,Preference>();
  private HashMap<String,Option> options = new HashMap<String,Option>();

  private Preferences () {}

  /**
   * Gets the Preferences instance.
   *
   * @return The Preferences singleton.
   */
  public static Preferences getInstance ()
  {
    return instance;
  }

  /**
   * Gets the preferences as a Map.
   *
   * @return The preferences in a Map.
   */
  public Map<String,String> getPreferencesAsMap ()
    throws Exception
  {
    IEclipsePreferences preferences = getPreferences();
    HashMap<String,String> map = new HashMap<String,String>();
    for(String key : preferences.keys()){
      map.put(key, preferences.get(key, null));
    }

    return map;
  }

  /**
   * Gets the preferences as a Map.
   *
   * @param _project The project.
   * @return The preferences in a Map.
   */
  public Map<String,String> getPreferencesAsMap (IProject _project)
    throws Exception
  {
    IEclipsePreferences globalPrefs = getPreferences();
    IEclipsePreferences projectPrefs = getPreferences(_project);

    String[] keys = globalPrefs.keys();
    HashMap<String,String> map = new HashMap<String,String>();
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
  public Map<String,String> getOptionsAsMap ()
    throws Exception
  {
    HashMap<String,String> allOptions = new HashMap<String,String>();
    for(OptionHandler handler : optionHandlers.values()){
      Map<String,String> options = handler.getOptionsAsMap();
      if (options != null){
        allOptions.putAll(options);
      }
    }

    Map<String,String> preferences = getPreferencesAsMap();
    for (Option option : options.values()){
      preferences.put(option.getName(), allOptions.get(option.getName()));
    }

    return preferences;
  }

  /**
   * Gets all the options and preferences as a Map.
   *
   * @param _project The project.
   * @return The options in a Map.
   */
  public Map<String,String> getOptionsAsMap (IProject _project)
    throws Exception
  {
    HashMap<String,String> allOptions = new HashMap<String,String>();
    for(OptionHandler handler : optionHandlers.values()){
      String nature = handler.getNature();
      if(CORE.equals(nature) || _project.getNature(nature) != null){
        allOptions.putAll(handler.getOptionsAsMap(_project));
      }
    }

    Map<String,String> preferences = getPreferencesAsMap(_project);
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
  public Option[] getOptions ()
    throws Exception
  {
    return getOptions(null);
  }

  /**
   * Gets all the options and preferencs for the supplied project.
   *
   * @param _project The project.
   * @return The options.
   */
  public Option[] getOptions (IProject _project)
    throws Exception
  {
    ArrayList<OptionInstance> results = new ArrayList<OptionInstance>();
    Map<String,String> options = _project == null ?
      getOptionsAsMap() : getOptionsAsMap(_project);
    for(Object key : options.keySet()){
      String value = (String)options.get(key);
      Option option = (Option)this.options.get(key);
      if(option == null){
        option = (Option)this.preferences.get(key);
      }

      if(option != null && value != null){
        String nature = option.getNature();
        if (CORE.equals(nature) ||
            _project == null ||
            _project.getNature(nature) != null)
        {
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
   * @param _name The preference name.
   * @param _value The preference value.
   */
  public void setPreference (String _name, String _value)
    throws Exception
  {
    IEclipsePreferences preferences = getPreferences();
    validatePreference(_name, _value);
    preferences.put(_name, _value);
    preferences.flush();
  }

  /**
   * Sets the supplied preference for the specified project.
   *
   * @param _project The project.
   * @param _name The preference name.
   * @param _value The preference value.
   */
  public void setPreference (IProject _project, String _name, String _value)
    throws Exception
  {
    IEclipsePreferences global = getPreferences();
    IEclipsePreferences preferences = getPreferences(_project);

    // if project value is the same as the global, then remove it.
    if(_value.equals(global.get(_name, null))){
      removePreference(_project, _name);

    // if project value differs from global, then persist it.
    }else{
      validatePreference(_name, _value);
      preferences.put(_name, _value);
      preferences.flush();
    }
  }

  /**
   * Removes the supplied preference from the specified project.
   *
   * @param _project The project.
   * @param _name The preference name.
   */
  public void removePreference (IProject _project, String _name)
    throws Exception
  {
    IEclipsePreferences preferences = getPreferences(_project);
    preferences.remove(_name);
    preferences.flush();
  }

  /**
   * Sets the supplied option.
   *
   * @param _name The preference name.
   * @param _value The preference value.
   */
  public void setOption (String _name, String _value)
    throws Exception
  {
    if(_name.startsWith(ECLIM_PREFIX)){
      setPreference(_name, _value);
    }else{
      validateOption(_name, _value);

      OptionHandler handler = null;
      for(Object k : optionHandlers.keySet()){
        String key = (String)k;
        if(_name.startsWith(key)){
          handler = (OptionHandler)optionHandlers.get(key);
          break;
        }
      }

      if(handler != null){
        handler.setOption(_name, _value);
      }else{
        logger.warn("No handler found for option '{}'", _name);
      }
    }
  }

  /**
   * Sets the supplied option for the specified project.
   *
   * @param _project The project.
   * @param _name The preference name.
   * @param _value The preference value.
   */
  public void setOption (IProject _project, String _name, String _value)
    throws Exception
  {
    if(_name.startsWith(ECLIM_PREFIX)){
      setPreference(_project.getProject(), _name, _value);
    }else{
      validateOption(_name, _value);

      OptionHandler handler = null;
      for(Object k : optionHandlers.keySet()){
        String key = (String)k;
        if(_name.startsWith(key)){
          handler = (OptionHandler)optionHandlers.get(key);
          break;
        }
      }

      if(handler != null){
        handler.setOption(_project, _name, _value);
      }else{
        logger.warn("No handler found for option '{}'", _name);
      }
    }
  }

  /**
   * Gets the supplied preference for the specified project.
   *
   * @param _project The project.
   * @param _name The preference name.
   */
  public String getPreference (String _project, String _name)
    throws Exception
  {
    IProject project = ProjectUtils.getProject(_project, true);
    return getPreference(project, _name);
  }

  /**
   * Gets the supplied preference for the specified project.
   *
   * @param _project The project.
   * @param _name The preference name.
   */
  public String getPreference (IProject _project, String _name)
    throws Exception
  {
    return (String)getPreferencesAsMap(_project).get(_name);
  }

  /**
   * Get the preferences instance.
   *
   * @return The preferences.
   */
  protected IEclipsePreferences getPreferences ()
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
   * @param _project The project.
   * @return The preferences.
   */
  protected IEclipsePreferences getPreferences (IProject _project)
    throws Exception
  {
    IScopeContext context = new ProjectScope(_project);
    IEclipsePreferences preferences = context.getNode(NODE_NAME);

    return preferences;
  }

  /**
   * Initializes the default preferences.
   *
   * @param _preferences The eclipse preferences.
   */
  protected void initializeDefaultPreferences (IEclipsePreferences _preferences)
    throws Exception
  {
    for(Preference preference : preferences.values()){
      if(_preferences.get(preference.getName(), null) == null){
        _preferences.put(preference.getName(), preference.getDefaultValue());
      }
    }
    _preferences.flush();
  }

  /**
   * Validates that the supplied value is valid for the specified preference.
   *
   * @param _name The name of the preference.
   * @param _value The value of the preference.
   */
  public void validatePreference (String _name, String _value)
    throws Exception
  {
    Preference preference = (Preference)preferences.get(_name);
    if(preference != null){
      if(preference.getName().equals(_name)){
        if(preference.getPattern() == null ||
            preference.getPattern().matcher(_value).matches())
        {
          return;
        }else{
          throw new IllegalArgumentException(
              Services.getMessage("preference.invalid",
                _name, _value, preference.getRegex()));
        }
      }
    }
    throw new IllegalArgumentException(
        Services.getMessage("preference.not.found", _name));
  }

  /**
   * Validates that the supplied value is valid for the specified option.
   *
   * @param _name The name of the option.
   * @param _value The value of the option.
   */
  public void validateOption (String _name, String _value)
    throws Exception
  {
    Option option = (Option)options.get(_name);
    if(option != null){
      if(option.getName().equals(_name)){
        if (option.getPattern() == null ||
            option.getPattern().matcher(_value).matches())
        {
          return;
        }else{
          throw new IllegalArgumentException(
              Services.getMessage("option.invalid",
                _name, _value, option.getRegex()));
        }
      }
    }
    throw new IllegalArgumentException(
        Services.getMessage("option.not.found", _name));
  }

  /**
   * Adds the supplied OptionHandler to manage options with
   * the specified prefix.
   *
   * @param _prefix The prefix.
   * @param _handler The OptionHandler.
   * @return The OptionHandler.
   */
  public static OptionHandler addOptionHandler (
      String _prefix, OptionHandler _handler)
  {
    optionHandlers.put(_prefix, _handler);
    return _handler;
  }

  /**
   * Adds a preference to be made available.
   *
   * @param _preference The preference to add.
   */
  public void addPreference (Preference _preference)
  {
    preferences.put(_preference.getName(), _preference);
  }

  /**
   * Adds a preference to be made available.
   *
   * @param _option The option.
   */
  public void addOption (Option _option)
  {
    options.put(_option.getName(), _option);
  }
}
