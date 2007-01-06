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
package org.eclim.plugin.pydev.preference;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import org.eclim.Services;

import org.eclim.util.ProjectUtils;

import org.eclipse.core.resources.IProject;

import org.python.pydev.core.IPythonPathNature;

import org.python.pydev.plugin.PydevPlugin;

import org.python.pydev.plugin.nature.PythonNature;

/**
 * Option handler for pydev options.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class OptionHandler
  implements org.eclim.preference.OptionHandler
{
  private static final String NATURE = "org.python.pydev.pythonNature";
  private static final String PYTHON_VERSION =
    PydevPlugin.getPluginID() + ".PYTHON_PROJECT_VERSION";
  private static final String PROJECT_SOURCE_PATH =
    PydevPlugin.getPluginID() + ".PROJECT_SOURCE_PATH";
  private static final String PROJECT_EXTERNAL_SOURCE_PATH =
    PydevPlugin.getPluginID() + ".PROJECT_EXTERNAL_SOURCE_PATH";

  private static final String PROJECT_ROOT = ".";
  private static final String PATH_DELIMITER = "|";

  /**
   * {@inheritDoc}
   * @see org.eclim.preference.OptionHandler#getNature()
   */
  public String getNature ()
  {
    return NATURE;
  }

  /**
   * {@inheritDoc}
   */
  public Map getOptionsAsMap ()
    throws Exception
  {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public Map getOptionsAsMap (IProject _project)
    throws Exception
  {
    PythonNature nature = PythonNature.getPythonNature(_project);
    if (nature != null){
      IPythonPathNature pathNature = nature.getPythonPathNature();
      HashMap options = new HashMap();
      options.put(PYTHON_VERSION, nature.getVersion());

      String paths = pathNature.getProjectSourcePath();
      options.put(PROJECT_SOURCE_PATH, normalizeProjectPaths(_project, paths));

      paths = pathNature.getProjectExternalSourcePath();
      options.put(PROJECT_EXTERNAL_SOURCE_PATH, formatArrayOption(paths));

      return options;
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public void setOption (String _name, String _value)
    throws Exception
  {
  }

  /**
   * {@inheritDoc}
   */
  public void setOption (IProject _project, String _name, String _value)
    throws Exception
  {
    PythonNature nature = PythonNature.getPythonNature(_project);
    if (nature != null){
      IPythonPathNature pathNature = nature.getPythonPathNature();
      if(PYTHON_VERSION.equals(_name)){
        nature.setVersion(_value);
      }else if(PROJECT_SOURCE_PATH.equals(_name)){
        String[] paths = StringUtils.split(_value, PATH_DELIMITER);
        for (int ii = 0; ii < paths.length; ii++){
          String path = paths[ii];
          if (path.endsWith("/")){
            path = path.substring(0, path.length() - 1);
          }
          if (!PROJECT_ROOT.equals(path) &&
              !_project.getFolder(path).exists())
          {
            throw new RuntimeException(Services.getMessage(
                  "project.path.not.found", path));
          }
        }
        pathNature.setProjectSourcePath(
            denormalizeProjectPaths(_project, _value));
      }else if(PROJECT_EXTERNAL_SOURCE_PATH.equals(_name)){
        pathNature.setProjectExternalSourcePath(_value);
      }
      nature.rebuildPath();
    }
  }

  /**
   * Normalize all the paths in the pipe delimited list of paths so that they
   * aren't prefixed with the project name.
   *
   * @param _project The project.
   * @param _paths The pipe delimited array of paths.
   * @return Normalized paths.
   */
  private String normalizeProjectPaths (IProject _project, String _paths)
    throws Exception
  {
    String projectName = _project.getName();
    String[] paths = StringUtils.split(_paths, PATH_DELIMITER);
    for (int ii = 0; ii < paths.length; ii++){
      String path = paths[ii];
      if (path.startsWith('/' + projectName)){
        path = path.substring(projectName.length() + 1);
        if (path.startsWith("/")){
          path = path.substring(1);
        }
      }
      if (path.trim().length() == 0){
        path = PROJECT_ROOT;
      }
      paths[ii] = path;
    }
    return StringUtils.join(paths, PATH_DELIMITER);
  }

  /**
   * De-normalize all the paths in the pipe delimited list of paths so that they
   * are prefixed with the project name.
   *
   * @param _project The project.
   * @param _paths The pipe delimited array of paths.
   * @return De-normalized paths.
   */
  private String denormalizeProjectPaths (IProject _project, String _paths)
    throws Exception
  {
    String projectName = _project.getName();
    if (_paths.trim().length() == 0){
      return '/' + projectName;
    }

    String[] paths = StringUtils.split(_paths, PATH_DELIMITER);
    for (int ii = 0; ii < paths.length; ii++){
      String path = paths[ii];
      path = '/' + projectName + '/' + path;
      int index = path.indexOf('/' + PROJECT_ROOT);
      if (index != -1){
        path = path.replaceAll("/\\.", "");
      }
      paths[ii] = path;
    }
    return StringUtils.join(paths, PATH_DELIMITER);
  }

  /**
   * Format the supplied array property for display on eclim settings page.
   * <p/>
   * TODO: This should be an attribute of the option configuration so that the
   * settings filter can handle display issues.
   *
   * @param _value The option value.
   * @return The formatted option.
   */
  private String formatArrayOption (String _value)
  {
    return "\\\n\t\t" + _value.replaceAll("\\|", "|\\\\\n\t\t");
  }
}
