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
package org.eclim.plugin.cdt;

import org.eclim.Services;

import org.eclim.plugin.AbstractPluginResources;

import org.eclim.plugin.cdt.project.CProjectManager;

import org.eclim.project.ProjectManagement;
import org.eclim.project.ProjectNatureFactory;
import org.eclipse.cdt.core.CCProjectNature;

import org.eclipse.cdt.core.CProjectNature;

/**
 * Implementation of AbstractPluginResources.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 */
public class PluginResources
  extends AbstractPluginResources
{
  /**
   * Name that can be used to lookup this PluginResources from
   * {@link Services#getPluginResources(String)}.
   */
  public static final String NAME = "org.eclim.cdt";

  public static final String NATURE_C = CProjectNature.C_NATURE_ID;
  public static final String NATURE_CPP = CCProjectNature.CC_NATURE_ID;

  /**
   * {@inheritDoc}
   * @see AbstractPluginResources#initialize(String)
   */
  @Override
  public void initialize(String name)
  {
    super.initialize(name);

    ProjectNatureFactory.addNature("c", CProjectNature.C_NATURE_ID);
    ProjectNatureFactory.addNature("c++", CCProjectNature.CC_NATURE_ID);
    ProjectNatureFactory.addNature("cpp", CCProjectNature.CC_NATURE_ID);

    ProjectManagement.addProjectManager(
        CProjectNature.C_NATURE_ID, new CProjectManager());
    ProjectManagement.addProjectManager(
        CCProjectNature.CC_NATURE_ID, new CProjectManager());
  }

  /**
   * {@inheritDoc}
   * @see AbstractPluginResources#getBundleBaseName()
   */
  protected String getBundleBaseName()
  {
    return "org/eclim/plugin/cdt/messages";
  }
}
