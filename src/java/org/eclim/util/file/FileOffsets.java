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
package org.eclim.util.file;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.ArrayList;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;

import org.eclim.Services;

import org.eclim.util.IOUtils;

/**
 * Compiles a list of offsets to line numbers and stores them for quick
 * translation of offset to line number and column.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class FileOffsets
{
  private Integer[] offsets;

  private FileOffsets ()
  {
  }

  /**
   * Reads the supplied file and compiles a list of offsets.
   *
   * @param _filename The file to compile a list of offsets for.
   * @return The FileOffsets instance.
   */
  public static FileOffsets compile (String _filename)
  {
    FileOffsets offsets = new FileOffsets();
    offsets.compileOffsets(_filename);
    return offsets;
  }

  /**
   * Reads the supplied file and compiles a list of offsets.
   *
   * @param _filename The file to compile a list of offsets for.
   */
  private void compileOffsets (String _filename)
  {
    BufferedReader reader = null;
    try{
      FileSystemManager fsManager = VFS.getManager();
      FileObject file = fsManager.resolveFile(_filename);

      // disable caching (the cache seems to become invalid at some point
      // causing vfs errors).
      //fsManager.getFilesCache().clear(file.getFileSystem());

      if(!file.exists()){
        throw new IllegalArgumentException(
            Services.getMessage("file.not.found", _filename));
      }
      reader = new BufferedReader(
          new InputStreamReader(file.getContent().getInputStream()));

      ArrayList<Integer> lines = new ArrayList<Integer>();
      lines.add(new Integer(0));

      int offset = 0;
      int count = 0;
      char[] buffer = new char[1024];
      while((count = reader.read(buffer, 0, buffer.length)) != -1){
        for(int ii = 0; ii < count; ii++){
          if(buffer[ii] == '\n'){
            offset++;
            lines.add(new Integer(offset));
          }else{
            offset++;
          }
        }
      }

      offsets = (Integer[])lines.toArray(new Integer[lines.size()]);
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
   * @param _offset The offset.
   * @return The line and column int array.
   */
  public int[] offsetToLineColumn (int _offset)
  {
    if(_offset <= 0){
      return new int[]{1,1};
    }

    int bot = -1;
    int top = offsets.length - 1;
    while (top - bot > 1) {
      int mid = (top + bot) / 2;
      if (offsets[mid].intValue() <  _offset){
        bot = mid;
      }else{
        top = mid;
      }
    }
    if(offsets[top].intValue() > _offset){
      top--;
    }
    int line = top + 1;
    int column = 1 + _offset - offsets[top].intValue();
    return new int[]{line, column};
  }

  /**
   * Gets the offset where the supplied line starts.
   *
   * @param _line The line.
   * @return The starting offset.
   */
  public int getLineStart (int _line)
  {
    return offsets[_line - 1].intValue();
  }

  /**
   * Gets the offset where the supplied line ends.
   *
   * @param _line The line.
   * @return The ending offset.
   */
  public int getLineEnd (int _line)
  {
    return offsets[_line].intValue() - 1;
  }
}
