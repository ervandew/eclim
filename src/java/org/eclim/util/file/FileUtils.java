/**
 * Copyright (c) 2004 - 2005
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
package org.eclim.util.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import java.util.Enumeration;

import java.util.regex.Pattern;

import javax.naming.CompositeName;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;

import org.apache.log4j.Logger;

import org.eclipse.core.resources.IProject;

/**
 * Utilities for working w/ files and commons vfs.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class FileUtils
{
  private static final Logger logger = Logger.getLogger(FileUtils.class);

  public static final String JAR_PREFIX = "jar://";
  public static final String ZIP_PREFIX = "zip://";
  public static final String JAR_EXT = ".jar";
  public static final String ZIP_EXT = ".zip";

  /**
   * Gets a project relative file.
   *
   * @param _project The project.
   * @param _file The file.
   * @return The File.
   */
  public static File getProjectRelativeFile (IProject _project, String _file)
  {
    return new File(FilenameUtils.concat(
          _project.getRawLocation().toOSString(), _file));
  }

  /**
   * Converts the supplied offset into an int array where the first element is
   * the line number and the second is the column number.
   *
   * @param _filename The file to translate the offset for.
   * @param _offset The offset.
   * @return The line and column int array.
   */
  public static int[] offsetToLineColumn (String _filename, int _offset)
    throws Exception
  {
    BufferedReader reader = null;
    try{
      FileSystemManager fsManager = VFS.getManager();
      FileObject file = fsManager.resolveFile(_filename);

      // disable caching (the cache seems to become invalid at some point
      // causing vfs errors).
      //fsManager.getFilesCache().clear(file.getFileSystem());

      if(!file.exists()){
        logger.warn("File '{}' not found.", _filename);
        return null;
      }
      reader = new BufferedReader(
          new InputStreamReader(file.getContent().getInputStream()));

      int offset = 0;
      int lines = 1;
      int count = 0;
      int columns = 1;
      char[] buffer = new char[1024];
      while((count = reader.read(buffer, 0, buffer.length)) != -1){
        for(int ii = 0; ii < count; ii++){
          if(buffer[ii] == '\n'){
            offset++;
            lines++;
            columns = 1;
          }else if(buffer[ii] == '\r'){
            // assume that in vim the user has fileformat set to dos, otherwise
            // they are staring at ^M everywhere, and i can't imagine someone
            // coding on a regular basis like that.  Otherwise we would need to
            // incrment the columns here too.
            offset++;
          }else{
            offset++;
            columns++;
          }

          if(offset == _offset){
            return new int[]{lines, columns};
          }
        }
      }
    }catch(Exception e){
      throw new RuntimeException(e);
    }finally{
      IOUtils.closeQuietly(reader);
    }
    return new int[]{1, 1};
  }

  /**
   * Translates a file name that does not conform to the standard url file
   * format.
   * <p/>
   * Main purpose is to convert paths like:<br/>
   * <code>/opt/sun-jdk-1.5.0.05/src.zip/javax/swing/Spring.java</code><br/>
   * to<br/>
   * <code>zip:file:///opt/sun-jdk-1.5.0.05/src.zip!/javax/swing/Spring.java</code>
   *
   * @param _file The file to translate.
   * @return The translated file.
   */
  public static String toUrl (String _file)
  {
    // if the path points to a real file, return it.
    if(new File(_file).exists()){
      return _file;
    }

    // already an url.
    if(_file.startsWith(JAR_PREFIX) || _file.startsWith(ZIP_PREFIX)){
      return _file;
    }

    // convert to unix path separator so CompositeName can parse it.
    _file = _file.replace('\\', '/');

    // otherwise do some convertion.
    StringBuffer buffer = new StringBuffer(FilenameUtils.getPrefix(_file));

    try{
      int index = FilenameUtils.getPrefixLength(_file);
      CompositeName fileName = new CompositeName(_file.substring(index));
      Enumeration names = fileName.getAll();
      while(names.hasMoreElements()){
        String name = (String)names.nextElement();
        if(name.indexOf("$") != -1){
          name = name.substring(0, name.indexOf("$")) + '.' +
            FilenameUtils.getExtension(name);
        }
        if(name.length() != 0){
          if(buffer.length() != index){
            buffer.append('/');
          }
          buffer.append(name);

          if(!new File(buffer.toString()).exists()){
            String path = FilenameUtils.getFullPath(buffer.toString());
            if(path.endsWith(JAR_EXT)){
              buffer = new StringBuffer(JAR_PREFIX)
                .append(path).append('!').append('/').append(name);
            }else if(path.endsWith(ZIP_EXT)){
              buffer = new StringBuffer(ZIP_PREFIX)
                .append(path).append('!').append('/').append(name);
            }
          }
        }
      }
    }catch(Exception e){
      throw new RuntimeException(e);
    }

    return buffer.toString();
  }
}
