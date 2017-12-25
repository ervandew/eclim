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
package org.eclim.plugin.wst;

import org.eclim.Services;

import org.eclim.plugin.AbstractPluginResources;

import org.eclim.plugin.core.project.ProjectNatureFactory;

import org.eclipse.wst.jsdt.core.JavaScriptCore;

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
  public static final String NAME = "org.eclim.wst";

  /**
   * Constant representing the javascript nature id.
   */
  public static final String JAVASCRIPT_NATURE = JavaScriptCore.NATURE_ID;

  @Override
  public void initialize(String name)
  {
    super.initialize(name);

    ProjectNatureFactory.addNature("javascript", JAVASCRIPT_NATURE);
  }

  @Override
  protected String getBundleBaseName()
  {
    return "org/eclim/plugin/wst/messages";
  }
}
