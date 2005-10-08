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
package org.eclim.command.project;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclim.Services;

import org.eclim.client.Options;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;

import org.eclim.server.eclipse.EclimPreferences;
import org.eclim.server.eclipse.Option;
import org.eclim.server.eclipse.OptionInstance;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * Command to obtain project info.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ProjectInfoCommand
  extends AbstractCommand
{
  private static final Log log = LogFactory.getLog(ProjectInfoCommand.class);

  private EclimPreferences eclimPreferences;
  private Option[] editableOptions;

  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
    throws IOException
  {
    try{
      String name = _commandLine.getValue(Options.NAME_OPTION);
      List results = new ArrayList();

      // list all projects.
      if(name == null){
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        for(int ii = 0; ii < projects.length; ii++){
          if(projects[ii].exists()){
            StringBuffer info = new StringBuffer(projects[ii].getName())
              .append(" - ")
              .append(projects[ii].getRawLocation());
            results.add(info.toString());
          }
        }

      // retrieve project settings.
      }else{
        String setting = _commandLine.getValue(Options.SETTING_OPTION);
        IJavaModel model = JavaCore.create(
            ResourcesPlugin.getWorkspace().getRoot());
        IJavaProject javaProject = model.getJavaProject(name);
        if(!javaProject.exists()){
          throw new IllegalArgumentException(Services.getMessage(
                "project.not.found", new Object[]{name}));
        }
        Map options = javaProject.getOptions(true);
        Map preferences = eclimPreferences.getPreferencesAsMap(
            javaProject.getProject());

        // only retrieving the requested setting.
        if(setting != null){
          OptionInstance option = new OptionInstance();
          option.setName(setting);
          if(options.containsKey(setting)){
            option.setValue((String)options.get(setting));
          }else{
            option.setValue((String)preferences.get(setting));
          }
          results.add(option);

        // retrieve all settings.
        }else{
          for(int ii = 0; ii < editableOptions.length; ii++){
            results.add(new OptionInstance(editableOptions[ii],
                (String)options.get(editableOptions[ii].getName())));
          }
          for(Iterator ii = preferences.keySet().iterator(); ii.hasNext();){
            OptionInstance option = new OptionInstance();
            String key = (String)ii.next();
            option.setName(key);
            option.setValue((String)preferences.get(key));
            results.add(option);
          }
        }
      }
     return filter(_commandLine, results);
    }catch(Throwable t){
      return t;
    }
  }

  /**
   * Set eclimPreferences.
   * <p/>
   * Dependency injection.
   *
   * @param _eclimPreferences the value to set.
   */
  public void setEclimPreferences (EclimPreferences _eclimPreferences)
  {
    this.eclimPreferences = _eclimPreferences;
  }

  /**
   * Set editableOptions.
   * <p/>
   * Dependency injection.
   *
   * @param _editableOptions the value to set.
   */
  public void setEditableOptions (Option[] _editableOptions)
  {
    this.editableOptions = _editableOptions;
  }
}
