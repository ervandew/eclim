/**
 * Copyright (C) 2011
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
package org.eclim.plugin.sdt;

import org.eclim.Services;

import org.eclim.plugin.AbstractPluginResources;

import org.eclim.plugin.core.project.ProjectManagement;
import org.eclim.plugin.core.project.ProjectNatureFactory;

import org.eclim.plugin.sdt.project.ScalaProjectManager;

import scala.tools.eclipse.ScalaPlugin;

/**
 * Implementation of AbstractPluginResources.
 *
 * @author <author>
 */
public class PluginResources
  extends AbstractPluginResources
{
  /**
   * Name that can be used to lookup this PluginResources from
   * {@link Services#getPluginResources(String)}.
   */
  public static final String NAME = "org.eclim.sdt";

  public static final String NATURE = ScalaPlugin.plugin().natureId();

  /**
   * {@inheritDoc}
   * @see AbstractPluginResources#initialize(String)
   */
  @Override
  public void initialize(String name)
  {
    super.initialize(name);

    //System.setProperty(ScalaPlugin.HEADLESS_TEST); // private property
    System.setProperty("sdtcore.headless", "true");

    ProjectNatureFactory.addNature("scala", NATURE);
    ProjectManagement.addProjectManager(NATURE, new ScalaProjectManager());
  }

  /**
   * {@inheritDoc}
   * @see AbstractPluginResources#getBundleBaseName()
   */
  protected String getBundleBaseName()
  {
    return "org/eclim/plugin/sdt/messages";
  }
}
