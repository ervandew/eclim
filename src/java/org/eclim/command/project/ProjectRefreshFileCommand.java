/**
 * Copyright (C) 2005 - 2009  Eric Van Dewoestine
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
package org.eclim.command.project;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.util.ProjectUtils;
import org.eclim.util.StringUtils;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;

import org.eclipse.core.internal.localstore.FileSystemResourceManager;

import org.eclipse.core.internal.resources.File;

/**
 * Command to refresh a file in a project.
 *
 * @author Eric Van Dewoestine
 */
public class ProjectRefreshFileCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public String execute(CommandLine commandLine)
    throws Exception
  {
    String name = commandLine.getValue(Options.PROJECT_OPTION);
    String filename = commandLine.getValue(Options.FILE_OPTION);

    // the act of getting the file refreshes it.
    File file = (File)ProjectUtils.getFile(name, filename);

    // update local history
    if (file.exists()){
      FileSystemResourceManager localManager = file.getLocalManager();
      IFileStore store = localManager.getStore(file);
      IFileInfo fileInfo = store.fetchInfo();
      localManager.getHistoryStore()
        .addState(file.getFullPath(), store, fileInfo, false);
    }

    return StringUtils.EMPTY;
  }
}
