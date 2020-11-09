/**
 * Copyright (C) 2005 - 2020  Eric Van Dewoestine
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

import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.ArrayList;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;

import org.eclim.Services;

import org.eclim.util.IOUtils;

/**
 * Compiles a list of char offsets to line numbers and stores them for quick
 * translation of offset to line number and column.
 *
 * @author Eric Van Dewoestine
 */
public class FileOffsets
{
  private Integer[] offsets;
  private String[] multiByteLines;

  private FileOffsets ()
  {
  }

  /**
   * Reads the supplied file and compiles a list of offsets.
   *
   * @param filename The file to compile a list of offsets for.
   * @return The FileOffsets instance.
   */
  public static FileOffsets compile(String filename)
  {
    try{
      FileSystemManager fsManager = VFS.getManager();
      FileObject file = fsManager.resolveFile(filename.replace("%", "%25"));

      // disable caching (the cache seems to become invalid at some point
      // causing vfs errors).
      //fsManager.getFilesCache().clear(file.getFileSystem());

      if(!file.exists()){
        throw new IllegalArgumentException(
            Services.getMessage("file.not.found", filename));
      }
      return compile(file.getContent().getInputStream());
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }

  /**
   * Reads the supplied input stream and compiles a list of offsets.
   *
   * @param in The InputStream to compile a list of offsets for.
   * @return The FileOffsets instance.
   */
  public static FileOffsets compile(InputStream in)
  {
    FileOffsets offsets = new FileOffsets();
    offsets.compileOffsets(in);
    return offsets;
  }

  /**
   * Reads the supplied input stream and compiles a list of offsets.
   *
   * @param in The InputStream to compile a list of offsets for.
   */
  private void compileOffsets(InputStream in)
  {
    BufferedReader reader = null;
    try{
      reader = new BufferedReader(new InputStreamReader(in));

      ArrayList<Integer> lines = new ArrayList<Integer>();
      lines.add(Integer.valueOf(0));
      ArrayList<String> byteLines = new ArrayList<String>();
      byteLines.add(null);

      int offset = 0;
      String line = null;
      while((line = reader.readLine()) != null){
        offset += line.length();
        lines.add(Integer.valueOf(offset));
        if (line.length() != line.getBytes().length){
          byteLines.add(line);
        }else{
          byteLines.add(null);
        }
      }

      offsets = (Integer[])lines.toArray(new Integer[lines.size()]);
      multiByteLines = (String[])byteLines.toArray(new String[byteLines.size()]);
    }catch(Exception e){
      throw new RuntimeException(e);
    }finally{
      IOUtils.closeQuietly(reader);
    }
  }

  /**
   * Converts the supplied offset into an int array where the first element is
   * the line number and the second is the column number.
   *
   * @param offset The offset.
   * @return The line and column int array.
   */
  public int[] offsetToLineColumn(int offset)
  {
    if(offset <= 0){
      return new int[]{1, 1};
    }

    int bot = -1;
    int top = offsets.length - 1;
    while (top - bot > 1) {
      int mid = (top + bot) / 2;
      if (offsets[mid].intValue() <  offset){
        bot = mid;
      }else{
        top = mid;
      }
    }
    if(offsets[top].intValue() > offset){
      top--;
    }
    int line = top + 1;
    int column = 1 + offset - offsets[top].intValue();
    String value = multiByteLines.length > line ? multiByteLines[line] : null;
    if (value != null){
      column = value.substring(0, column).getBytes().length;
    }
    return new int[]{line, column};
  }

  /**
   * Gets the offset where the supplied line starts.
   *
   * @param line The line.
   * @return The starting offset.
   */
  public int getLineStart(int line)
  {
    return offsets[line - 1].intValue();
  }

  /**
   * Gets the offset where the supplied line ends.
   *
   * @param line The line.
   * @return The ending offset.
   */
  public int getLineEnd(int line)
  {
    if (offsets.length == line){
      return offsets[offsets.length - 1].intValue();
    }
    return offsets[line].intValue() - 1;
  }
}
