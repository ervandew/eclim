/**
 * Copyright (C) 2005 - 2018  Eric Van Dewoestine
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
package org.eclim.plugin.wst.util;

import org.eclim.Services;

import org.eclim.plugin.core.project.ProjectNatureFactory;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.plugin.wst.PluginResources;

import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.wst.jsdt.core.IJavaScriptProject;
import org.eclipse.wst.jsdt.core.IJavaScriptUnit;
import org.eclipse.wst.jsdt.core.JavaScriptCore;

/**
 * Utility methods for working with javascript files / projects.
 *
 * @author Eric Van Dewoestine
 */
public class JavaScriptUtils
{
  /**
   * Gets a javascript project by name.
   *
   * @param project The name of the project.
   * @return The project.
   */
  public static IJavaScriptProject getJavaScriptProject(String project)
  {
    return getJavaScriptProject(ProjectUtils.getProject(project, true));
  }

  /**
   * Gets a javascript project from the supplied IProject.
   *
   * @param project The IProject.
   * @return The javascript project.
   */
  public static IJavaScriptProject getJavaScriptProject(IProject project)
  {
    if(ProjectUtils.getPath(project) == null){
      throw new IllegalArgumentException(
          Services.getMessage("project.location.null", project.getName()));
    }

    try{
      if(!project.hasNature(PluginResources.JAVASCRIPT_NATURE)){
        String alias = ProjectNatureFactory
          .getAliasForNature(PluginResources.JAVASCRIPT_NATURE);
        throw new IllegalArgumentException(Services.getMessage(
              "project.missing.nature", project.getName(), alias));
      }
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }

    IJavaScriptProject javascriptProject = JavaScriptCore.create(project);
    if(javascriptProject == null || !javascriptProject.exists()){
      throw new IllegalArgumentException(
          Services.getMessage("project.not.found", project));
    }

    return javascriptProject;
  }

  /**
   * Gets the javascript unit from the supplied project.
   *
   * @param project The project name.
   * @param file The absolute path to the file.
   * @return The compilation unit or null if not found.
   */
  public static IJavaScriptUnit getJavaScriptUnit(String project, String file)
  {
    IJavaScriptProject javascriptProject = getJavaScriptProject(project);
    return getJavaScriptUnit(javascriptProject, file);
  }

  /**
   * Gets the javascript unit from the supplied project.
   *
   * @param project The project.
   * @param file The absolute path to the file.
   * @return The compilation unit or null if not found.
   */
  public static IJavaScriptUnit getJavaScriptUnit(
      IJavaScriptProject project, String file)
  {
    if (!project.isOpen()){
      try{
        project.open(null);
      }catch(CoreException ce){
        throw new RuntimeException(ce);
      }
    }
    return JavaScriptCore.createCompilationUnitFrom(
        ProjectUtils.getFile(project.getProject(), file));
  }
}
