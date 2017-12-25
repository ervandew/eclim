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
package org.eclim.plugin.pdt;

import org.eclim.Services;

import org.eclim.logging.Logger;

import org.eclim.plugin.AbstractPluginResources;

import org.eclim.plugin.core.preference.PreferenceFactory;
import org.eclim.plugin.core.preference.Preferences;

import org.eclim.plugin.core.project.ProjectManagement;
import org.eclim.plugin.core.project.ProjectNatureFactory;

import org.eclim.plugin.pdt.preference.OptionHandler;

import org.eclim.plugin.pdt.project.PhpProjectManager;

import org.eclipse.php.internal.core.PHPCorePlugin;

import org.eclipse.php.internal.core.project.PHPNature;

/**
 * Implementation of AbstractPluginResources.
 *
 * @author Eric Van Dewoestine
 */
public class PluginResources
  extends AbstractPluginResources
{
  private static final Logger logger = Logger.getLogger(PluginResources.class);

  /**
   * Name that can be used to lookup this PluginResources from
   * {@link Services#getPluginResources(String)}.
   */
  public static final String NAME = "org.eclim.pdt";

  @Override
  public void initialize(String name)
  {
    super.initialize(name);

    ProjectNatureFactory.addNature("php", PHPNature.ID);
    ProjectManagement.addProjectManager(PHPNature.ID, new PhpProjectManager());

    Preferences.addOptionHandler("org.eclipse.php", new OptionHandler());
    PreferenceFactory.addOptions("org.eclipse.php.core.PHPNature",
      "PDT org.eclipse.php.core.phpVersion (?i)php(?:4|5|5_3)"
    );

    try{
      PHPCorePlugin.initializeAfterLoad(null);
    }catch(Exception e){
      logger.error("Failed to initialize php core plugin.", e);
    }
  }

  @Override
  protected String getBundleBaseName()
  {
    return "org/eclim/plugin/pdt/messages";
  }
}
