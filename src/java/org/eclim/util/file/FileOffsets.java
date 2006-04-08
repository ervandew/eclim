/**
 * Copyright (c) 2004 - 2006
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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;

import org.eclim.Services;

/**
 * Compiles a list of offsets to line numbers and stores them for quick
 * translation of offset to line number and column.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
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

      List lines = new ArrayList();
      lines.add(Integer.valueOf(0));

      int offset = 0;
      int count = 0;
      char[] buffer = new char[1024];
      while((count = reader.read(buffer, 0, buffer.length)) != -1){
        for(int ii = 0; ii < count; ii++){
          if(buffer[ii] == '\n'){
            offset++;
            lines.add(Integer.valueOf(offset));
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
    int top = offsets.length;
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
