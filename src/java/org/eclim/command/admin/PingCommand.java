/**
 * Copyright (c) 2005 - 2008
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclim.command.admin;

import org.eclim.Services;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;

import org.eclim.plugin.PluginResources;

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
      version = resources.getProperty("pluginName") + ' ' +
        resources.getProperty("pluginVersion");
    }

    return version;
  }
}
