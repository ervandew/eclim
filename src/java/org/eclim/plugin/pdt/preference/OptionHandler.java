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
package org.eclim.plugin.pdt.preference;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclim.Services;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;

import org.eclipse.jface.preference.IPersistentPreferenceStore;

import org.eclipse.php.internal.core.PHPCoreConstants;

import org.eclipse.php.internal.core.project.properties.handlers.PhpVersionProjectPropertyHandler;

import org.eclipse.php.internal.ui.PHPUiPlugin;

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
  private static final String PREFIX = "org.eclipse.php.core.";
  private static final String VERSION =
    PREFIX + PHPCoreConstants.PHP_OPTIONS_PHP_VERSION;

  private IPersistentPreferenceStore store;
  private Map<String, String> options;

  /**
   * {@inheritDoc}
   * @see org.eclim.preference.OptionHandler#getNature()
   */
  public String getNature()
  {
    return NATURE;
  }

  /**
   * {@inheritDoc}
   */
  public Map<String, String> getOptionsAsMap()
    throws Exception
  {
    if(options == null){
      options = new HashMap<String, String>();
      options.put(VERSION,
          getPreferences().getString(
            PHPCoreConstants.PHP_OPTIONS_PHP_VERSION)
      );
    }
    return options;
    //return DLTKCore.getOptions();
  }

  /**
   * {@inheritDoc}
   */
  public Map<String, String> getOptionsAsMap(IProject project)
    throws Exception
  {
    /*IScriptProject scriptProject = DLTKCore.create(project);
    if(!scriptProject.exists()){
      throw new IllegalArgumentException(Services.getMessage(
            "project.not.found", project.getName()));
    }

    return scriptProject.getOptions(true);*/
    IEclipsePreferences preferences = getPreferences(project);
    Map<String, String> map = getOptionsAsMap();
    for(String key : preferences.keys()){
      map.put(PREFIX + key, preferences.get(key, null));
    }

    return map;
  }

  /**
   * {@inheritDoc}
   */
  public void setOption(String name, String value)
    throws Exception
  {
    /*Map<String,String> options = DLTKCore.getOptions();

    if(name.equals(PHPCoreConstants.PHP_OPTIONS_PHP_VERSION)){
      // not supported accross projects?
    }else{
      options.put(name, value);
      DLTKCore.setOptions((Hashtable)options);
    }*/
    if(VERSION.equals(name)){
      getOptionsAsMap().put(VERSION, value);
      IPersistentPreferenceStore store = getPreferences();
      store.setValue(name.substring(PREFIX.length()), value);
      store.save();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void setOption(IProject project, String name, String value)
    throws Exception
  {
    /*IScriptProject scriptProject = DLTKCore.create(project);
    if(!scriptProject.exists()){
      throw new IllegalArgumentException(
          Services.getMessage("project.not.found", project.getName()));
    }
    Map<String,String> global = scriptProject.getOptions(true);
    Map<String,String> options = scriptProject.getOptions(false);

    Object current = global.get(name);
    if(current == null || !current.equals(value)){
      if(name.equals(PHPCoreConstants.PHP_OPTIONS_PHP_VERSION)){
        PhpVersionProjectPropertyHandler.setVersion(value, project);
      }else{
        options.put(name, value);
        scriptProject.setOptions(options);
      }
    }*/
    IEclipsePreferences preferences = getPreferences(project);

    if(name.startsWith(PREFIX)){
      name = name.substring(PREFIX.length());
    }
    preferences.put(name, value);
    preferences.flush();
  }

  private IPersistentPreferenceStore getPreferences()
  {
    if (store == null){
      store = (IPersistentPreferenceStore)
        PHPUiPlugin.getDefault().getPreferenceStore();
    }
    return store;
  }

  private IEclipsePreferences getPreferences(IProject project)
  {
    IScopeContext context = new ProjectScope(project);
    IEclipsePreferences preferences = context.getNode("org.eclipse.php.core");
    return preferences;
  }
}
