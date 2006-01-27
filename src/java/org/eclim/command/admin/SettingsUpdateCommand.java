/**
 * Copyright (c) 2004 - 2005
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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.util.Iterator;
import java.util.Properties;

import org.eclim.Services;

import org.eclim.client.Options;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;

/**
 * Command to update global settings.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class SettingsUpdateCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
    throws IOException
  {
    try{
      String settings = _commandLine.getValue(Options.SETTINGS_OPTION);

      settings = settings.replace('|', '\n');
      Properties properties = new Properties();
      properties.load(new ByteArrayInputStream(settings.getBytes()));

      boolean updateOptions = false;
      for(Iterator ii = properties.keySet().iterator(); ii.hasNext();){
        String name = (String)ii.next();
        String value = properties.getProperty(name);
        getEclimPreferences().setOption(name, value);
      }

      return Services.getMessage("settings.updated");
    }catch(Throwable t){
      return t;
    }
  }
}
