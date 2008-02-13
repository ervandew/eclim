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

import org.apache.commons.lang.StringUtils;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
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
  /**
   * {@inheritDoc}
   */
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    String file = _commandLine.getValue(Options.FILE_OPTION);
    FileSystemManager manager = VFS.getManager();
    FileObject archive = manager.resolveFile(file);
    FileObject[] children = archive.getChildren();
    String[] results = new String[children.length];
    for (int ii = 0; ii < children.length; ii++){
      results[ii] = new StringBuffer()
        .append(children[ii].getName().getBaseName()).append('|')
        .append(children[ii].getURL()).append('|')
        .append(children[ii].getType())
        .toString();
    }
    return StringUtils.join(results, '\n');
  }
}
