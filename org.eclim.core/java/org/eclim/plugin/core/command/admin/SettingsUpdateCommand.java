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

import java.io.File;
import java.io.FileReader;

import java.util.ArrayList;
import java.util.Map;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.logging.Logger;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.preference.Preferences;

import org.eclim.util.IOUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;

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

  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String settings = commandLine.getValue(Options.SETTINGS_OPTION);

    FileReader in = null;
    File file = new File(settings);
    ArrayList<Error> errors = new ArrayList<Error>();
    try{
      in = new FileReader(file);
      JsonStreamParser parser = new JsonStreamParser(in);
      JsonObject obj = (JsonObject)parser.next();

      Preferences preferences = getPreferences();
      for (Map.Entry<String, JsonElement> entry : obj.entrySet()){
        String name = entry.getKey();
        String value = entry.getValue().getAsString();
        try{
          preferences.setValue(name, value);
        }catch(IllegalArgumentException iae){
          errors.add(new Error(iae.getMessage(), null, 0, 0));
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
      return errors;
    }
    return Services.getMessage("settings.updated");
  }
}
