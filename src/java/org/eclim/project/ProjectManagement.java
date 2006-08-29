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
package org.eclim.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;

import org.eclim.util.ProjectUtils;

import org.eclipse.core.resources.IProject;

/**
 * Class that handles registering and retrieving of {@link ProjectManager}s.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ProjectManagement
{
  private static Map managers = new HashMap();

  /**
   * Registers a ProjectManager.
   *
   * @param _nature The project nature that the manager will manage.
   * @param _manager The ProjectManager.
   * @return The ProjectManager.
   */
  public static ProjectManager addProjectManager (
      String _nature, ProjectManager _manager)
  {
    managers.put(_nature, _manager);
    return _manager;
  }

  /**
   * Gets a ProjectManager.
   *
   * @param _nature The nature to get the ProjectManager for.
   * @return The ProjectManager or null if none.
   */
  public static ProjectManager getProjectManager (String _nature)
  {
    return (ProjectManager)managers.get(_nature);
  }

  /**
   * Updates a project.
   *
   * @param _project The project.
   * @param _commandLine The command line for the project create command.
   */
  public static Error[] update (IProject _project, CommandLine _commandLine)
    throws Exception
  {
    ProjectUtils.assertExists(_project);

    List errors = new ArrayList();

    for (Iterator ii = managers.keySet().iterator(); ii.hasNext();){
      String nature = (String)ii.next();
      if(_project.hasNature(nature)){
        ProjectManager manager = ProjectManagement.getProjectManager(nature);
        Error[] errs = manager.update(_project, _commandLine);
        if(errs != null){
          CollectionUtils.addAll(errors, errs);
        }
      }
    }
    return (Error[])errors.toArray(new Error[errors.size()]);
  }

  /**
   * Removes the nature(s) from a project that this manager manages, or deletes
   * the project if no other natures exist for the project.
   *
   * @param _project The project.
   * @param _commandLine The command line for the project create command.
   */
  public static void delete (IProject _project, CommandLine _commandLine)
    throws Exception
  {
    ProjectUtils.assertExists(_project);

    for (Iterator ii = managers.keySet().iterator(); ii.hasNext();){
      String nature = (String)ii.next();
      if(_project.hasNature(nature)){
        ProjectManager manager = ProjectManagement.getProjectManager(nature);
        manager.delete(_project, _commandLine);
      }
    }
    _project.delete(false/*deleteContent*/, true/*force*/, null/*monitor*/);
  }

  /**
   * Refreshes a project by synchronizing it against the files on disk.
   *
   * @param _project The project.
   * @param _commandLine The command line for the project create command.
   */
  public static void refresh (IProject _project, CommandLine _commandLine)
    throws Exception
  {
    ProjectUtils.assertExists(_project);

    for (Iterator ii = managers.keySet().iterator(); ii.hasNext();){
      String nature = (String)ii.next();
      if(_project.hasNature(nature)){
        ProjectManager manager = ProjectManagement.getProjectManager(nature);
        manager.refresh(_project, _commandLine);
      }
    }
  }
}
