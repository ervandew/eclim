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
package org.eclim.plugin.dltk.util;

import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;

import org.eclim.Services;

import org.eclim.util.IOUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.dltk.compiler.env.IModuleSource;
import org.eclipse.dltk.compiler.env.ModuleSource;

import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IProjectFragment;
import org.eclipse.dltk.core.IScriptFolder;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.ISourceModule;

import org.eclipse.dltk.internal.core.util.Util;

/**
 * Utility methods for working with dltk projects.
 *
 * @author Eric Van Dewoestine
 */
public class DltkUtils
{
  private static ArrayList<String> natures = new ArrayList<String>();

  /**
   * Add the supplied nature to the list of known dltk based natures.
   *
   * @param nature The nature id.
   */
  public static void addDltkNature(String nature)
  {
    if (!natures.contains(nature)){
      natures.add(nature);
    }
  }

  /**
   * Gets an array of registered dltk natures.
   *
   * @return An array of natures ids.
   */
  public static String[] getDltkNatures()
  {
    return natures.toArray(new String[natures.size()]);
  }

  /**
   * Get an IModuleSource instance for the given file.
   *
   * @param file The file.
   * @return The IModuleSource
   */
  public static IModuleSource getModuleSource(IFile file)
  {
    InputStream in = null;
    try {
      in = file.getContents();
      String path = file.getLocation().toOSString();
      return new ModuleSource(path, IOUtils.toString(in));
    }catch(IOException ioe){
      throw new RuntimeException(ioe);
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }finally{
      IOUtils.closeQuietly(in);
    }
  }

  /**
   * Get an ISourceModule instance for the given file.
   *
   * @param file The file.
   * @return The ISourceModule
   */
  public static ISourceModule getSourceModule(IFile file)
  {
    // This block to find the ISourceModule is mostly copied from:
    // org.eclipse.dltk.ruby.internal.debug.ui.console.RubyConsoleSourceModuleLookup
    // Is there an easier way?
    IPath path = file.getFullPath();
    IProject project = file.getProject();
    IScriptProject scriptProject = DLTKCore.create(project);
    IProjectFragment[] roots = null;
    try{
      roots = scriptProject.getProjectFragments();
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }
    ISourceModule module = null;
    for (int j = 0, rootCount = roots.length; j < rootCount; j++) {
      final IProjectFragment root = roots[j];
      IPath rootPath = root.getPath();

      if (rootPath.isPrefixOf(path) && !Util.isExcluded(path, root, false)) {
        IPath localPath = path.setDevice(null).removeFirstSegments(
            rootPath.segmentCount());
        if (localPath.segmentCount() >= 1) {
          final IScriptFolder folder;
          if (localPath.segmentCount() > 1) {
            folder = root.getScriptFolder(localPath.removeLastSegments(1));
          } else {
            folder = root.getScriptFolder(Path.EMPTY);
          }
          module = folder.getSourceModule(localPath.lastSegment());
          break;
        }
      }
    }
    if (module == null || !module.exists()){
      // hacky removal of first segment to get project relative path
      String filepath = path.removeFirstSegments(1).toString();
      throw new IllegalArgumentException(
          Services.getMessage("src.file.not.found", filepath, ".buildpath"));
    }
    return module;
  }
}
