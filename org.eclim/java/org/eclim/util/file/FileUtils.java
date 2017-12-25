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
package org.eclim.util.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.Enumeration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.CompositeName;

import org.apache.commons.lang.StringUtils;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;

import org.eclim.util.IOUtils;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Utilities for working w/ files and commons vfs.
 *
 * @author Eric Van Dewoestine
 */
public class FileUtils
{
  public static final String JAR_PREFIX = "jar://";
  public static final String ZIP_PREFIX = "zip://";
  public static final String JAR_EXT = ".jar";
  public static final String ZIP_EXT = ".zip";
  public static final char UNIX_SEPARATOR = '/';
  public static final char WINDOWS_SEPARATOR = '\\';
  public static final String UTF8 = "utf-8";

  /**
   * Converts the supplied byte offset in the specified file to the
   * corresponding char offset for that file using the supplied file encoding.
   *
   * @param filename The absolute path to the file.
   * @param byteOffset The byte offset to be converted.
   * @param encoding The encoding of the file.  If null, defaults to utf-8.
   *
   * @return The char offset.
   */
  public static int byteOffsetToCharOffset(
      String filename, int byteOffset, String encoding)
  {
    try{
      FileSystemManager fsManager = VFS.getManager();
      FileObject file = fsManager.resolveFile(filename.replace("%", "%25"));

      return byteOffsetToCharOffset(
          file.getContent().getInputStream(), byteOffset, encoding);
    }catch(FileSystemException fse){
      throw new RuntimeException(fse);
    }
  }

  /**
   * Converts the supplied byte offset in the specified file to the
   * corresponding char offset for that file using the supplied file encoding.
   *
   * @param in InputStream for the file contents.
   * @param byteOffset The byte offset to be converted.
   * @param encoding The encoding of the file.  If null, defaults to utf-8.
   *
   * @return The char offset.
   */
  public static int byteOffsetToCharOffset(
      InputStream in, int byteOffset, String encoding)
  {
    if (encoding == null){
      encoding = UTF8;
    }

    BufferedInputStream bin = null;
    try{
      byte[] bytes = new byte[byteOffset];
      bin = new BufferedInputStream(in);
      bin.read(bytes, 0, bytes.length);
      String value = new String(bytes, encoding);

      return value.length();
    }catch(IOException ioe){
      throw new RuntimeException(ioe);
    }finally{
      IOUtils.closeQuietly(bin);
    }
  }

  /**
   * Converts the supplied char offset into an int array where the first element
   * is the line number and the second is the column number.
   *
   * @param filename The file to translate the offset for.
   * @param offset The offset.
   * @return The line and column int array.
   */
  public static int[] offsetToLineColumn(String filename, int offset)
  {
    FileOffsets offsets = FileOffsets.compile(filename);
    return offsets.offsetToLineColumn(offset);
  }

  /**
   * Converts the supplied char offset into an int array where the first element
   * is the line number and the second is the column number.
   *
   * @param in The InputStream to compile a list of offsets for.
   * @param offset The offset.
   * @return The line and column int array.
   */
  public static int[] offsetToLineColumn(InputStream in, int offset)
  {
    FileOffsets offsets = FileOffsets.compile(in);
    return offsets.offsetToLineColumn(offset);
  }

  /**
   * Obtains a matcher to run the supplied pattern on the specified file.
   *
   * @param pattern The regex pattern
   * @param file The path to the file.
   * @return The Matcher.
   */
  public static Matcher matcher(Pattern pattern, String file)
  {
    FileInputStream is = null;
    try{
      is = new FileInputStream(file);
      String contents = IOUtils.toString(is);
      return pattern.matcher(contents);
    }catch(IOException ioe){
      throw new RuntimeException(ioe);
    }finally{
      IOUtils.closeQuietly(is);
    }
  }

  /**
   * Translates a file name that does not conform to the standard url file
   * format.
   * <p>
   * Main purpose is to convert paths like:<br>
   * <code>/opt/sun-jdk-1.5.0.05/src.zip/javax/swing/Spring.java</code><br>
   * to<br>
   * <code>zip:file:///opt/sun-jdk-1.5.0.05/src.zip!/javax/swing/Spring.java</code>
   * </p>
   *
   * @param file The file to translate.
   * @return The translated file.
   */
  public static String toUrl(String file)
  {
    file = file.replace('\\', '/');

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
              path = path.substring(0, path.length() - 1);
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
   * @param parts The path parts to concat.
   * @return The concatenated paths.
   */
  public static String concat(String head, String... parts)
  {
    IPath path = Path.fromOSString(head);
    for (String part : parts){
      path = path.append(part);
    }
    return path.toOSString().replace('\\', '/');
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
  public static String getBaseName(String path)
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
  public static String getExtension(String path)
  {
    String ext = Path.fromOSString(path).getFileExtension();
    if (ext == null){
      return StringUtils.EMPTY;
    }
    return ext;
  }

  public static String getPath(String path)
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
   * Gets the full path from the supplied path by removing any trailing path
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
  public static String getFullPath(String path)
  {
    IPath p = Path.fromOSString(path);
    if (!p.hasTrailingSeparator()){
      p = p.uptoSegment(p.segmentCount() - 1);
    }
    return p.addTrailingSeparator().toOSString();
  }

  /**
   * Gets the directory path from the supplied path by removing the last path
   * segment.
   * <pre>
   * FileUtils.getDirName("/a/b/c/") :  "/a/b/"
   * FileUtils.getDirName("/a/b/c") :  "/a/b/"
   * FileUtils.getDirName("/a/b/c.txt") :  "/a/b/"
   * </pre>
   *
   * @param path The path.
   * @return The full path.
   */
  public static String getDirName(String path)
  {
    IPath p = Path.fromOSString(path);
    p = p.uptoSegment(p.segmentCount() - 1);
    return p.addTrailingSeparator().toOSString();
  }

  /**
   * Gets the file name minus the extension from the supplied path.
   * <pre>
   * FileUtils.getFileName("/a/b/c/") :  ""
   * FileUtils.getFileName("/a/b/c") :  "c"
   * FileUtils.getFileName("/a/b/c.txt") :  "c"
   * </pre>
   *
   * @param path The path.
   * @return The file name without the extension.
   */
  public static String getFileName(String path)
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
  public static String removeExtension(String path)
  {
    return Path.fromOSString(path).removeFileExtension().toOSString();
  }

  /**
   * Adds the trailing slash if it doesn't exists.
   * <pre>
   * FileUtils.addTrailingSlash("/a/b/c/") :  "/a/b/c/"
   * FileUtils.addTrailingSlash("/a/b/c") :  "/a/b/c/"
   * </pre>
   *
   * @param path The path.
   * @return The path with a trailing slash.
   */
  public static String addTrailingSlash(String path)
  {
    if (!path.endsWith("/")){
      return path += '/';
    }
    return path;
  }

  /**
   * Removes the trailing slash if it exists.
   * <pre>
   * FileUtils.removeTrailingSlash("/a/b/c/") :  "/a/b/c"
   * FileUtils.removeTrailingSlash("/a/b/c") :  "/a/b/c"
   * </pre>
   *
   * @param path The path.
   * @return The path with any trialing slash removed.
   */
  public static String removeTrailingSlash(String path)
  {
    if (path.endsWith("/")){
      return path.substring(0, path.length() - 1);
    }
    return path;
  }

  /**
   * Convert all path separators for the given path to unix style separators.
   *
   * @param path The path.
   * @return The updated path.
   */
  public static String separatorsToUnix(String path)
  {
    if (path == null || path.indexOf(WINDOWS_SEPARATOR) == -1) {
      return path;
    }
    return path.replace(WINDOWS_SEPARATOR, UNIX_SEPARATOR);
  }

  /**
   * Deletes a directory and all of its contents.
   *
   * @param dir The directory to delete.
   */
  public static void deleteDirectory(File dir)
  {
    if(!dir.exists()){
      return;
    }

    for(File f : dir.listFiles()){
      if(f.isDirectory()){
        deleteDirectory(f);
      }else{
        f.delete();
      }
    }
    dir.delete();
  }
}
