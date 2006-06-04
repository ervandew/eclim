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
}
