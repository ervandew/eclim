/**
 * Copyright (C) 2005 - 2017  Eric Van Dewoestine
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
package org.eclim.plugin.core.command.history;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;

import org.eclipse.core.internal.localstore.FileSystemResourceManager;

import org.eclipse.core.internal.resources.File;

/**
 * Command to add current file state to the eclipse local history.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "history_add",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG"
)
public class HistoryAddCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String name = commandLine.getValue(Options.PROJECT_OPTION);
    String filename = commandLine.getValue(Options.FILE_OPTION);

    File file = (File)ProjectUtils.getFile(name, filename);

    // update local history
    if (file.exists()){
      FileSystemResourceManager localManager = file.getLocalManager();
      IFileStore store = localManager.getStore(file);
      IFileInfo fileInfo = store.fetchInfo();
      localManager.getHistoryStore()
        .addState(file.getFullPath(), store, fileInfo, false);
    }
    return null;
  }
}
