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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import org.eclim.Services;

import org.eclim.command.Error;

import org.eclim.logging.Logger;

import org.eclim.util.IOUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;

import org.osgi.service.prefs.BackingStoreException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;

import com.google.gson.reflect.TypeToken;

/**
 * Class for handling preferences for eclim.
 *
 * This class uses the word 'option' for a built in eclipse options (like the
 * jdt compiler source version), and the word 'preference' for eclim provided
 * key values.
 *
 * @author Eric Van Dewoestine
 */
public class Preferences
{
  private static final Logger logger = Logger.getLogger(Preferences.class);

  public static final String USERNAME_PREFERENCE = "org.eclim.user.name";
  public static final String USEREMAIL_PREFERENCE = "org.eclim.user.email";

  public static final String PROJECT_COPYRIGHT_PREFERENCE =
    "org.eclim.project.copyright";

  public static final String CORE = "core";

  private static final String NODE_NAME = "org.eclim";
  private static final String GLOBAL = "_global_";

  private static Gson gson = new GsonBuilder().setLenient().create();

  private static Preferences instance = new Preferences();
  private static Map<String, OptionHandler> optionHandlers =
    new HashMap<String, OptionHandler>();

  private Map<String, Preference> preferences = new HashMap<String, Preference>();
  private Map<String, Option> options = new HashMap<String, Option>();

  // cache preference values
  private Map<String, Map<String, String>> preferenceValues =
    new HashMap<String, Map<String, String>>();
  // cache option values
  private Map<String, Map<String, String>> optionValues =
    new HashMap<String, Map<String, String>>();

  private Preferences() {}

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
   * Adds the supplied OptionHandler to manage eclipse options with the
   * specified prefix.
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
   * Adds the supplied PreferencesOptionHandler to manage eclipse preferences.
   *
   * Note: before calling this method, all calls to
   * PreferencesOptionHandler.addSupportedPreferences must have already been
   * performed. Any new qualifiers added to the handler after adding the handler
   * here will have no affect.
   *
   * @param handler The PreferencesOptionHandler.
   * @return The PreferencesOptionHandler.
   */
  public static PreferencesOptionHandler addOptionHandler(
      PreferencesOptionHandler handler)
  {
    for (String qualifier : handler.getQualifiers()){
      optionHandlers.put(qualifier, handler);
    }
    return handler;
  }

  /**
   * Adds an eclim preference to be made available.
   *
   * @param preference The preference to add.
   */
  public void addPreference(Preference preference)
  {
    preferences.put(preference.getName(), preference);
    preferenceValues.clear();
  }

  /**
   * Gets an array of configured preference names.
   *
   * @return Array of preference names.
   */
  public String[] getPreferenceNames()
  {
    return preferences.keySet().toArray(new String[0]);
  }

  /**
   * Adds an eclipse option to be configurable via eclim.
   *
   * @param option The option.
   */
  public void addOption(Option option)
  {
    options.put(option.getName(), option);
    optionValues.clear();
  }

  /**
   * Gets an array of configured option names.
   *
   * @return Array of option names.
   */
  public String[] getOptionNames()
  {
    return options.keySet().toArray(new String[0]);
  }

  /**
   * Clear cached option/preference values.
   *
   * @param project The project.
   */
  public void clearProjectValueCache(IProject project)
  {
    preferenceValues.remove(project.getName());
    optionValues.remove(project.getName());
  }

  /**
   * Gets a map of all options/preferences.
   *
   * @return A map of key values.
   */
  public Map<String, String> getValues()
  {
    return getValues(null);
  }

  /**
   * Gets a map of all options/preferences.
   *
   * @param project The current project.
   * @return A map of key values.
   */
  public Map<String, String> getValues(IProject project)
  {
    try{
      String cacheKey = project != null ? project.getName() : GLOBAL;

      // eclim preferences
      Map<String, String> prefVals = preferenceValues.get(cacheKey);
      if(prefVals == null){
        prefVals = new HashMap<String, String>();
        preferenceValues.put(cacheKey, prefVals);

        IScopeContext context = InstanceScope.INSTANCE;

        // global
        IEclipsePreferences globalPrefs = context.getNode(NODE_NAME);
        initializeDefaultPreferences(globalPrefs);
        for(String key : globalPrefs.keys()){
          prefVals.put(key, globalPrefs.get(key, null));
        }

        // project
        if (project != null){
          context = new ProjectScope(project);
          IEclipsePreferences projectPrefs = context.getNode(NODE_NAME);
          for(String key : projectPrefs.keys()){
            prefVals.put(key, projectPrefs.get(key, null));
          }
        }
      }

      // eclipse option
      Map<String, String> optVals = optionValues.get(cacheKey);
      if(optVals == null){
        optVals = new HashMap<String, String>();
        optionValues.put(cacheKey, optVals);
        for(OptionHandler handler : optionHandlers.values()){
          String nature = handler.getNature();
          if (CORE.equals(nature) ||
              project == null ||
              project.getNature(nature) != null)
          {
            Map<String, String> ops = project == null ?
              handler.getValues() : handler.getValues(project);
            if (ops != null){
              optVals.putAll(ops);
            }
          }
        }
      }

      Map<String, String> all =
        new HashMap<String, String>(preferenceValues.size() + optionValues.size());
      all.putAll(optVals);
      all.putAll(prefVals);
      return all;
    }catch(BackingStoreException bse){
      throw new RuntimeException(bse);
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }
  }

  /**
   * Gets the value of an option/preference.
   *
   * @param name The name of the option/preference.
   * @return The value or null if not found.
   */
  public String getValue(String name)
  {
    return getValues(null).get(name);
  }

  /**
   * Gets the value of a project option/preference.
   *
   * @param project The project.
   * @param name The name of the option/preference.
   * @return The value or null if not found.
   */
  public String getValue(IProject project, String name)
  {
    return getValues(project).get(name);
  }

  /**
   * Gets the integer value of an option/preference.
   *
   * @param name The name of the option/preference.
   * @return The value or -1 if not found.
   */
  public int getIntValue(String name)
  {
    return getIntValue(null, name);
  }

  /**
   * Gets the integer value of a project option/preference.
   *
   * @param project The project.
   * @param name The name of the option/preference.
   * @return The value or -1 if not found.
   */
  public int getIntValue(IProject project, String name)
  {
    String value = getValues(project).get(name);
    return value != null ? Integer.parseInt(value) : -1;
  }

  /**
   * Gets the array value of an option/preference.
   *
   * @param name The name of the option/preference.
   * @return The possibly empty array value.
   */
  public String[] getArrayValue(String name)
  {
    return getArrayValue(null, name);
  }

  /**
   * Gets the array value of a project option/preference.
   *
   * @param project The project.
   * @param name The name of the option/preference.
   * @return The possibly empty array value.
   */
  public String[] getArrayValue(IProject project, String name)
  {
    String value = getValues(project).get(name);
    if (value != null && value.trim().length() != 0){
      return gson.fromJson(value, String[].class);
    }
    return ArrayUtils.EMPTY_STRING_ARRAY;
  }

  /**
   * Gets an array value of an option/preference as a set.
   *
   * @param name The name of the option/preference.
   * @return The possibly empty set.
   */
  public Set<String> getSetValue(String name)
  {
    return getSetValue(null, name);
  }

  /**
   * Gets an array value of a project option/preference as a set.
   *
   * @param project The project.
   * @param name The name of the option/preference.
   * @return The possibly empty set.
   */
  @SuppressWarnings("unchecked")
  public Set<String> getSetValue(IProject project, String name)
  {
    String value = getValues(project).get(name);
    if (value != null && value.trim().length() != 0){
      // more gson being overly strict on back slashes
      value = value.replace("\\", "\\\\");
      return (Set<String>)gson.fromJson(
          value, new TypeToken<Set<String>>(){}.getType());
    }
    return new HashSet<String>();
  }

  /**
   * Gets the Map value of an option/preference.
   *
   * @param name The name of the option/preference.
   * @return The Map value or null if not found
   */
  public Map<String, String> getMapValue(String name)
  {
    return getMapValue(null, name);
  }

  /**
   * Gets the Map value of a project option/preference.
   *
   * @param project The project.
   * @param name The name of the option/preference.
   * @return The Map value or and empty array if not found.
   */
  @SuppressWarnings("unchecked")
  public Map<String, String> getMapValue(IProject project, String name)
  {
    String value = getValues(project).get(name);
    if (value != null && value.trim().length() != 0){
      // more gson being overly strict on back slashes
      value = value.replace("\\", "\\\\");
      return (Map<String, String>)gson.fromJson(
          value, new TypeToken<Map<String, String>>(){}.getType());
    }
    return new HashMap<String, String>();
  }

  /**
   * Gets the global Option/Preference objects.
   *
   * @return Array of Option.
   */
  public Option[] getOptions()
  {
    return getOptions(null);
  }

  /**
   * Gets the Option/Preference objects.
   *
   * @param project The project scope or null for global.
   * @return Array of Option.
   */
  public Option[] getOptions(IProject project)
  {
    ArrayList<OptionInstance> results = new ArrayList<OptionInstance>();
    Map<String, String> options = new HashMap<String, String>();

    // global
    IScopeContext context = InstanceScope.INSTANCE;
    IEclipsePreferences globalPrefs = context.getNode(NODE_NAME);
    initializeDefaultPreferences(globalPrefs);
    try{
      for(String key : globalPrefs.keys()){
        options.put(key, globalPrefs.get(key, null));
      }
    }catch(BackingStoreException bse){
      throw new RuntimeException(bse);
    }

    // project
    if (project != null){
      context = new ProjectScope(project);
      IEclipsePreferences projectPrefs = context.getNode(NODE_NAME);
      try{
        for(String key : projectPrefs.keys()){
          options.put(key, projectPrefs.get(key, null));
        }
      }catch(BackingStoreException bse){
        throw new RuntimeException(bse);
      }
    }

    try{
      for(OptionHandler handler : optionHandlers.values()){
        String nature = handler.getNature();
        if (CORE.equals(nature) ||
            project == null ||
            project.getNature(nature) != null)
        {
          Map<String, String> ops = project == null ?
            handler.getValues() : handler.getValues(project);
          if (ops != null){
            options.putAll(ops);
          }
        }
      }

      for(String key : options.keySet()){
        String value = options.get(key);
        Option option = this.options.get(key);
        if(option == null){
          option = this.preferences.get(key);
        }

        if(option != null && value != null){
          String nature = option.getNature();
          if (CORE.equals(nature) ||
              project == null ||
              project.getNature(nature) != null)
          {
            OptionInstance instance;
            Validator validator = option.getValidator();
            if (validator != null){
              instance = validator.optionInstance(option, value);
            }else{
              instance = new OptionInstance(option, value);
            }
            results.add(instance);
          }
        }
      }
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }

    return results.toArray(new Option[results.size()]);
  }

  /**
   * Sets the supplied option/preference value.
   *
   * @param name The option/preference name.
   * @param value The option/preference value.
   */
  public void setValue(String name, String value)
  {
    setValue(null, name, value);
  }

  /**
   * Set the supplied value.
   *
   * @param project The project to set the value for or null for global.
   * @param name The name of the option/preference.
   * @param value The value of the option/preference.
   */
  public void setValue(IProject project, String name, String value)
  {
    if(name.startsWith(NODE_NAME)){
      setPreference(NODE_NAME, project, name, value);
    }else{
      validateValue(options.get(name), name, value);

      OptionHandler handler = null;
      for(Object k : optionHandlers.keySet()){
        String key = (String)k;
        if(name.startsWith(key)){
          handler = (OptionHandler)optionHandlers.get(key);
          break;
        }
      }

      if(handler != null){
        if (project == null){
          handler.setOption(name, value);
        }else{
          handler.setOption(project, name, value);
        }
        optionValues.clear();
      }else{
        logger.warn("No handler found for option '{}'", name);
      }
    }
  }

  /**
   * Sets an eclim preference value.
   *
   * @param nodeName The name of the preferences node to write the preference
   * to.
   * @param project The project to set the value for or null to set globally.
   * @param name The name of the preference.
   * @param value The value of the preference.
   */
  public void setPreference(
      String nodeName, IProject project, String name, String value)
  {
    IScopeContext context = InstanceScope.INSTANCE;

    IEclipsePreferences globalPrefs = context.getNode(nodeName);
    initializeDefaultPreferences(globalPrefs);

    Option pref = preferences.get(name);
    if (pref == null){
      pref = options.get(name);
    }

    try{
      // set global
      if (project == null){
        validateValue(pref, name, value);
        globalPrefs.put(name, value);
        globalPrefs.flush();

      }else{
        context = new ProjectScope(project);
        IEclipsePreferences projectPrefs = context.getNode(nodeName);

        // if project value is the same as the global, then remove it.
        if(value.equals(globalPrefs.get(name, null))){
          projectPrefs.remove(name);
          projectPrefs.flush();

        // if project value differs from global, then persist it.
        }else{
          validateValue(pref, name, value);
          projectPrefs.put(name, value);
          projectPrefs.flush();
        }
      }
    }catch(BackingStoreException bse){
      throw new RuntimeException(bse);
    }
    preferenceValues.clear();
  }

  /**
   * Set values using the supplied json file.
   *
   * @param file The file containing a json dict of name / value pairs.
   *
   * @return List of any errors attempting to set the values.
   *
   * @throws FileNotFoundException If the supplied file doesn't exist.
   */
  public List<Error> setValues(File file)
    throws FileNotFoundException
  {
    return setValues(null, file);
  }

  /**
   * Set values using the supplied json file.
   *
   * @param project The project to set the values for or null for global.
   * @param file The file containing a json dict of name / value pairs.
   *
   * @return List of any errors attempting to set the values.
   *
   * @throws FileNotFoundException If the supplied file doesn't exist.
   */
  public List<Error> setValues(IProject project, File file)
    throws FileNotFoundException
  {
    FileReader in = null;
    ArrayList<Error> errors = new ArrayList<Error>();
    try{
      in = new FileReader(file);
      JsonStreamParser parser = new JsonStreamParser(in);
      JsonObject obj = (JsonObject)parser.next();
      Gson gson = new Gson();

      for (Map.Entry<String, JsonElement> entry : obj.entrySet()){
        String name = entry.getKey();
        JsonElement element = entry.getValue();
        String value;
        if (element instanceof JsonArray){
          value = gson.toJson(element);
        }else{
          value = element.getAsString();
        }
        try{
          setValue(project, name, value);
        }catch(IllegalArgumentException iae){
          errors.add(new Error(iae.getMessage(), null, 0, 0));
        }
      }
    }finally{
      IOUtils.closeQuietly(in);
    }
    return errors;
  }

  /**
   * Initializes the default preferences.
   * Note: should only be run against the global preferences (not project, etc.).
   *
   * @param preferences The eclipse preferences.
   */
  private void initializeDefaultPreferences(IEclipsePreferences preferences)
  {
    String node = preferences.name();
    for(Preference preference : this.preferences.values()){
      String name = preference.getName();
      if (name.startsWith(node) && preferences.get(name, null) == null){
        preferences.put(preference.getName(), preference.getDefaultValue());
      }
    }
    try{
      preferences.flush();
    }catch(BackingStoreException bse){
      throw new RuntimeException(bse);
    }
  }

  /**
   * Validates that the supplied value is valid for the specified
   * option/preference.
   *
   * @param option The option/preference instance.
   * @param name The name of the option/preference.
   * @param value The value of the option/preference.
   */
  private void validateValue(Option option, String name, String value)
  {
    if(option != null){
      Validator validator = option.getValidator();
      if (validator == null || validator.isValid(value)){
        return;
      }

      throw new IllegalArgumentException(
          Services.getMessage("setting.invalid",
            name, validator.getMessage(name, value)));
    }
    throw new IllegalArgumentException(
        Services.getMessage("setting.not.found", name));
  }
}
