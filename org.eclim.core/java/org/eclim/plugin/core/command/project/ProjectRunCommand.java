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

@org.eclim.annotation.Command(name = "project_run")
public class ProjectRunCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public Object execute(CommandLine commandLine)
    throws Exception
  {

    ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations();
    StringBuilder builder = new StringBuilder();
    ILaunchConfiguration found = null;
    String[] launchNames = commandLine.getUnrecognizedArgs();
    if (launchNames.length == 1) {
      String name = launchNames[0];
      for (ILaunchConfiguration config:configs) {
        if (config.getName().startsWith(name)) {
          if (found != null) {
            return "Error Multiple Matches";
          }
          found = config;
        }
      }
      if (found == null) {
        return "Error Not Found";
      }

      DebugUITools.launch(found, 
          DebugUIPlugin.getDefault().getLaunchConfigurationManager()
            .getLaunchGroup(IDebugUIConstants.ID_DEBUG_LAUNCH_GROUP).getMode());
      return found.getName() + " Started";
    } else {
      return "Error Invalid Arguments";
    }
  }
}
