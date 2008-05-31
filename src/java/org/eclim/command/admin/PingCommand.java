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
package org.eclim.command.admin;

import java.util.Dictionary;

import org.apache.commons.lang.StringUtils;

import org.eclim.Services;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;

import org.eclim.plugin.PluginResources;

import org.eclipse.core.resources.ResourcesPlugin;

/**
 * Pings the server.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class PingCommand
  extends AbstractCommand
{
  private String version;

  /**
   * {@inheritDoc}
   */
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    if(version == null){
      PluginResources resources = Services.getPluginResources();
      String eclim_name = resources.getProperty("pluginName");
      String eclipse_name = "eclipse";
      int pad = Math.max(eclim_name.length(), eclipse_name.length());

      String eclim_version = StringUtils.rightPad(eclim_name, pad) + ' ' +
        resources.getProperty("pluginVersion");

      Dictionary headers = ResourcesPlugin.getPlugin().getBundle().getHeaders();
      String eclipse_version = StringUtils.rightPad(eclipse_name, pad) + ' ' +
        headers.get("Bundle-Version");

      version = eclim_version + '\n' + eclipse_version;
    }

    return version;
  }
}
