/**
 * Copyright (c) 2005 - 2006
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

import java.io.File;
import java.io.FileInputStream;

import java.util.Properties;

import org.eclim.Services;

import org.eclim.logging.Logger;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.util.IOUtils;

/**
 * Command to update global settings.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class SettingsUpdateCommand
  extends AbstractCommand
{
  private static final Logger logger =
    Logger.getLogger(SettingsUpdateCommand.class);

  /**
   * {@inheritDoc}
   */
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    String settings = _commandLine.getValue(Options.SETTINGS_OPTION);

    Properties properties = new Properties();
    FileInputStream in = null;
    File file = new File(settings);
    try{
      in = new FileInputStream(file);
      properties.load(in);

      for(Object key : properties.keySet()){
        String name = (String)key;
        String value = properties.getProperty(name);
        getPreferences().setOption(name, value);
      }
    }finally{
      IOUtils.closeQuietly(in);
      try{
        file.delete();
      }catch(Exception e){
        logger.warn("Error deleting settings temp file: " + file, e);
      }
    }

    return Services.getMessage("settings.updated");
  }
}
