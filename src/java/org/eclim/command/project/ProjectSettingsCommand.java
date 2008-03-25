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
package org.eclim.command.project;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.command.admin.SettingsFilter;

import org.eclim.preference.Option;

import org.eclim.util.ProjectUtils;

import org.eclipse.core.resources.IProject;

/**
 * Command to obtain project info.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class ProjectSettingsCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    String name = _commandLine.getValue(Options.PROJECT_OPTION);
    ArrayList<Option> results = new ArrayList<Option>();

    IProject project = ProjectUtils.getProject(name, true);
    String setting = _commandLine.getValue(Options.SETTING_OPTION);
    Option[] options = getPreferences().getOptions(project);

    // only retrieving the requested setting.
    if(setting != null){
      for(int ii = 0; ii < options.length; ii++){
        if(options[ii].getName().equals(setting)){
          results.add(options[ii]);
          break;
        }
      }

    // retrieve all settings.
    }else{
      results.addAll(Arrays.asList(options));
    }
   return SettingsFilter.instance.filter(_commandLine, results);
  }
}
