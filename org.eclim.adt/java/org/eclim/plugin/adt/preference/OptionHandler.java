/**
 * Copyright (C) 2012 - 2017  Eric Van Dewoestine
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
package org.eclim.plugin.adt.preference;

import java.io.File;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;

import com.android.ide.eclipse.adt.AdtConstants;

import com.android.ide.eclipse.adt.internal.preferences.AdtPrefs;

/**
 * Option handler for android (adt) options.
 *
 * @author Eric Van Dewoestine
 */
public class OptionHandler
  implements org.eclim.plugin.core.preference.OptionHandler
{
  @Override
  public String getNature()
  {
    return AdtConstants.NATURE_DEFAULT;
  }

  @Override
  public Map<String,String> getValues()
  {
    Map<String,String> values = new HashMap<String,String>();
    values.put(AdtPrefs.PREFS_SDK_DIR, AdtPrefs.getPrefs().getOsSdkFolder());
    return values;
  }

  @Override
  public Map<String,String> getValues(IProject project)
  {
    return getValues();
  }

  @Override
  public void setOption(String name, String value)
  {
    AdtPrefs prefs = AdtPrefs.getPrefs();
    if (AdtPrefs.PREFS_SDK_DIR.equals(name)){
      prefs.setSdkLocation(new File(value));
    }

    // force adt prefs to reload.
    // When setting the sdk dir, File.getPath doesn't include the trailing slash
    // and AdtPrefs.setSdkLocation will not add it if necessary. Apparently the
    // gui relies on a property change event to handle that, so we'll mimic that
    // by calling loadValue here to handle that edge case and any others handled
    // in loadValues.
    prefs.loadValues(null);
  }

  @Override
  public void setOption(IProject project, String name, String value)
  {
    setOption(name, value);
  }
}
