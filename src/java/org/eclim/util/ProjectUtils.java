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
package org.eclim.util;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

import org.eclim.Services;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.jface.text.IDocument;

/**
 * Utility methods for working with eclipse projects.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
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
    return FilenameUtils.concat(getPath(_project), _file);
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
    return FilenameUtils.concat(getPath(_project), _file);
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
          Services.getMessage("project.file.mismatch",
            new String[]{_file, path}));
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
    File file = new File(FilenameUtils.concat(getPath(_project), _file));
    if(!file.exists()){
      return null;
    }

    ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
    IPath location= new Path(file.getAbsolutePath());
    boolean connected= false;
    try {
      ITextFileBuffer buffer= manager.getTextFileBuffer(location);
      if (buffer == null) {
        //no existing file buffer..create one
        manager.connect(location, new NullProgressMonitor());
        connected= true;
        buffer= manager.getTextFileBuffer(location);
        if (buffer == null) {
          return null;
        }
      }
      return buffer.getDocument();
    } finally {
      if (connected) {
        try {
          manager.disconnect(location, new NullProgressMonitor());
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
