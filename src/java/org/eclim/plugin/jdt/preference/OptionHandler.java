/**
 * Copyright (C) 2005 - 2009  Eric Van Dewoestine
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
 * @author Eric Van Dewoestine
 */
public class OptionHandler
  implements org.eclim.plugin.core.preference.OptionHandler
{
  private static final String NATURE = "org.eclipse.jdt.core.javanature";

  /**
   * {@inheritDoc}
   * @see org.eclim.plugin.core.preference.OptionHandler#getNature()
   */
  public String getNature()
  {
    return NATURE;
  }

  /**
   * {@inheritDoc}
   */
  public Map<String, String> getValues()
    throws Exception
  {
    return JavaCore.getOptions();
  }

  /**
   * {@inheritDoc}
   */
  public Map<String, String> getValues(IProject project)
    throws Exception
  {
    IJavaProject javaProject = JavaCore.create(project);
    if(!javaProject.exists()){
      throw new IllegalArgumentException(Services.getMessage(
            "project.not.found", project.getName()));
    }

    return javaProject.getOptions(true);
  }

  /**
   * {@inheritDoc}
   */
  public void setOption(String name, String value)
    throws Exception
  {
    Map<String, String> options = JavaCore.getOptions();

    if(name.equals(JavaCore.COMPILER_SOURCE)){
      JavaUtils.setCompilerSourceCompliance(value);
    }else{
      options.put(name, value);
      JavaCore.setOptions((Hashtable)options);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void setOption(IProject project, String name, String value)
    throws Exception
  {
    IJavaProject javaProject = JavaCore.create(project);
    if(!javaProject.exists()){
      throw new IllegalArgumentException(
          Services.getMessage("project.not.found", project.getName()));
    }
    Map<String, String> global = javaProject.getOptions(true);
    Map<String, String> options = javaProject.getOptions(false);

    Object current = global.get(name);
    if(current == null || !current.equals(value)){
      if(name.equals(JavaCore.COMPILER_SOURCE)){
        JavaUtils.setCompilerSourceCompliance(javaProject, value);
      }else{
        options.put(name, value);
        javaProject.setOptions(options);
      }
    }
  }
}
