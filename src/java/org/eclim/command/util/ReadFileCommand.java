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
package org.eclim.command.util;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import org.apache.commons.lang.SystemUtils;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

/**
 * Command that reads a file from a commons vfs compatable path.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ReadFileCommand
  extends AbstractCommand
{
  private static final String URI_PREFIX = "file://";
  private static final Pattern WIN_PATH = Pattern.compile("^/[a-zA-Z]:/.*");

  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
  {
    InputStream in = null;
    OutputStream out = null;
    FileSystemManager fsManager = null;
    try{
      String file = _commandLine.getValue(Options.FILE_OPTION);

      fsManager = VFS.getManager();
      FileObject fileObject = fsManager.resolveFile(file);
      FileObject tempFile = fsManager.resolveFile(
          SystemUtils.JAVA_IO_TMPDIR + fileObject.getName().getPath());

      // disable caching (the cache seems to become invalid at some point
      // causing vfs errors).
      //fsManager.getFilesCache().clear(fileObject.getFileSystem());
      //fsManager.getFilesCache().clear(tempFile.getFileSystem());

      // NOTE: FileObject.getName().getPath() does not include the drive
      // information.
      String path = tempFile.getName().getURI().substring(URI_PREFIX.length());
      // account for windows uri which has an extra '/' in front of the drive
      // letter (file:///C:/blah/blah/blah).
      if (WIN_PATH.matcher(path).matches()){
        path = path.substring(1);
      }

      if(!tempFile.exists()){
        tempFile.createFile();

        in = fileObject.getContent().getInputStream();
        out = tempFile.getContent().getOutputStream();
        IOUtils.copy(in, out);

        new File(path).deleteOnExit();
      }

      return path;
    }catch(Exception e){
      return e;
    }finally{
      IOUtils.closeQuietly(in);
      IOUtils.closeQuietly(out);
    }
  }
}
