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
 * @author Eric Van Dewoestine (ervandew@gmail.com)
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
  public String execute(CommandLine commandLine)
    throws Exception
  {
    String settings = commandLine.getValue(Options.SETTINGS_OPTION);

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
