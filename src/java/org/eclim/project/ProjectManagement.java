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

import java.util.HashMap;
import java.util.Map;

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
}
