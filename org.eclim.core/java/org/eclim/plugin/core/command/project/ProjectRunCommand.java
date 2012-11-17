/**
 * Copyright (C) 2012 Tyler Dodge
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

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;

import org.eclim.command.CommandLine;

import org.eclim.plugin.core.command.AbstractCommand;
import org.eclim.annotation.Command;

import org.eclipse.debug.ui.DebugUITools;

import org.eclipse.debug.ui.IDebugUIConstants;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.core.DebugPlugin;

@org.eclim.annotation.Command(
    name = "project_run",
    options = "OPTIONAL l list ANY, OPTIONAL i indices ANY, OPTIONAL d debug ANY"
    )
public class ProjectRunCommand
  extends AbstractCommand
{
  private static final String FORMAT_SHOW_INDICES = "%-3d: %s\n";
  private static final String FORMAT_NO_INDICES = "%s\n";
  private static final String FORMAT_STARTED = "%s Started";
  
  private static final String ERROR_PREFIX = "Error: ";
  private static final String ERROR_INVALID_ARGS = ERROR_PREFIX + "Invalid Args";
  private static final String ERROR_NOT_IN_RANGE = ERROR_PREFIX + "Index Out of Range";
  private static final String ERROR_MULTIPLE_VAL = ERROR_PREFIX + "Multiple Launch Configurations Found";
  private static final String ERROR_NOT_FOUND    = ERROR_PREFIX + "Configuration Not Found";

  /**
   * {@inheritDoc}
   */
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations();
    String mode;
    //sets the mode for running the launc
    if (commandLine.hasOption("d")) {
      mode = IDebugUIConstants.ID_DEBUG_LAUNCH_GROUP;
    } else {
      mode = IDebugUIConstants.ID_RUN_LAUNCH_GROUP;
    }
    //Lists launchers if l is set
    if (commandLine.hasOption("l")) {
      StringBuilder builder = new StringBuilder();
      boolean showIndices = commandLine.hasOption("i");
      int index = 0;
      for (ILaunchConfiguration config:configs) {
        //shows indices of each launcher in case user wants to launch by index
        if (showIndices) {
          builder.append(String.format(FORMAT_SHOW_INDICES,index++,config.getName()));
        } else {
          builder.append(String.format(FORMAT_NO_INDICES,config.getName()));
        }
      }
      return builder.toString();
    }
    mode = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchGroup(mode).getMode();

    ILaunchConfiguration found = null;
    String[] launchNames = commandLine.getUnrecognizedArgs();

    //Only one name is allowed
    if (launchNames.length == 1) {
      String name = launchNames[0];
      try {
        //checks if given an index
        int index = Integer.parseInt(name);
        if (index < 0 || index >= configs.length) {
          return ERROR_NOT_IN_RANGE;
        }
        found = configs[index];
      } catch (NumberFormatException e) {
        //Searches for configuration instead of index
        for (ILaunchConfiguration config:configs) {
          if (config.getName().startsWith(name)) {
            //Checks if found has already been set
            if (found != null) {
              return ERROR_MULTIPLE_VAL;
            }
            found = config;
          }
        }
        if (found == null) {
          return ERROR_NOT_FOUND;
        }
      }
      //launches the found LaunchConfiguration
      DebugUITools.launch(found, mode);
      return String.format(FORMAT_STARTED, found.getName());
    } else {
      return ERROR_INVALID_ARGS;
    }
  }
}
