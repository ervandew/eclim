/**
 * Copyright (C) 2005 - 2016  Eric Van Dewoestine
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
package org.eclim.plugin.core.util;

import org.eclim.Services;
import org.eclim.logging.Logger;
import org.eclim.plugin.core.util.ProjectUtils;
import org.eclipse.core.resources.IProject;

/**
 * Util class which helps to generate absolute paths and checks paths for not
 * allowed content.
 *
 * @author Lukas Roth
 *
 */
public class PathUtil
{
  private static final Logger logger = Logger.getLogger(PathUtil.class);

  /**
   * Checks if the <code>path</code> contains ".." as a substring. If so throw a
   * PathUtilException.
   *
   * @param path
   * @throws PathUtilException
   */
  public static void checkPathForEscaping(String path)
      throws PathUtilException
  {
    if (path.contains("..")) {
      String errMsg = Services.getMessage("file.path.error.illegal.path", path);
      throw new PathUtilException(errMsg);
    }
  }

  /**
   * Returns the absolute file path generated form the <code>projectName</code>
   * and the relative file path to the project <code>filePath</code>.
   *
   * Throws <code>PathUtilException</code> if the project can not be found.
   *
   * Example: If there is an exampleProject at location
   * '/home/user/projects/exampleProject' then
   * 'getAbsoluteFilePath(exampleProject, /path/to/my/file.txt)' returns
   * '/home/user/projects/exampleProject/path/to/my/file.txt'
   *
   * @param projectName
   * @param filePath
   * @return absoluteFilePath Absolute file path.
   * @throws PathUtilException
   */
  public static String getAbsolutePath(String projectName, String filePath)
      throws PathUtilException
  {
    return getProjectPath(projectName) + "/" + filePath;
  }

  /**
   * Returns the absolute path of the project <code>projectName</code>.
   *
   * @param projectName
   * @return absoluteProjectPath Absolute path of the project.
   * @throws PathUtilException
   *           Throws PathUtilException the project does not exist.
   */
  public static String getProjectPath(String projectName)
      throws PathUtilException
  {
    IProject project;
    try {
      project = ProjectUtils.getProject(projectName);
    } catch (Exception e) {
      throw new PathUtilException(
          Services.getMessage("file.path.error.project.not.exist", projectName), e);
    }
    if (project == null || project.getLocation() == null) {
      throw new PathUtilException(
          Services.getMessage("file.path.error.project.not.exist", projectName));
    }
    return project.getLocation().toOSString();
  }
}
