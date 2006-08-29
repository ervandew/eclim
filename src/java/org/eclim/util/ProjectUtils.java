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
package org.eclim.util;

import org.eclim.Services;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.IPath;

/**
 * Utility methods for working with eclipse projects.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ProjectUtils
{
  /**
   * Gets the path on disk to the directory of the supplied project.
   *
   * @param _project The project.
   * @return The path or null if not found.
   */
  public static String getPath (IProject _project)
    throws Exception
  {
     IPath path = _project.getRawLocation();

    // eclipse returns null for raw location if project is under the workspace.
    if(path == null){
      String name = _project.getName();
      path = ResourcesPlugin.getWorkspace().getRoot().getRawLocation();
      path = path.append(name);
    }

    return path != null ? path.toOSString() : null;
  }

  /**
   * Gets a project by name.
   *
   * @param _name The name of the project.
   * @return The project which may or may not exist.
   */
  public static IProject getProject (String _name)
    throws Exception
  {
    return getProject(_name, false);
  }

  /**
   * Gets a project by name.
   *
   * @param _name The name of the project.
   * @param _open true to open the project if not already open, or false to do
   * nothing.
   * @return The project which may or may not exist.
   */
  public static IProject getProject (String _name, boolean _open)
    throws Exception
  {
    IProject project =
      ResourcesPlugin.getWorkspace().getRoot().getProject(_name);

    if(_open && project.exists() && !project.isOpen()){
      project.open(null);
    }

    return project;
  }

  /**
   * Closes the supplied project and suppresses any exceptions thrown.
   *
   * @param _project The project.
   */
  public static void closeQuietly (IProject _project)
  {
    try{
      if(_project != null){
        _project.close(null);
      }
    }catch(Exception ignore){
    }
  }

  /**
   * Assertion that the supplied project exists.
   *
   * @param _project The project.
   */
  public static void assertExists (IProject _project)
    throws Exception
  {
    if(_project == null || !_project.exists()){
      throw new IllegalArgumentException(
          Services.getMessage("project.not.found",
            _project != null ? _project.getName() : null));
    }
  }
}
