/**
 * Copyright (C) 2005 - 2017  Eric Van Dewoestine
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

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;

import org.eclipse.jface.preference.IPersistentPreferenceStore;

import org.eclipse.php.internal.core.PHPCoreConstants;

import org.eclipse.php.internal.ui.PHPUiPlugin;

import org.osgi.service.prefs.BackingStoreException;

/**
 * Option handler for pdt/php options.
 *
 * @author Eric Van Dewoestine
 */
public class OptionHandler
  implements org.eclim.plugin.core.preference.OptionHandler
{
  private static final String NATURE = "org.eclipse.php.core.PHPNature";
  private static final String PREFIX = "org.eclipse.php.core.";
  private static final String VERSION =
    PREFIX + PHPCoreConstants.PHP_OPTIONS_PHP_VERSION;

  private IPersistentPreferenceStore store;
  private Map<String, String> options;

  @Override
  public String getNature()
  {
    return NATURE;
  }

  @Override
  public Map<String, String> getValues()
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

  @Override
  public Map<String, String> getValues(IProject project)
  {
    /*IScriptProject scriptProject = DLTKCore.create(project);
    if(!scriptProject.exists()){
      throw new IllegalArgumentException(Services.getMessage(
            "project.not.found", project.getName()));
    }

    return scriptProject.getOptions(true);*/
    IEclipsePreferences preferences = getPreferences(project);
    Map<String, String> map = getValues();
    try{
      for(String key : preferences.keys()){
        map.put(PREFIX + key, preferences.get(key, null));
      }
    }catch(BackingStoreException bse){
      throw new RuntimeException(bse);
    }

    return map;
  }

  @Override
  public void setOption(String name, String value)
  {
    /*Map<String,String> options = DLTKCore.getOptions();

    if(name.equals(PHPCoreConstants.PHP_OPTIONS_PHP_VERSION)){
      // not supported accross projects?
    }else{
      options.put(name, value);
      DLTKCore.setOptions((Hashtable)options);
    }*/
    if(VERSION.equals(name)){
      getValues().put(VERSION, value);
      IPersistentPreferenceStore store = getPreferences();
      store.setValue(name.substring(PREFIX.length()), value);
      try{
        store.save();
      }catch(IOException ioe){
        throw new RuntimeException(ioe);
      }
    }
  }

  @Override
  public void setOption(IProject project, String name, String value)
  {
    try{
      IEclipsePreferences preferences = getPreferences(project);

      if(name.startsWith(PREFIX)){
        name = name.substring(PREFIX.length());
      }
      preferences.put(name, value);
      preferences.flush();
    }catch(BackingStoreException bse){
      throw new RuntimeException(bse);
    }
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
