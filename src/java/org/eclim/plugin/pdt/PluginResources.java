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
package org.eclim.plugin.pdt;

import org.eclim.Services;

import org.eclim.plugin.AbstractPluginResources;

import org.eclim.plugin.pdt.preference.OptionHandler;

import org.eclim.plugin.pdt.project.PhpProjectManager;

import org.eclim.preference.PreferenceFactory;
import org.eclim.preference.Preferences;

import org.eclim.project.ProjectManagement;
import org.eclim.project.ProjectNatureFactory;

/**
 * Implementation of AbstractPluginResources.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision: 940 $
 */
public class PluginResources
  extends AbstractPluginResources
{
  /**
   * Name that can be used to lookup this PluginResources from
   * {@link Services#getPluginResources(String)}.
   */
  public static final String NAME = "org.eclim.pdt";

  /**
   * {@inheritDoc}
   * @see AbstractPluginResources#initialize(String)
   */
  @Override
  public void initialize (String _name)
  {
    super.initialize(_name);

    ProjectNatureFactory.addNature("php", "org.eclipse.php.core.PHPNature");
    ProjectManagement.addProjectManager(
        "org.eclipse.php.core.PHPNature", new PhpProjectManager());

    Preferences.addOptionHandler("org.eclipse.php", new OptionHandler());
    PreferenceFactory.addOptions("org.eclipse.php.core.PHPNature",
      "PDT org.eclipse.php.core.phpVersion php[45]"
    );

    registerCommand("php_src_update",
        org.eclim.plugin.pdt.command.src.SrcUpdateCommand.class);
    registerCommand("php_complete",
        org.eclim.plugin.pdt.command.complete.CodeCompleteCommand.class);
    registerCommand("php_find_definition",
        org.eclim.plugin.pdt.command.search.FindDefinitionCommand.class);
    registerCommand("php_search",
        org.eclim.plugin.pdt.command.search.SearchCommand.class);
    registerCommand("php_include_paths",
        org.eclim.plugin.pdt.command.includepath.IncludePathsCommand.class);
    registerCommand("php_includepath_variables",
        org.eclim.plugin.pdt.command.includepath.IncludePathVariablesCommand.class);
    registerCommand("php_includepath_variable_create",
        org.eclim.plugin.pdt.command.includepath.IncludePathVariableCreateCommand.class);
    registerCommand("php_includepath_variable_delete",
        org.eclim.plugin.pdt.command.includepath.IncludePathVariableDeleteCommand.class);
  }

  /**
   * {@inheritDoc}
   * @see AbstractPluginResources#getBundleBaseName()
   */
  protected String getBundleBaseName ()
  {
    return "org/eclim/plugin/pdt/messages";
  }
}
