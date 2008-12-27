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
package org.eclim.command.admin;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.preference.Option;

/**
 * Command to obtain global settings.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class SettingsCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public String execute(CommandLine commandLine)
    throws Exception
  {
    ArrayList<Option> results = new ArrayList<Option>();

    String setting = commandLine.getValue(Options.SETTING_OPTION);
    Option[] options = getPreferences().getOptions();

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
   return SettingsFilter.instance.filter(commandLine, results);
  }
}
