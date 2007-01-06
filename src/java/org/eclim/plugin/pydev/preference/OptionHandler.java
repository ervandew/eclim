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

      String paths = pathNature.getProjectExternalSourcePath();
      // make it look good in eclim settings window.
      paths = "\\\n\t\t" + paths.replaceAll("\\|", "|\\\\\n\t\t");
      options.put(PROJECT_EXTERNAL_SOURCE_PATH, paths);

      // account pydev including project name at root the the path.
      String name = _project.getName();
      String path = pathNature.getProjectSourcePath();
      if (path.startsWith('/' + name + '/')){
        path = path.substring(name.length() + 2);
      }
      if (path.length() == 0){
        path = ".";
      }
      options.put(PROJECT_SOURCE_PATH, path);
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
        if (!".".equals(_value) && !_project.getFolder(_value).exists()){
          throw new RuntimeException(Services.getMessage(
                "project.path.not.found", _value));
        }
        if(".".equals(_value)){
          _value = "";
        }
        pathNature.setProjectSourcePath('/' + _project.getName() + '/' + _value);
      }else if(PROJECT_EXTERNAL_SOURCE_PATH.equals(_name)){
        pathNature.setProjectExternalSourcePath(_value);
      }
      nature.rebuildPath();
    }
  }
}
