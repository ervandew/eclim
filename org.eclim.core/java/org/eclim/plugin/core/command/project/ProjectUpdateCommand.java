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
package org.eclim.plugin.core.command.project;

import java.io.File;
import java.io.FileReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.logging.Logger;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.preference.Preferences;

import org.eclim.plugin.core.project.ProjectManagement;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.util.IOUtils;

import org.eclipse.core.resources.IProject;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;

/**
 * Command to update a project.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "project_update",
  options =
    "REQUIRED p project ARG," +
    "OPTIONAL b buildfile ARG," +
    "OPTIONAL s settings ARG"
)
public class ProjectUpdateCommand
  extends AbstractCommand
{
  private static final Logger logger =
    Logger.getLogger(ProjectUpdateCommand.class);

  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String name = commandLine.getValue(Options.PROJECT_OPTION);
    String settings = commandLine.getValue(Options.SETTINGS_OPTION);

    IProject project = ProjectUtils.getProject(name);

    if(settings != null){
      List<Error> errors = updateSettings(project, settings);
      if (errors.size() > 0){
        return errors;
      }
    }else{
      List<Error> errors = ProjectManagement.update(project, commandLine);
      if(errors.size() > 0){
        return errors;
      }
    }

    return Services.getMessage("project.updated", name);
  }

  /**
   * Updates the projects settings.
   *
   * @param project The project.
   * @param settings The temp settings file.
   * @return List of errors or an empty List if none.
   */
  private List<Error> updateSettings(IProject project, String settings)
    throws Exception
  {
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
          preferences.setValue(project, name, value);
        }catch(IllegalArgumentException iae){
          errors.add(new Error(iae.getMessage(), null, 0, 0));
        }
      }
    }finally{
      IOUtils.closeQuietly(in);
      try{
        file.delete();
      }catch(Exception e){
        logger.warn("Error deleting project settings temp file: " + file, e);
      }
    }
    return errors;
  }
}
