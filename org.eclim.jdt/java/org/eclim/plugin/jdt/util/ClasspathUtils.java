/**
 * Copyright (C) 2005 - 2012  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclim.logging.Logger;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.IPath;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Utility methods for working with the java project's classpath.
 *
 * @author Eric Van Dewoestine
 */
public class ClasspathUtils
{
  private static final Logger logger = Logger.getLogger(ClasspathUtils.class);

  /**
   * Gets an array of paths representing the project's classpath.
   *
   * @param project The java project instance.
   * @return Array of paths.
   */
  public static String[] getClasspath(IJavaProject project)
    throws Exception
  {
    HashSet<IJavaProject> visited = new HashSet<IJavaProject>();
    ArrayList<String> paths = new ArrayList<String>();
    collect(project, paths, visited, true);
    return paths.toArray(new String[paths.size()]);
  }

  /**
   * Gets an array of paths representing the project's source paths.
   *
   * @param project The java project instance.
   * @return Array of paths.
   */
  public static String[] getSrcPaths(IJavaProject project)
    throws Exception
  {
    ArrayList<String> paths = new ArrayList<String>();
    IClasspathEntry[] entries = project.getResolvedClasspath(true);
    for (IClasspathEntry entry : entries){
      if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE){
        paths.add(ProjectUtils.getFilePath(
              project.getProject(), entry.getPath().toOSString()));
      }
    }

    return paths.toArray(new String[paths.size()]);
  }

  /**
   * Recursively collects classpath entries from the current and dependent
   * projects.
   */
  private static void collect(
      IJavaProject javaProject,
      List<String> paths,
      Set<IJavaProject> visited,
      boolean isFirstProject)
    throws Exception
  {
    if(visited.contains(javaProject)){
      return;
    }
    visited.add(javaProject);

    try{
      IPath out = javaProject.getOutputLocation();
      paths.add(
          ProjectUtils.getFilePath(
            javaProject.getProject(),
            out.addTrailingSeparator().toOSString()));
    }catch(JavaModelException ignore){
      // ignore... just signals that no output dir was configured.
    }

    IProject project = javaProject.getProject();
    String name = project.getName();

    IClasspathEntry[] entries = null;
    try {
      entries = javaProject.getResolvedClasspath(true);
    }catch(JavaModelException jme){
      // this may or may not be a problem.
      logger.warn(
          "Unable to retrieve resolved classpath for project: " + name, jme);
      return;
    }

    final List<IJavaProject> nextProjects = new ArrayList<IJavaProject>();
    for(IClasspathEntry entry : entries) {
      switch (entry.getEntryKind()) {
        case IClasspathEntry.CPE_LIBRARY :
        case IClasspathEntry.CPE_CONTAINER :
        case IClasspathEntry.CPE_VARIABLE :
          String path = entry.getPath().toOSString().replace('\\', '/');
          if(path.startsWith("/" + name + "/")){
            path = ProjectUtils.getFilePath(project, path);
          }
          paths.add(path);
          break;
        case IClasspathEntry.CPE_PROJECT :
          if (isFirstProject || entry.isExported()){
            javaProject =
              JavaUtils.getJavaProject(entry.getPath().segment(0));
            if (javaProject != null){
              // breadth first, not depth first, to preserve dependency ordering
              nextProjects.add(javaProject);
            }
          }
          break;
        case IClasspathEntry.CPE_SOURCE :
          IPath out = entry.getOutputLocation();
          if (out != null){
            paths.add(
                ProjectUtils.getFilePath(
                  javaProject.getProject(),
                  out.addTrailingSeparator().toOSString()));
          }
          break;
      }
    }
    // depth second
    for(final IJavaProject nextProject : nextProjects) {
      collect(nextProject, paths, visited, false);
    }
  }
}
