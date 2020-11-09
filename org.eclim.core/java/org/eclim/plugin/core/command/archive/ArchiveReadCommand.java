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
package org.eclim.plugin.core.command.archive;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.regex.Pattern;

import org.apache.commons.lang3.SystemUtils;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.util.IOUtils;

/**
 * Command to read a file from a commons vfs compatable path.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "archive_read",
  options = "REQUIRED f file ARG"
)
public class ArchiveReadCommand
  extends AbstractCommand
{
  private static final String URI_PREFIX = "file://";
  private static final Pattern WIN_PATH = Pattern.compile("^/[a-zA-Z]:/.*");

  @Override
  public String execute(CommandLine commandLine)
    throws Exception
  {
    InputStream in = null;
    OutputStream out = null;
    FileSystemManager fsManager = null;
    try{
      String file = commandLine.getValue(Options.FILE_OPTION);

      fsManager = VFS.getManager();
      FileObject fileObject = fsManager.resolveFile(file.replace("%", "%25"));
      FileObject tempFile = fsManager.resolveFile(
          SystemUtils.JAVA_IO_TMPDIR + "/eclim/" +
          fileObject.getName().getPath().replace("%", "%25"));

      // the vfs file cache isn't very intelligent, so clear it.
      fsManager.getFilesCache().clear(fileObject.getFileSystem());
      fsManager.getFilesCache().clear(tempFile.getFileSystem());

      // NOTE: FileObject.getName().getPath() does not include the drive
      // information.
      String path = tempFile.getName().getURI().substring(URI_PREFIX.length());
      // account for windows uri which has an extra '/' in front of the drive
      // letter (file:///C:/blah/blah/blah).
      if (WIN_PATH.matcher(path).matches()){
        path = path.substring(1);
      }

      //if(!tempFile.exists()){
        tempFile.createFile();

        in = fileObject.getContent().getInputStream();
        out = tempFile.getContent().getOutputStream();
        IOUtils.copy(in, out);

        new File(path).deleteOnExit();
      //}

      return path;
    }finally{
      IOUtils.closeQuietly(in);
      IOUtils.closeQuietly(out);
    }
  }
}
