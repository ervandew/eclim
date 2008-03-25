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
package org.eclim.util;

import java.io.File;

import org.eclim.Services;

import org.eclim.util.file.FileUtils;

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
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class ProjectUtils
{
  /**
   * Gets the path on disk to the directory of the supplied project.
   *
   * @param _project The project name.
   * @return The path or null if not found.
   */
  public static String getPath (String _project)
    throws Exception
  {
    return getPath(getProject(_project));
  }

  /**
   * Gets the path on disk to the directory of the supplied project.
   *
   * @param _project The project.
   * @return The path or null if not found.
   */
  public static String getPath (IProject _project)
    throws Exception
  {
     IPath path = _project.getRawLocation();

    // eclipse returns null for raw location if project is under the workspace.
    if(path == null){
      String name = _project.getName();
      path = ResourcesPlugin.getWorkspace().getRoot().getRawLocation();
      path = path.append(name);
    }

    return path != null ? path.toOSString() : null;
  }

  /**
   * Gets a project by name.
   *
   * @param _name The name of the project.
   * @return The project which may or may not exist.
   */
  public static IProject getProject (String _name)
    throws Exception
  {
    return getProject(_name, false);
  }

  /**
   * Gets a project by name.
   *
   * @param _name The name of the project.
   * @param _open true to open the project if not already open, or false to do
   * nothing.
   * @return The project which may or may not exist.
   */
  public static IProject getProject (String _name, boolean _open)
    throws Exception
  {
    IProject project =
      ResourcesPlugin.getWorkspace().getRoot().getProject(_name);

    if(_open && project.exists() && !project.isOpen()){
      project.open(null);
    }

    return project;
  }

  /**
   * Gets the absolute file path.
   *
   * @param _project The file's project.
   * @param _file The file.
   * @return The absolute file path.
   */
  public static String getFilePath (String _project, String _file)
    throws Exception
  {
    return getFilePath(getProject(_project), _file);
  }

  /**
   * Gets the absolute file path.
   *
   * @param _project The file's project.
   * @param _file The file.
   * @return The absolute file path.
   */
  public static String getFilePath (IProject _project, String _file)
    throws Exception
  {
    if(_file.startsWith("/" + _project.getName() + "/")){
      _file = _file.substring(2 + _project.getName().length());
    }
    return FileUtils.concat(getPath(_project), _file);
  }

  /**
   * Gets the IFile instance for the specified file located in the supplied
   * project.
   *
   * @param _project The file's project.
   * @param _file The file.
   * @return The IFile.
   */
  public static IFile getFile (String _project, String _file)
    throws Exception
  {
    return getFile(getProject(_project), _file);
  }

  /**
   * Gets the IFile instance for the specified file located in the supplied
   * project.
   *
   * @param _project The file's project.
   * @param _file The file.
   * @return The IFile.
   */
  public static IFile getFile (IProject _project, String _file)
    throws Exception
  {
    _project.open(null);
    String path = getPath(_project);
    path = path.replace('\\', '/');
    /*if(!_file.startsWith(path)){
      throw new RuntimeException(
          Services.getMessage("project.file.mismatch", _file, path));
    }*/
    //String file = _file.substring(path.length());

    IFile ifile = _project.getFile(_file);
    ifile.refreshLocal(IResource.DEPTH_INFINITE, null);
    return ifile;
  }

  /**
   * Gets the IDocument instance for the given file.
   * <p/>
   * Borrowed from org.eclipse.ant.internal.ui.AntUtil
   *
   * @param _project The project name.
   * @param _file The file.
   * @return The IDocument.
   */
  public static IDocument getDocument (String _project, String _file)
    throws Exception
  {
    return getDocument(getProject(_project), _file);
  }

  /**
   * Gets the IDocument instance for the given file.
   * <p/>
   * Borrowed from org.eclipse.ant.internal.ui.AntUtil
   *
   * @param _project The project.
   * @param _file The file.
   * @return The IDocument.
   */
  public static IDocument getDocument (IProject _project, String _file)
    throws Exception
  {
    // using IFile would ensure that ifile.getProject() (used by at least pdt
    // internally) would result in the proper project reference, but seems to
    // break ant code completion and validation.
    //IFile file = getFile(_project, _file);
    File file = new File(FileUtils.concat(getPath(_project), _file));
    if(!file.exists()){
      return null;
    }

    ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
    //IPath location = file.getFullPath();
    IPath location= new Path(file.getAbsolutePath());
    boolean connected= false;
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
        } catch (Exception e) {
        }
      }
    }
  }

  /**
   * Closes the supplied project and suppresses any exceptions thrown.
   *
   * @param _project The project.
   */
  public static void closeQuietly (IProject _project)
  {
    try{
      if(_project != null){
        _project.close(null);
      }
    }catch(Exception ignore){
    }
  }

  /**
   * Assertion that the supplied project exists.
   *
   * @param _project The project.
   */
  public static void assertExists (IProject _project)
    throws Exception
  {
    if(_project == null || !_project.exists()){
      throw new IllegalArgumentException(
          Services.getMessage("project.not.found",
            _project != null ? _project.getName() : null));
    }
  }
}
