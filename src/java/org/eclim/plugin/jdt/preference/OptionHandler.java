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
package org.eclim.plugin.jdt.preference;

import java.util.Hashtable;
import java.util.Map;

import org.eclim.Services;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclipse.core.resources.IProject;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * Option handler for jdt/java options.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class OptionHandler
  implements org.eclim.preference.OptionHandler
{
  private static final String NATURE = "org.eclipse.jdt.core.javanature";

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
  {
    return JavaCore.getOptions();
  }

  /**
   * {@inheritDoc}
   */
  public Map getOptionsAsMap (IProject _project)
  {
    IJavaProject javaProject = JavaCore.create(_project);
    if(!javaProject.exists()){
      throw new IllegalArgumentException(Services.getMessage(
            "project.not.found", _project.getName()));
    }

    return javaProject.getOptions(true);
  }

  /**
   * {@inheritDoc}
   */
  public void setOption (String _name, String _value)
  {
    try{
      Map options = JavaCore.getOptions();

      if(_name.equals(JavaCore.COMPILER_SOURCE)){
        JavaUtils.setCompilerSourceCompliance((String)_value);
      }else{
        options.put(_name, _value);
        JavaCore.setOptions((Hashtable)options);
      }
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void setOption (IProject _project, String _name, String _value)
  {
    IJavaProject javaProject = JavaCore.create(_project);
    if(!javaProject.exists()){
      throw new IllegalArgumentException(
          Services.getMessage("project.not.found", _project.getName()));
    }
    Map global = javaProject.getOptions(true);
    Map options = javaProject.getOptions(false);

    Object current = global.get(_name);
    try{
      if(current == null || !current.equals(_value)){
        if(_name.equals(JavaCore.COMPILER_SOURCE)){
          JavaUtils.setCompilerSourceCompliance(javaProject, (String)_value);
        }else{
          options.put(_name, _value);
          javaProject.setOptions(options);
        }
      }
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }
}
