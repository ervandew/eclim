/**
 * Copyright (C) 2005 - 2013  Eric Van Dewoestine
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
package org.eclim.plugin.dltkruby;

import org.eclim.Services;

import org.eclim.plugin.AbstractPluginResources;

import org.eclim.plugin.core.project.ProjectManagement;
import org.eclim.plugin.core.project.ProjectNatureFactory;

import org.eclim.plugin.dltk.preference.DltkInterpreterTypeManager;

import org.eclim.plugin.dltk.util.DltkUtils;

import org.eclim.plugin.dltkruby.project.RubyProjectManager;

import org.eclipse.dltk.ruby.core.RubyNature;

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
  public static final String NAME = "org.eclim.dltkruby";

  @Override
  public void initialize(String name)
  {
    super.initialize(name);

    ProjectNatureFactory.addNature("ruby", RubyNature.NATURE_ID);
    ProjectManagement.addProjectManager(
        RubyNature.NATURE_ID, new RubyProjectManager());

    DltkUtils.addDltkNature(RubyNature.NATURE_ID);
    DltkInterpreterTypeManager.addInterpreterType(
        "ruby",
        RubyNature.NATURE_ID,
        "org.eclipse.dltk.internal.debug.ui.launcher.GenericRubyInstallType");
    DltkInterpreterTypeManager.addInterpreterType(
        "jruby",
        RubyNature.NATURE_ID,
        "org.eclipse.dltk.ruby.internal.launching.JRubyInstallType");
  }

  @Override
  protected String getBundleBaseName()
  {
    return "org/eclim/plugin/dltkruby/messages";
  }
}
