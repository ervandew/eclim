/**
 * Copyright (C) 2012 - 2020  Eric Van Dewoestine
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
import java.util.HashSet;
import java.util.Map;

import org.eclim.util.StringUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;

import org.osgi.service.prefs.BackingStoreException;

/**
 * OptionHandler which uses IEclipsePreferences as the back end.
 *
 * @author Eric Van Dewoestine
 */
public class PreferencesOptionHandler
  implements OptionHandler
{
  private String natureId;
  private boolean supportsProjectScope;
  private HashMap<String, HashSet<String>> supportedPreferences =
    new HashMap<String, HashSet<String>>();

  /**
   * Constructs a new handler for the supplied nature id with support for
   * project scoped preferences.
   *
   * @param natureId The nature id.
   */
  public PreferencesOptionHandler(String natureId)
  {
    this(natureId, true);
  }

  /**
   * Constructs a new handler for the supplied nature id with support for
   * project scoped preferences if supportsProjectScope is true, otherwise only
   * globally scoped preferences will be supported.
   *
   * @param natureId The nature id.
   * @param supportsProjectScope True to support project scoped preferences,
   * false otherwise.
   */
  public PreferencesOptionHandler(String natureId, boolean supportsProjectScope)
  {
    this.natureId = natureId;
    this.supportsProjectScope = supportsProjectScope;
  }

  /**
   * Add an array of supported preferences for the supplied qualifier.
   *
   * @param qualifier The preferences node qualifier.
   * @param names Array of preference names to add.
   */
  public void addSupportedPreferences(String qualifier, String[] names)
  {
    HashSet<String> set = supportedPreferences.get(qualifier);
    if (set == null){
      set = new HashSet<String>(names.length);
      supportedPreferences.put(qualifier, set);
    }
    for (String name : names){
      set.add(name);
    }
  }

  /**
   * Gets an array of preference qualifiers supported by this handle.
   *
   * @return Array of preference qualifiers.
   */
  public String[] getQualifiers()
  {
    return supportedPreferences.keySet().toArray(new String[0]);
  }

  @Override
  public String getNature()
  {
    return natureId;
  }

  @Override
  public Map<String, String> getValues()
  {
    return getValues(InstanceScope.INSTANCE);
  }

  @Override
  public Map<String, String> getValues(IProject project)
  {
    return getValues(supportsProjectScope ?
        new ProjectScope(project) : InstanceScope.INSTANCE);
  }

  private Map<String, String> getValues(IScopeContext scope)
  {
    Map<String, String> values = new HashMap<String, String>();

    for (String qualifier : supportedPreferences.keySet()){
      IEclipsePreferences prefs = scope.getNode(qualifier);
      IEclipsePreferences defaults = DefaultScope.INSTANCE.getNode(qualifier);
      for (String key : supportedPreferences.get(qualifier)){
        values.put(qualifier + '.' + key,
            prefs.get(key, defaults.get(key, StringUtils.EMPTY)));
      }
    }

    return values;
  }

  @Override
  public void setOption(String name, String value)
  {
    setOption(InstanceScope.INSTANCE, name, value);
  }

  @Override
  public void setOption(IProject project, String name, String value)
  {
    setOption(supportsProjectScope ?
        new ProjectScope(project) : InstanceScope.INSTANCE, name, value);
  }

  private void setOption(IScopeContext scope, String name, String value)
  {
    try{
      for (String qualifier : supportedPreferences.keySet()){
        String relName = name.replaceFirst(qualifier + '.', "");
        if (supportedPreferences.get(qualifier).contains(relName)){
          IEclipsePreferences prefs = scope.getNode(qualifier);
          prefs.put(relName, value);
          prefs.flush();
          return;
        }
      }
    }catch(BackingStoreException bse){
      throw new RuntimeException(bse);
    }
  }
}
