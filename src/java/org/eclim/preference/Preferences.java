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

import java.io.IOException;
import java.io.StringWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import org.eclim.Services;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.IScopeContext;

import org.osgi.service.prefs.BackingStoreException;

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

  private static final String ECLIM_PREFIX = "org.eclim";
  private static final String NODE_NAME = "org.eclim";

  private static Preferences instance = new Preferences();
  private static Map optionHandlers = new HashMap();

  private List preferences = new ArrayList();
  private List options = new ArrayList();

  private Preferences () {}

  /**
   * Gets the Preferences instance.
   *
   * @return
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
  public Map getPreferencesAsMap ()
    throws Exception
  {
    IEclipsePreferences preferences = getPreferences();
    String[] keys = preferences.keys();
    Map map = new HashMap();
    for(int ii = 0; ii < keys.length; ii++){
      map.put(keys[ii], preferences.get(keys[ii], null));
    }

    return map;
  }

  /**
   * Gets the preferences as a Map.
   *
   * @param _project The project.
   * @return The preferences in a Map.
   */
  public Map getPreferencesAsMap (IProject _project)
    throws Exception
  {
    IEclipsePreferences globalPrefs = getPreferences();
    IEclipsePreferences projectPrefs = getPreferences(_project);

    String[] keys = globalPrefs.keys();
    Map map = new HashMap();
    for(int ii = 0; ii < keys.length; ii++){
      map.put(keys[ii], globalPrefs.get(keys[ii], null));
    }

    keys = projectPrefs.keys();
    for(int ii = 0; ii < keys.length; ii++){
      map.put(keys[ii], projectPrefs.get(keys[ii], null));
    }

    return map;
  }

  /**
   * Gets all the options and preferences as a Map.
   *
   * @return The options in a Map.
   */
  public Map getOptionsAsMap ()
    throws Exception
  {
    Map allOptions = new HashMap();
    for(Iterator ii = optionHandlers.keySet().iterator(); ii.hasNext();){
      OptionHandler handler = (OptionHandler)optionHandlers.get(ii.next());
      allOptions.putAll(handler.getOptionsAsMap());
    }

    Map preferences = getPreferencesAsMap();
    for (Iterator ii = options.iterator(); ii.hasNext();){
      Option option = (Option)ii.next();
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
  public Map getOptionsAsMap (IProject _project)
    throws Exception
  {
    Map allOptions = new HashMap();
    for(Iterator ii = optionHandlers.keySet().iterator(); ii.hasNext();){
      OptionHandler handler = (OptionHandler)optionHandlers.get(ii.next());
      allOptions.putAll(handler.getOptionsAsMap(_project));
    }

    Map preferences = getPreferencesAsMap(_project);
    for (Iterator ii = options.iterator(); ii.hasNext();){
      Option option = (Option)ii.next();
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
    List results = new ArrayList();
    Map options = getOptionsAsMap();
    for(Iterator ii = options.keySet().iterator(); ii.hasNext();){
      OptionInstance option = new OptionInstance();
      String key = (String)ii.next();
      option.setName(key);
      option.setValue((String)options.get(key));
      results.add(option);
    }

    return (Option[])results.toArray(new Option[results.size()]);
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
    List results = new ArrayList();
    Map options = getOptionsAsMap(_project);
    for(Iterator ii = options.keySet().iterator(); ii.hasNext();){
      OptionInstance option = new OptionInstance();
      String key = (String)ii.next();
      option.setName(key);
      option.setValue((String)options.get(key));
      results.add(option);
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
      for(Iterator ii = optionHandlers.keySet().iterator(); ii.hasNext();){
        String key = (String)ii.next();
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
      for(Iterator ii = optionHandlers.keySet().iterator(); ii.hasNext();){
        String key = (String)ii.next();
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
    IProject project =
      ResourcesPlugin.getWorkspace().getRoot().getProject(_project);
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
    for(Iterator ii = preferences.iterator(); ii.hasNext();){
      Preference preference = (Preference)ii.next();
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
    for(Iterator ii = preferences.iterator(); ii.hasNext();){
      Preference preference = (Preference)ii.next();
      if(preference.getName().equals(_name)){
        if(preference.getPattern() == null ||
            preference.getPattern().matcher(_value).matches())
        {
          return;
        }else{
          throw new IllegalArgumentException(
              Services.getMessage("preference.invalid",
              new Object[]{_name, _value, preference.getRegex()}));
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
    for (Iterator ii = options.iterator(); ii.hasNext();){
      Option option = (Option)ii.next();
      if(option.getName().equals(_name)){
        if (option.getPattern() == null ||
            option.getPattern().matcher(_value).matches())
        {
          return;
        }else{
          throw new IllegalArgumentException(
              Services.getMessage("option.invalid",
              new Object[]{_name, _value, option.getRegex()}));
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
    preferences.add(_preference);
  }

  /**
   * Adds a preference to be made available.
   *
   * @param _option The option.
   */
  public void addOption (Option _option)
  {
    options.add(_option);
  }
}
