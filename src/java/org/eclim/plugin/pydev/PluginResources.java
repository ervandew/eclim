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
package org.eclim.plugin.pydev;

import org.eclim.plugin.AbstractPluginResources;

import org.eclim.plugin.pydev.preference.OptionHandler;

import org.eclim.preference.PreferenceFactory;
import org.eclim.preference.Preferences;

import org.eclim.project.ProjectNatureFactory;

/**
 * Implementation of AbstractPluginResources.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class PluginResources
  extends AbstractPluginResources
{
  /**
   * Name that can be used to lookup this PluginResources from
   * {@link Services#getPluginResources(String)}.
   */
  public static final String NAME = "org.eclim.pydev";

  /**
   * {@inheritDoc}
   * @see AbstractPluginResources#initialize(String)
   */
  @Override
  public void initialize (String _name)
  {
    super.initialize(_name);

    Preferences.addOptionHandler("org.python.pydev", new OptionHandler());
    ProjectNatureFactory.addNature("python", "org.python.pydev.pythonNature");

    PreferenceFactory.addOptions("org.python.pydev.pythonNature",
      "PyDev org.python.pydev.INTERPRETER_PATH\n" +
      "PyDev org.python.pydev.PYTHON_PROJECT_VERSION python 2\\.[3-5]\n" +
      "PyDev org.python.pydev.PROJECT_SOURCE_PATH\n" +
      "PyDev org.python.pydev.PROJECT_EXTERNAL_SOURCE_PATH"
    );

    registerCommand("python_src_update",
        org.eclim.plugin.pydev.command.src.SrcUpdateCommand.class);
    registerCommand("python_complete",
        org.eclim.plugin.pydev.command.complete.CodeCompleteCommand.class);
    registerCommand("python_find_definition",
        org.eclim.plugin.pydev.command.definition.FindDefinitionCommand.class);
  }

  /**
   * {@inheritDoc}
   * @see AbstractPluginResources#getBundleBaseName()
   */
  protected String getBundleBaseName ()
  {
    return "org/eclim/plugin/pydev/messages";
  }
}
