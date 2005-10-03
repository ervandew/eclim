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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclim.Services;

import org.eclim.client.Options;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;

import org.eclim.server.eclipse.EclimPreferences;

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
  private static final String[] SETTINGS = {JavaCore.COMPILER_SOURCE};

  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
    throws IOException
  {
    try{
      String name = _commandLine.getValue(Options.NAME_OPTION);

      // list all projects.
      if(name == null){
        List results = new ArrayList();
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        for(int ii = 0; ii < projects.length; ii++){
          if(projects[ii].exists()){
            StringBuffer info = new StringBuffer(projects[ii].getName())
              .append(" - ")
              .append(projects[ii].getRawLocation());
            results.add(info.toString());
          }
        }
       return filter(_commandLine, results);
      }else{
        IJavaModel model = JavaCore.create(
            ResourcesPlugin.getWorkspace().getRoot());
        IJavaProject javaProject = model.getJavaProject(name);
        if(!javaProject.exists()){
          throw new IllegalArgumentException(Services.getMessage(
                "project.not.found", new Object[]{name}));
        }
        Map results = new HashMap();
        Map options = javaProject.getOptions(true);
        for(int ii = 0; ii < SETTINGS.length; ii++){
          results.put(SETTINGS[ii], options.get(SETTINGS[ii]));
        }
        results.putAll(
            EclimPreferences.getPreferencesAsMap(javaProject.getProject()));

       return filter(_commandLine, results);
      }
    }catch(Throwable t){
      return t;
    }
  }
}
