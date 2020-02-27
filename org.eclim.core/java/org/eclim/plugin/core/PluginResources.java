/**
 * Copyright (C) 2005 - 2020  Eric Van Dewoestine
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
package org.eclim.plugin.core;

import org.eclim.Services;

import org.eclim.plugin.AbstractPluginResources;

import org.eclim.plugin.core.preference.PreferenceFactory;
import org.eclim.plugin.core.preference.Preferences;
import org.eclim.plugin.core.preference.PreferencesOptionHandler;

/**
 * Implementation of AbstractPluginResources.
 *
 * @author Eric Van Dewoestine
 */
public class PluginResources
  extends AbstractPluginResources
{
  /**
   * Name that can be used to lookup this PluginResources from
   * {@link Services#getPluginResources(String)}.
   */
  public static final String NAME = "org.eclim.core";

  @Override
  public void initialize(String name)
  {
    super.initialize(name);

    PreferenceFactory.addPreferences(Preferences.CORE,
      "General org.eclim.user.name\n" +
      "General org.eclim.user.email\n" +
      "General/Project org.eclim.project.version 1.0\n" +
      "General/Project org.eclim.project.copyright"
    );

    PreferencesOptionHandler handler = new PreferencesOptionHandler(
      Preferences.CORE, false);
    handler.addSupportedPreferences(
      "org.eclipse.recommenders.news.rcp",
      new String[]{
        "newsEnabled",
      }
    );
    Preferences.addOptionHandler(handler);
    PreferenceFactory.addOptions(Preferences.CORE,
      "General org.eclipse.recommenders.news.rcp.newsEnabled ^(true|false)"
    );
  }

  @Override
  protected String getBundleBaseName()
  {
    return "org/eclim/plugin/core/messages";
  }
}
