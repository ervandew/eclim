/**
 * Copyright (C) 2005 - 2008  Eric Van Dewoestine
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
 * @author Eric Van Dewoestine (ervandew@gmail.com)
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
