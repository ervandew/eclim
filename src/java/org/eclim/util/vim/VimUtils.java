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
   * Converts the offset into a vim compatible line / column string.
   *
   * @param _fileName The file name.
   * @param _offset The offset in the file.
   * @return The vim compatable string.
   */
  public static String translateOffset (String _fileName, int _offset)
  {
    if(_offset != -1){
      log.debug("TraslateOffset for '" + _fileName + "'.");

      BufferedReader reader = null;
      try{
        int offset = 0;
        int lines = 0;

        FileSystemManager fsManager = VFS.getManager();
        FileObject file = fsManager.resolveFile(_fileName);

        // disable caching (the cache seems to become invalid at some point
        // causing vfs errors).
        fsManager.getFilesCache().clear(file.getFileSystem());

        reader = new BufferedReader(
            new InputStreamReader(file.getContent().getInputStream()));

        String line = null;
        while((line = reader.readLine()) != null){
          lines++;
          int newOffset = offset + line.length() + 1;

          if(newOffset >= _offset){
            return lines + " col " + ((_offset - offset) + 1);
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
}
