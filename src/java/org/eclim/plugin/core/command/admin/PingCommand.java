/**
 * Copyright (C) 2005 - 2011  Eric Van Dewoestine
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

import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.lang.StringUtils;

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
  private String version;

  /**
   * {@inheritDoc}
   */
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    if(version == null){
      PluginResources resources = Services.getPluginResources("org.eclim");
      String eclim_name = "eclim";
      String eclipse_name = "eclipse";
      int pad = Math.max(eclim_name.length(), eclipse_name.length());

      String eclim_version = StringUtils.rightPad(eclim_name, pad) + ' ' +
        resources.getProperty("pluginVersion");

      String eclipse_version = getVersion();
      eclipse_version = StringUtils.rightPad(
          eclipse_name, pad) + ' ' + eclipse_version;

      version = eclim_version + '\n' + eclipse_version;
    }

    return version;
  }

  private String getVersion()
  {
    // I can't find a way to get a definitive eclipse version number, so try
    // comparing the version numbers of some bundles and take the highest one as
    // the current version.
    ArrayList<String> versions = new ArrayList<String>();
    String[] names = {"org.eclipse.osgi", "org.eclipse.swt"};
    for (String name : names){
      String version = getVersion(name);
      if(version != null){
        versions.add(version);
      }
    }
    Collections.sort(versions);
    return versions.get(versions.size() - 1);
  }

  private String getVersion(String bundleName)
  {
    Bundle bundle = Platform.getBundle(bundleName);
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
