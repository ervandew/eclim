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
package org.eclim.plugin.pydev.preference;

import java.io.File;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import org.eclim.Services;

import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.python.pydev.core.IInterpreterManager;
import org.python.pydev.core.IPythonPathNature;

import org.python.pydev.plugin.PydevPlugin;

import org.python.pydev.plugin.nature.PythonNature;

/**
 * Option handler for pydev options.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class OptionHandler
  implements org.eclim.preference.OptionHandler
{
  private static final String NATURE = "org.python.pydev.pythonNature";
  private static final String INTERPRETER_PATH =
    PydevPlugin.getPluginID() + ".INTERPRETER_PATH";
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
  public Map<String,String> getOptionsAsMap ()
    throws Exception
  {
    IInterpreterManager manager = PydevPlugin.getPythonInterpreterManager();
    String list = manager.getPersistedString();
    String [] executables = manager.getInterpretersFromPersistedString(list);
    HashMap options = new HashMap();
    options.put(INTERPRETER_PATH,
        formatArrayOption(StringUtils.join(executables, PATH_DELIMITER)));
    return options;
  }

  /**
   * {@inheritDoc}
   */
  public Map<String,String> getOptionsAsMap (IProject _project)
    throws Exception
  {
    PythonNature nature = PythonNature.getPythonNature(_project);
    if (nature != null){
      IPythonPathNature pathNature = nature.getPythonPathNature();
      HashMap<String,String> options = new HashMap<String,String>();
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
    IInterpreterManager manager = PydevPlugin.getPythonInterpreterManager();
    if(INTERPRETER_PATH.equals(_name)){
      String[] executables = StringUtils.stripAll(
          StringUtils.split(_value, PATH_DELIMITER));
      List original = Arrays.asList(
        manager.getInterpretersFromPersistedString(manager.getPersistedString()));
      for (int ii = 0; ii < executables.length; ii++){
        if(!original.contains(executables[ii])){
          File file = new File(executables[ii]);
          if(!file.exists()){
            throw new RuntimeException(
                Services.getMessage("executable.not.found", executables[ii]));
          }
          if(!file.isFile()){
            throw new RuntimeException(
                Services.getMessage("executable.not.a.file", executables[ii]));
          }
          String message = canExecute(executables[ii]);
          if(message != null){
            throw new RuntimeException(message);
          }
          manager.getInterpreterInfo(executables[ii], new NullProgressMonitor());
        }
      }
      String list = manager.getStringToPersist(executables);
      manager.setPersistedString(list);
      PydevPlugin.getDefault().savePluginPreferences();
    }
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

  private String canExecute (String _file)
  {
    try{
      Process process = Runtime.getRuntime().exec(_file + " -V");
      int exit = process.waitFor();
      if(exit != 0){
        return Services.getMessage("executable.failed", _file, new Integer(exit));
      }
    }catch(Exception e){
      return e.getMessage();
    }
    return null;
  }
}
