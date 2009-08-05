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

import org.eclim.plugin.core.preference.Preferences;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import org.eclipse.jdt.ui.JavaUI;

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
    @SuppressWarnings("unchecked")
    Map<String, String> coreOptions = JavaCore.getOptions();
    Map<String, String> options = new Hashtable<String, String>();
    options.putAll(coreOptions);
    options.putAll(getUIValues(null));
    return options;
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

    @SuppressWarnings("unchecked")
    Map<String, String> coreOptions = javaProject.getOptions(true);
    Map<String, String> options = new Hashtable<String, String>();
    options.putAll(coreOptions);
    options.putAll(getUIValues(project));

    return options;
  }

  private Map<String, String> getUIValues(IProject project)
    throws Exception
  {
    Hashtable<String, String> options = new Hashtable<String, String>();

    IScopeContext[] contexts = null;
    if (project != null){
      contexts = new IScopeContext[]{
        new ProjectScope(project),
        new InstanceScope(),
        new DefaultScope(),
      };
    }else{
      contexts = new IScopeContext[]{
        new InstanceScope(),
        new DefaultScope(),
      };
    }

    String[] names = Preferences.getInstance().getOptionNames();
    for (String name : names){
      if (name.startsWith(JavaUI.ID_PLUGIN)){
        for (IScopeContext context : contexts){
          IEclipsePreferences node = context.getNode(JavaUI.ID_PLUGIN);
          String value = node.get(name, null);
          if (value != null){
            options.put(name, value);
            break;
          }
        }
      }
    }

    return options;
  }

  /**
   * {@inheritDoc}
   */
  public void setOption(String name, String value)
    throws Exception
  {
    @SuppressWarnings("unchecked")
    Map<String, String> options = JavaCore.getOptions();

    if(name.equals(JavaCore.COMPILER_SOURCE)){
      JavaUtils.setCompilerSourceCompliance(value);
    }else if (name.startsWith(JavaUI.ID_PLUGIN)){
      Preferences.getInstance()
        .setPreference(JavaUI.ID_PLUGIN, null, name, value);
    }else{
      options.put(name, value);
      JavaCore.setOptions((Hashtable<String, String>)options);
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

    @SuppressWarnings("unchecked")
    Map<String, String> global = javaProject.getOptions(true);
    @SuppressWarnings("unchecked")
    Map<String, String> options = javaProject.getOptions(false);

    Object current = global.get(name);
    if(current == null || !current.equals(value)){
      if(name.equals(JavaCore.COMPILER_SOURCE)){
        JavaUtils.setCompilerSourceCompliance(javaProject, value);
      }else if (name.startsWith(JavaUI.ID_PLUGIN)){
        Preferences.getInstance()
          .setPreference(JavaUI.ID_PLUGIN, project, name, value);
      }else{
        options.put(name, value);
        javaProject.setOptions(options);
      }
    }
  }
}
