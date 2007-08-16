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
package org.eclim.util.file;

import java.io.File;
import java.io.FileInputStream;

import java.util.Enumeration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.CompositeName;

import org.apache.commons.lang.StringUtils;

import org.eclim.util.IOUtils;
import org.eclim.util.ProjectUtils;

import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Utilities for working w/ files and commons vfs.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class FileUtils
{
  public static final String JAR_PREFIX = "jar://";
  public static final String ZIP_PREFIX = "zip://";
  public static final String JAR_EXT = ".jar";
  public static final String ZIP_EXT = ".zip";
  public static final char UNIX_SEPARATOR = '/';
  public static final char WINDOWS_SEPARATOR = '\\';

  /**
   * Gets a project relative file.
   *
   * @param _project The project.
   * @param _file The file.
   * @return The File.
   */
  public static File getProjectRelativeFile (IProject _project, String _file)
    throws Exception
  {
    return new File(concat(ProjectUtils.getPath(_project), _file));
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
    FileOffsets offsets = FileOffsets.compile(_filename);
    return offsets.offsetToLineColumn(_offset);
  }

  /**
   * Obtains a matcher to run the supplied pattern on the specified file.
   *
   * @param _pattern The regex pattern
   * @param _file The path to the file.
   * @return The Matcher.
   */
  public static Matcher matcher (Pattern _pattern, String _file)
    throws Exception
  {
    FileInputStream is = null;
    try{
      is = new FileInputStream(_file);
      String contents = IOUtils.toString(is);
      return _pattern.matcher(contents);
    }finally{
      IOUtils.closeQuietly(is);
    }
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
    String file = _file.replace('\\', '/');

    // if the path points to a real file, return it.
    if(new File(file).exists()){
      return file;
    }

    // already an url.
    if(file.startsWith(JAR_PREFIX) || file.startsWith(ZIP_PREFIX)){
      return file;
    }

    // otherwise do some conversion.
    StringBuffer buffer = new StringBuffer();
    try{
      CompositeName fileName = new CompositeName(file);
      Enumeration<String> names = fileName.getAll();
      while(names.hasMoreElements()){
        String name = names.nextElement();
        if(name.indexOf("$") != -1){
          name = name.substring(0, name.indexOf("$")) + '.' + getExtension(name);
        }
        if(name.length() != 0){
          buffer.append('/').append(name);

          if(!new File(buffer.toString()).exists()){
            String path = getFullPath(buffer.toString());
            if(path.endsWith("/") || path.endsWith("\\")){
              path = path.substring(0, path.length() -1);
            }
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

  /**
   * Concatenates the given head path with the supplied tail path to form a new
   * path.
   *
   * @param head The head path.
   * @param tail The tail path.
   * @return The concatenated paths.
   */
  public static String concat (String head, String tail)
  {
    return Path.fromOSString(head).append(tail).toOSString();
  }

  /**
   * Gets the base name of the supplied path.
   * <pre>
   * FileUtils.getBaseName("/a/b/c/") :  "c"
   * FileUtils.getBaseName("/a/b/c") :  "c"
   * FileUtils.getBaseName("/a/b/c.txt") :  "c.txt"
   * </pre>
   *
   * @param path The path.
   * @return The base name of the path.
   */
  public static String getBaseName (String path)
  {
    IPath p = Path.fromOSString(path);
    return p.segment(p.segmentCount() - 1);
  }

  /**
   * Gets the file extension from the supplied path.
   * <pre>
   * FileUtils.getExtension("/a/b/c/") :  ""
   * FileUtils.getExtension("/a/b/c") :  ""
   * FileUtils.getExtension("/a/b/c.txt") :  "txt"
   * </pre>
   *
   * @param path The path.
   * @return The file extension.
   */
  public static String getExtension (String path)
  {
    String ext = Path.fromOSString(path).getFileExtension();
    if (ext == null){
      return StringUtils.EMPTY;
    }
    return ext;
  }

  /**
   *
   * <pre>
   * FileUtils.getPath("/a/b/c/") :  ""
   * FileUtils.getPath("/a/b/c") :  ""
   * FileUtils.getPath("/a/b/c.txt") :  "txt"
   * </pre>
   *
   * @param path The path.
   * @return
   */
  public static String getPath (String path)
  {
    IPath p = null;
    int index = path.indexOf(':');
    if (index != -1){
      p = new Path(path.substring(index + 1));
    }else{
      p = new Path(path);
    }
    if (!p.hasTrailingSeparator()){
      p = p.uptoSegment(p.segmentCount() - 1);
    }
    return p.makeRelative().addTrailingSeparator().toOSString();
  }

  /**
   * Gets the full path from the supplied path by removing any trialing path
   * segments that do not have a trailing separator.
   * <pre>
   * FileUtils.getFullPath("/a/b/c/") :  "/a/b/c/"
   * FileUtils.getFullPath("/a/b/c") :  "/a/b/"
   * FileUtils.getFullPath("/a/b/c.txt") :  "/a/b/"
   * </pre>
   *
   * @param path The path.
   * @return The full path.
   */
  public static String getFullPath (String path)
  {
    IPath p = Path.fromOSString(path);
    if (!p.hasTrailingSeparator()){
      p = p.uptoSegment(p.segmentCount() - 1);
    }
    return p.addTrailingSeparator().toOSString();
  }

  /**
   * Gets the file name minus the extension from the supplied path.
   * <pre>
   * FileUtils.getFullPath("/a/b/c/") :  ""
   * FileUtils.getFullPath("/a/b/c") :  "c"
   * FileUtils.getFullPath("/a/b/c.txt") :  "c"
   * </pre>
   *
   * @param path The path.
   * @return The file name without the extension.
   */
  public static String getFileName (String path)
  {
    IPath p = Path.fromOSString(path);
    if (p.hasTrailingSeparator()){
      return StringUtils.EMPTY;
    }
    return p.removeFileExtension().segment(p.segmentCount() - 1);
  }

  /**
   * Removes the extension from the supplied file name.
   * <pre>
   * FileUtils.removeExtension("/a/b/c/") :  "/a/b/c/"
   * FileUtils.removeExtension("/a/b/c") :  "/a/b/c"
   * FileUtils.removeExtension("/a/b/c.txt") :  "/a/b/c"
   * </pre>
   *
   * @param path The path.
   * @return The path with the extension removed.
   */
  public static String removeExtension (String path)
  {
    return Path.fromOSString(path).removeFileExtension().toOSString();
  }

  /**
   * Convert all path separators for the given path to unix style separators.
   *
   * @param path The path.
   * @return The updated path.
   */
  public static String separatorsToUnix (String path)
  {
    if (path == null || path.indexOf(WINDOWS_SEPARATOR) == -1) {
      return path;
    }
    return path.replace(WINDOWS_SEPARATOR, UNIX_SEPARATOR);
  }
}
