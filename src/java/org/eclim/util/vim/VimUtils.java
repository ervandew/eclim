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
package org.eclim.util.vim;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;

import org.eclim.util.file.FileUtils;
import org.eclim.util.file.Position;

/**
 * Utility functions for vim filters.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class VimUtils
{
  private static final Log log = LogFactory.getLog(VimUtils.class);

  /**
   * Converts the position into a vim compatible line / column string.
   *
   * @param _position The position instance.
   * @return The vim compatable string.
   */
  public static String translateLineColumn (Position _position)
  {
    if(_position.getOffset() != -1){
      String fileName = _position.getFilename();
      int fileOffset = _position.getOffset();
      log.debug("TraslateLineColumn for '" + fileName + "'.");

      BufferedReader reader = null;
      try{
        int offset = 0;
        int lines = 0;

        FileSystemManager fsManager = VFS.getManager();
        FileObject file = fsManager.resolveFile(fileName);

        // disable caching (the cache seems to become invalid at some point
        // causing vfs errors).
        fsManager.getFilesCache().clear(file.getFileSystem());

        reader = new BufferedReader(
            new InputStreamReader(file.getContent().getInputStream()));

        String line = null;
        while((line = reader.readLine()) != null){
          lines++;
          int newOffset = offset + line.length() + 1;

          if(newOffset >= fileOffset){
            return lines + " col " + ((fileOffset - offset) + 1);
          }
          offset = newOffset;
        }
      }catch(Exception e){
        throw new RuntimeException(e);
      }finally{
        IOUtils.closeQuietly(reader);
      }
    }
    return "1 col 1";
  }

  /**
   * Converts the position into a vim compatible start / end line string.
   *
   * @param _position The position instance.
   * @return The vim compatable string.
   */
  public static String translateStartEnd (Position _position)
  {
    if(_position.getOffset() != -1){
      String fileName = _position.getFilename();
      int fileOffset = _position.getOffset();
      int length = _position.getLength();

      log.debug("TraslateStartEnd for '" + fileName + "'.");

      BufferedReader reader = null;
      try{
        int offset = 0;
        int lines = 0;
        int start = 0;

        FileSystemManager fsManager = VFS.getManager();
        FileObject file = fsManager.resolveFile(fileName);

        // disable caching (the cache seems to become invalid at some point
        // causing vfs errors).
        fsManager.getFilesCache().clear(file.getFileSystem());

        reader = new BufferedReader(
            new InputStreamReader(file.getContent().getInputStream()));

        String line = null;
        while((line = reader.readLine()) != null){
          lines++;
          int newOffset = offset + line.length() + 1;
          if(newOffset >= (fileOffset + length)){
            return fileName + '|' + start + '|' + lines;
          }else if(newOffset >= fileOffset && start == 0){
            start = lines;
          }
          offset = newOffset;
        }
      }catch(Exception e){
        throw new RuntimeException(e);
      }finally{
        IOUtils.closeQuietly(reader);
      }
    }
    return _position.getFilename() + "|0|0";
  }
}
