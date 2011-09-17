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

import java.io.File;
import java.io.FileInputStream;

import java.util.ArrayList;
import java.util.Properties;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.logging.Logger;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.util.IOUtils;
import org.eclim.util.StringUtils;

/**
 * Command to update global settings.
 *
 * @author Eric Van Dewoestine
 */
@Command(name = "settings_update", options = "OPTIONAL s settings ARG")
public class SettingsUpdateCommand
  extends AbstractCommand
{
  private static final Logger logger =
    Logger.getLogger(SettingsUpdateCommand.class);

  /**
   * {@inheritDoc}
   */
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String settings = commandLine.getValue(Options.SETTINGS_OPTION);

    Properties properties = new Properties();
    FileInputStream in = null;
    File file = new File(settings);
    ArrayList<String> errors = new ArrayList<String>();
    try{
      in = new FileInputStream(file);
      properties.load(in);

      for(Object key : properties.keySet()){
        String name = (String)key;
        String value = properties.getProperty(name);
        try{
          getPreferences().setValue(name, value);
        }catch(IllegalArgumentException iae){
          errors.add(iae.getMessage());
        }
      }
    }finally{
      IOUtils.closeQuietly(in);
      try{
        file.delete();
      }catch(Exception e){
        logger.warn("Error deleting settings temp file: " + file, e);
      }
    }

    if (errors.size() > 0){
      return StringUtils.join(errors, '\n');
    }
    return Services.getMessage("settings.updated");
  }
}
