/**
 * Copyright (C) 2005 - 2017  Eric Van Dewoestine
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
package org.eclim.plugin.cdt.util;

import org.eclim.Services;

import org.eclim.plugin.cdt.PluginResources;

import org.eclim.plugin.core.project.ProjectNatureFactory;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;

import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

/**
 * Utility methods for working with c/cpp files / projects.
 *
 * @author Eric Van Dewoestine
 */
public class CUtils
{
  /**
   * Gets a c project by name.
   *
   * @param project The name of the project.
   * @return The project.
   */
  public static ICProject getCProject(String project)
  {
    return getCProject(ProjectUtils.getProject(project, true));
  }

  /**
   * Gets a c project from the supplied IProject.
   *
   * @param project The IProject.
   * @return The c project.
   */
  public static ICProject getCProject(IProject project)
  {
    if(ProjectUtils.getPath(project) == null){
      throw new IllegalArgumentException(
          Services.getMessage("project.location.null", project.getName()));
    }

    try{
      if (!project.hasNature(PluginResources.NATURE_C) &&
          !project.hasNature(PluginResources.NATURE_CPP))
      {
        String name =
          ProjectNatureFactory.getAliasForNature(PluginResources.NATURE_C) + " / " +
          ProjectNatureFactory.getAliasForNature(PluginResources.NATURE_CPP);

        throw new IllegalArgumentException(Services.getMessage(
              "project.missing.nature", project.getName(), name));
      }
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }

    ICProject cproject = CoreModel.getDefault().create(project);
    if(cproject == null || !cproject.exists()){
      throw new IllegalArgumentException(
          Services.getMessage("project.not.found", project));
    }

    return cproject;
  }

  /**
   * Finds a compilation unit by looking in all the java project of the supplied
   * name.
   *
   * @param project The name of the project to locate the file in.
   * @param file The file to find.
   * @return The compilation unit.
   */
  public static ITranslationUnit getTranslationUnit(String project, String file)
  {
    return getTranslationUnit(getCProject(project), file);
  }

  /**
   * Gets the compilation unit from the supplied project.
   *
   * @param project The project.
   * @param file The absolute path to the file.
   * @return The compilation unit or null if not found.
   */
  public static ITranslationUnit getTranslationUnit(
      ICProject project, String file)
  {
    try{
      Path path = new Path(ProjectUtils.getFilePath(project.getProject(), file));
      ITranslationUnit src =
        CoreModelUtil.findTranslationUnitForLocation(path, project);
      if(src == null || !src.exists()){
        throw new IllegalArgumentException(
            Services.getMessage("cdt.src.file.not.found", file));
      }

      // force sync w/ file on disk
      src.close();
      src.open(null);

      return src;
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }
  }
}
