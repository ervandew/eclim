/**
 * Copyright (C) 2005 - 2015  Eric Van Dewoestine
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
package org.eclim.plugin.core.util;

import java.io.File;

import java.net.URI;

import org.eclim.Services;

import org.eclim.plugin.core.project.ProjectManagement;
import org.eclim.plugin.core.project.ProjectManager;
import org.eclim.plugin.core.project.ProjectNatureFactory;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

import org.eclipse.jface.text.IDocument;

/**
 * Utility methods for working with eclipse projects.
 *
 * @author Eric Van Dewoestine
 */
public class ProjectUtils
{
  /**
   * Gets the path on disk to the directory of the supplied project.
   *
   * @param project The project name.
   * @return The path or null if not found.
   */
  public static String getPath(String project)
    throws Exception
  {
    return getPath(getProject(project));
  }

  /**
   * Gets the path on disk to the directory of the supplied project.
   *
   * @param project The project.
   * @return The path or null if not found.
   */
  public static String getPath(IProject project)
    throws Exception
  {
    IPath path = getIPath(project);
    return path != null ? path.toOSString().replace('\\', '/') : null;
  }

  /**
   * Gets the project relative path of the supplied absolute file path.
   *
   * @param path The absolute path to a file in a project.
   * @return The path or null if not found.
   */
  public static String getProjectRelativePath(String path)
    throws Exception
  {
    // can't use URLEncoder on the full file since the colon in 'C:' gets
    // encoded as well.
    //URI uri = new URI("file://" + URLEncoder.encode(file, "UTF-8"));
    URI uri = new URI("file://" + path.replaceAll(" ", "%20"));
    IFile[] files = ResourcesPlugin
      .getWorkspace().getRoot().findFilesForLocationURI(uri);

    if (files.length > 0){
      return files[0].getProjectRelativePath().toString();
    }
    return null;
  }

  /**
   * Gets the path on disk to the directory of the supplied project.
   *
   * @param project The project.
   * @return The path or null if not found.
   */
  public static IPath getIPath(IProject project)
    throws Exception
  {
    IPath path = project.getRawLocation();

    // eclipse returns null for raw location if project is under the workspace.
    if(path == null){
      String name = project.getName();
      path = ResourcesPlugin.getWorkspace().getRoot().getRawLocation();
      path = path.append(name);
    }

    return path;
  }

  /**
   * Gets a project by name.
   *
   * @param name The name of the project.
   * @return The project which may or may not exist.
   */
  public static IProject getProject(String name)
    throws Exception
  {
    return getProject(name, false);
  }

  /**
   * Gets a project by name.
   *
   * @param name The name of the project.
   * @param open true to open the project if not already open, or false to do
   * nothing.
   * @return The project which may or may not exist.
   */
  public static IProject getProject(String name, boolean open)
    throws Exception
  {
    IProject project =
      ResourcesPlugin.getWorkspace().getRoot().getProject(name);

    if(open && project.exists() && !project.isOpen()){
      project.open(null);
    }

    return project;
  }

  /**
   * Gets the absolute file path.
   *
   * @param project The file's project.
   * @param file The file.
   * @return The absolute file path.
   */
  public static String getFilePath(String project, String file)
    throws Exception
  {
    return getFilePath(getProject(project), file);
  }

  /**
   * Gets the absolute file path.
   *
   * @param project The file's project.
   * @param file The file.
   * @return The absolute file path.
   */
  public static String getFilePath(IProject project, String file)
    throws Exception
  {
    file = file.replace('\\', '/');
    if(file.startsWith("/" + project.getName())){
      if(file.startsWith("/" + project.getName() + "/")){
        file = file.substring(2 + project.getName().length());
      }else if(file.endsWith("/" + project.getName())){
        file = file.substring(1 + project.getName().length());
      }

      // path is the project root
      if (file.length() == 0){
        return getPath(project);
      }
    }else if(file.startsWith("/") ||
        file.toLowerCase().startsWith("jar:") ||
        file.toLowerCase().startsWith("zip:"))
    {
      return file;
    }

    String projectPath = getPath(project);
    if(file.toLowerCase().startsWith(projectPath.toLowerCase())){
      return file;
    }

    IFile ifile = project.getFile(file);
    return ifile.getLocation().toOSString().replace('\\', '/');
  }

  /**
   * Gets the IFile instance for the specified file located in the supplied
   * project.
   *
   * @param project The file's project.
   * @param file The file.
   * @return The IFile.
   */
  public static IFile getFile(String project, String file)
    throws Exception
  {
    return getFile(getProject(project), file);
  }

  /**
   * Gets the IFile instance for the specified file located in the supplied
   * project.
   *
   * @param project The file's project.
   * @param file The file.
   * @return The IFile.
   */
  public static IFile getFile(IProject project, String file)
    throws Exception
  {
    if (!project.isOpen()){
      project.open(null);
    }
    String path = getPath(project);
    path = path.replace('\\', '/');
    /*if(!file.startsWith(path)){
      throw new RuntimeException(
          Services.getMessage("project.file.mismatch", file, path));
    }*/
    //String file = file.substring(path.length());

    IFile ifile = project.getFile(file);
    ifile.refreshLocal(IResource.DEPTH_INFINITE, null);

    // invoke any nature specific file refreshing
    String[] natures = ProjectNatureFactory.getProjectNatures(project);
    for (String nature : natures){
      ProjectManager manager = ProjectManagement.getProjectManager(nature);
      if (manager != null){
        manager.refresh(project, ifile);
      }
    }

    return ifile;
  }

  /**
   * Gets the IDocument instance for the given file.
   * <p/>
   * Borrowed from org.eclipse.ant.internal.ui.AntUtil
   *
   * @param project The project name.
   * @param file The file.
   * @return The IDocument.
   */
  public static IDocument getDocument(String project, String file)
    throws Exception
  {
    return getDocument(getProject(project), file);
  }

  /**
   * Gets the IDocument instance for the given file.
   * <p/>
   * Borrowed from org.eclipse.ant.internal.ui.AntUtil
   *
   * @param project The project.
   * @param file The file.
   * @return The IDocument.
   */
  public static IDocument getDocument(IProject project, String file)
    throws Exception
  {
    // using IFile would ensure that ifile.getProject() (used by at least pdt
    // internally) would result in the proper project reference, but seems to
    // break ant code completion and validation.
    //IFile thefile = getFile(project, file);
    File thefile = new File(getFilePath(project, file));
    if(!thefile.exists()){
      return null;
    }

    ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
    //IPath location = thefile.getFullPath();
    IPath location = new Path(thefile.getAbsolutePath());
    boolean connected = false;
    try {
      ITextFileBuffer buffer =
        manager.getTextFileBuffer(location, LocationKind.LOCATION);
      if (buffer == null) {
        //no existing file buffer..create one
        manager.connect(location, LocationKind.LOCATION, new NullProgressMonitor());
        connected = true;
        buffer = manager.getTextFileBuffer(location, LocationKind.LOCATION);
        if (buffer == null) {
          return null;
        }
      }
      return buffer.getDocument();
    } finally {
      if (connected) {
        try {
          manager.disconnect(
              location, LocationKind.LOCATION, new NullProgressMonitor());
        }catch(Exception e){
        }
      }
    }
  }

  /**
   * Closes the supplied project and suppresses any exceptions thrown.
   *
   * @param project The project.
   */
  public static void closeQuietly(IProject project)
  {
    try{
      if(project != null){
        project.close(null);
      }
    }catch(Exception ignore){
    }
  }

  /**
   * Assertion that the supplied project exists.
   *
   * @param project The project.
   */
  public static void assertExists(IProject project)
    throws Exception
  {
    if(project == null || !project.exists()){
      throw new IllegalArgumentException(
          Services.getMessage("project.not.found",
            project != null ? project.getName() : null));
    }
  }
}
