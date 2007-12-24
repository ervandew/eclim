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
package org.eclim.plugin.pdt.preference;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;

import org.eclipse.jface.preference.IPersistentPreferenceStore;

import org.eclipse.php.internal.core.PHPCoreConstants;
import org.eclipse.php.internal.core.PHPCorePlugin;

/**
 * Option handler for pdt/php options.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class OptionHandler
  implements org.eclim.preference.OptionHandler
{
  private static final String NATURE = "org.eclipse.php.core.PHPNature";
  private static final String PREFIX = PHPCorePlugin.ID + ".";
  private static final String VERSION = PREFIX + "phpVersion";

  private IPersistentPreferenceStore store;
  private Map<String,String> options;

  /**
   * {@inheritDoc}
   * @see org.eclim.preference.OptionHandler#getNature()
   */
  public String getNature ()
  {
    return NATURE;
  }

  /**
   * {@inheritDoc}
   */
  public Map<String,String> getOptionsAsMap ()
    throws Exception
  {
    if(options == null){
      options = new HashMap<String,String>();
      options.put(VERSION,
          getPreferences().getString(
            PHPCoreConstants.PHP_OPTIONS_PHP_VERSION)
      );
    }
    return options;
  }

  /**
   * {@inheritDoc}
   */
  public Map<String,String> getOptionsAsMap (IProject _project)
    throws Exception
  {
    IEclipsePreferences preferences = getPreferences(_project);
    Map<String,String> map = getOptionsAsMap();
    for(String key : preferences.keys()){
      map.put(PREFIX + key, preferences.get(key, null));
    }

    return map;
  }

  /**
   * {@inheritDoc}
   */
  public void setOption (String _name, String _value)
    throws Exception
  {
    if(VERSION.equals(_name)){
      getOptionsAsMap().put(VERSION, _value);
      IPersistentPreferenceStore store = getPreferences();
      store.setValue(_name.substring(PREFIX.length()), _value);
      store.save();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void setOption (IProject _project, String _name, String _value)
    throws Exception
  {
    IEclipsePreferences preferences = getPreferences(_project);

    String name = _name;
    if(name.startsWith(PREFIX)){
      name = name.substring(PREFIX.length());
    }
    preferences.put(name, _value);
    preferences.flush();
  }

  private IPersistentPreferenceStore getPreferences ()
  {
    if (store == null){
      store = (IPersistentPreferenceStore)
        PHPCorePlugin.getDefault().getPreferenceStore();
    }
    return store;
  }

  private IEclipsePreferences getPreferences (IProject _project)
  {
    IScopeContext context = new ProjectScope(_project);
    IEclipsePreferences preferences = context.getNode(PHPCorePlugin.ID);
    return preferences;
  }
}
