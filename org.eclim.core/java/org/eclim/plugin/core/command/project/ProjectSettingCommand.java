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
package org.eclim.plugin.core.command.project;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.preference.Preferences;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclipse.core.resources.IProject;

/**
 * Command to get/set a project setting.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "project_setting",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED s setting ARG," +
    "OPTIONAL v value ARG"
)
public class ProjectSettingCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String name = commandLine.getValue(Options.PROJECT_OPTION);
    IProject project = ProjectUtils.getProject(name, true);
    String setting = commandLine.getValue(Options.SETTING_OPTION);

    Preferences preferences = getPreferences();

    if (commandLine.hasOption(Options.VALUE_OPTION)){
      String value = commandLine.getValue(Options.VALUE_OPTION);
      preferences.setValue(project, setting, value);
      return null;
    }

    return preferences.getValue(project, setting);
  }
}
