/**
 * Copyright (c) 2005 - 2008
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
package org.eclim.command.archive;

import java.text.SimpleDateFormat;

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
