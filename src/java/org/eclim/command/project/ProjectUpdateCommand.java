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
package org.eclim.command.project;

import java.io.File;
import java.io.FileInputStream;

import java.util.List;
import java.util.Properties;

import org.eclim.Services;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.command.filter.ErrorFilter;

import org.eclim.logging.Logger;

import org.eclim.project.ProjectManagement;

import org.eclim.util.IOUtils;
import org.eclim.util.ProjectUtils;

import org.eclipse.core.resources.IProject;

/**
 * Command to update a project.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class ProjectUpdateCommand
  extends AbstractCommand
{
  private static final Logger logger =
    Logger.getLogger(ProjectUpdateCommand.class);

  /**
   * {@inheritDoc}
   */
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    String name = _commandLine.getValue(Options.PROJECT_OPTION);
    String settings = _commandLine.getValue(Options.SETTINGS_OPTION);

    IProject project = ProjectUtils.getProject(name);

    if(settings != null){
      updateSettings(project, settings);
    }else{
      List<Error> errors = ProjectManagement.update(project, _commandLine);
      if(errors.size() > 0){
        return ErrorFilter.instance.filter(_commandLine, errors);
      }
    }

    return Services.getMessage("project.updated", name);
  }

  /**
   * Updates the projects settings.
   *
   * @param _project The project.
   * @param _settings The temp settings file.
   */
  private void updateSettings (IProject _project, String _settings)
    throws Exception
  {
    Properties properties = new Properties();
    FileInputStream in = null;
    File file = new File(_settings);
    try{
      in = new FileInputStream(file);
      properties.load(in);

      for(Object key : properties.keySet()){
        String name = (String)key;
        String value = properties.getProperty(name);
        getPreferences().setOption(_project, name, value);
      }
    }finally{
      IOUtils.closeQuietly(in);
      try{
        file.delete();
      }catch(Exception e){
        logger.warn("Error deleting project settings temp file: " + file, e);
      }
    }
  }
}
