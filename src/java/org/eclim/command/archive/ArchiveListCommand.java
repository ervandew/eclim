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
package org.eclim.command.archive;

import java.text.Collator;
import java.text.SimpleDateFormat;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.VFS;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

/**
 * Command to list the contents of an archive file.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class ArchiveListCommand
  extends AbstractCommand
{
  private static final SimpleDateFormat DATE_FORMAT =
    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  /**
   * {@inheritDoc}
   */
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    String file = _commandLine.getValue(Options.FILE_OPTION);
    FileSystemManager manager = VFS.getManager();
    FileObject archive = manager.resolveFile(file);
    FileObject[] children = getFiles(archive);
    String[] results = processFiles(children);

    // the vfs file cache isn't very intelligent, so clear it.
    manager.getFilesCache().clear(archive.getFileSystem());

    return StringUtils.join(results, '\n');
  }

  protected FileObject[] getFiles (FileObject archive)
    throws Exception
  {
    return archive.getChildren();
  }

  protected String[] processFiles (FileObject[] files)
    throws Exception
  {
    Arrays.sort(files, new Comparator<FileObject>(){
      private Collator collator =  Collator.getInstance();
      public int compare (FileObject o1, FileObject o2) {
        return collator.compare(
          o1.getName().getBaseName(),
          o2.getName().getBaseName());
      }
    });

    String[] results = new String[files.length];
    for (int ii = 0; ii < files.length; ii++){
      FileObject file = files[ii];
      FileType type = file.getType();
      FileContent content = file.getContent();
      FileName name = file.getName();
      results[ii] = new StringBuffer()
        .append(name.getPath()).append('|')
        .append(name.getBaseName()).append('|')
        .append(file.getURL()).append('|')
        .append(type).append('|')
        .append(type.hasContent() ? content.getSize() : 0).append('|')
        .append(type.hasContent() ?
            formatTime(content.getLastModifiedTime()) : "")
        .toString();
    }
    return results;
  }

  protected String formatTime (long time)
    throws Exception
  {
    return DATE_FORMAT.format(new Date(time));
  }

  protected String formatTime (Date time)
    throws Exception
  {
    return DATE_FORMAT.format(time);
  }
}
