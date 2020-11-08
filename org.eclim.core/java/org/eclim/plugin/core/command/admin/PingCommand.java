/**
 * Copyright (C) 2005 - 2020  Eric Van Dewoestine
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
package org.eclim.plugin.core.command.admin;

import java.util.HashMap;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;

import org.eclim.plugin.PluginResources;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclipse.core.runtime.Platform;

import org.osgi.framework.Bundle;

/**
 * Command which responds to ping requests, returning the eclim and eclipse
 * version numbers.
 *
 * @author Eric Van Dewoestine
 */
@Command(name = "ping")
public class PingCommand
  extends AbstractCommand
{
  private HashMap<String, String> versions;

  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    if(versions == null){
      PluginResources resources = Services.getPluginResources("org.eclim");

      versions = new HashMap<String, String>();
      versions.put("eclim", resources.getProperty("pluginVersion"));
      versions.put("eclipse", getVersion());
    }

    return versions;
  }

  private String getVersion()
  {
    Bundle bundle = Platform.getBundle("org.eclipse.platform");
    if(bundle != null){
      String eclipse_version = (String)bundle.getHeaders().get("Bundle-Version");
      if (eclipse_version != null){
        eclipse_version = eclipse_version.replaceFirst("([0-9.]+).*", "$1");
        return eclipse_version.replaceFirst("\\.$", "");
      }
    }
    return null;
  }
}
