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

import org.eclim.Services;

import org.eclim.plugin.jdt.JavaUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.IScopeContext;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import org.osgi.service.prefs.BackingStoreException;

/**
 * Class for handling preferences for eclim.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class Preferences
{
  private static final String ECLIM_PREFIX = "org.eclim";

  private Preference[] preferences;
  private Option[] options;
  private String nodeName;

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
    Map allOptions = JavaCore.getOptions();
    Map preferences = getPreferencesAsMap();

    for(int ii = 0; ii < options.length; ii++){
      preferences.put(
          options[ii].getName(), allOptions.get(options[ii].getName()));
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
    IJavaProject javaProject = JavaCore.create(_project);
    if(!javaProject.exists()){
      throw new IllegalArgumentException(Services.getMessage(
            "project.not.found", _project.getName()));
    }

    Map allOptions = javaProject.getOptions(true);
    Map preferences = getPreferencesAsMap(javaProject.getProject());

    for(int ii = 0; ii < options.length; ii++){
      preferences.put(
          options[ii].getName(), allOptions.get(options[ii].getName()));
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
      Map options = JavaCore.getOptions();

      validateOption(_name, _value);
      if(_name.equals(JavaCore.COMPILER_SOURCE)){
        JavaUtils.setCompilerSourceCompliance((String)_value);
      }else{
        options.put(_name, _value);
        JavaCore.setOptions((Hashtable)options);
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
      IJavaProject javaProject = JavaCore.create(_project);
      if(!javaProject.exists()){
        throw new IllegalArgumentException(
            Services.getMessage("project.not.found", _project.getName()));
      }
      Map global = javaProject.getOptions(true);
      Map options = javaProject.getOptions(false);

      validateOption(_name, _value);
      Object current = global.get(_name);
      if(current == null || !current.equals(_value)){
        if(_name.equals(JavaCore.COMPILER_SOURCE)){
          JavaUtils.setCompilerSourceCompliance(javaProject, (String)_value);
        }else{
          options.put(_name, _value);
          javaProject.setOptions(options);
        }
      }
    }
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
    IEclipsePreferences preferences = context.getNode(nodeName);

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
    IEclipsePreferences preferences = context.getNode(nodeName);

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
    for(int ii = 0; ii < preferences.length; ii++){
      if(_preferences.get(preferences[ii].getName(), null) == null){
        _preferences.put(preferences[ii].getName(),
            preferences[ii].getDefaultValue());
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
    for(int ii = 0; ii < preferences.length; ii++){
      if(preferences[ii].getName().equals(_name)){
        if(preferences[ii].getPattern() == null ||
            preferences[ii].getPattern().matcher(_value).matches())
        {
          return;
        }else{
          throw new IllegalArgumentException(
              Services.getMessage("preference.invalid",
              new Object[]{_name, _value, preferences[ii].getRegex()}));
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
    for(int ii = 0; ii < options.length; ii++){
      if(options[ii].getName().equals(_name)){
        if(options[ii].getPattern().matcher(_value).matches()){
          return;
        }else{
          throw new IllegalArgumentException(
              Services.getMessage("option.invalid",
              new Object[]{_name, _value, options[ii].getRegex()}));
        }
      }
    }
    throw new IllegalArgumentException(
        Services.getMessage("option.not.found", _name));
  }

  /**
   * Sets the name of the node to retrieve preferences for.
   * <p/>
   * Dependecy injection.
   *
   * @param _nodeName the value to set.
   */
  public void setNodeName (String _nodeName)
  {
    this.nodeName = _nodeName;
  }

  /**
   * Set available preferences.
   * <p/>
   * Dependecy injection.
   *
   * @param _preferences The preferences.
   */
  public void setPreferences (Preference[] _preferences)
  {
    this.preferences = _preferences;
  }

  /**
   * Set available options.
   * <p/>
   * Dependecy injection.
   *
   * @param _options The options.
   */
  public void setOptions (Option[] _options)
  {
    this.options = _options;
  }
}
