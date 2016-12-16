/**
 * Copyright (C) 2005 - 2016  Eric Van Dewoestine
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
package org.eclim.plugin.core.command.file;

import java.io.File;

import org.eclim.Services;
import org.eclim.annotation.Command;
import org.eclim.command.CommandException;
import org.eclim.command.CommandException.ErrorType;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;
import org.eclim.logging.Logger;
import org.eclim.plugin.core.command.AbstractCommand;
import org.eclim.plugin.core.util.PathUtil;
import org.eclim.plugin.core.util.PathUtilException;

@Command(
      name = "file_delete",
      options =
      "REQUIRED f relativeFilePath ARG," +
      "REQUIRED p project ARG"
)

/**
 * Command to delete a file.
 *
 * The <code>relativeFilePath</code> specifies the path relative to the
 * <code>project</code> where the file/folder should be deleted.
 *
 * If the file is a directory, also all subdirectories will be deleted.
 *
 * Warning: you might have to update the <code>project</code> after calling this
 * command.
 *
 *
 * @author Lukas Roth
 *
 */
public class FileDeleteCommand extends AbstractCommand
{
  private static final Logger logger = Logger.getLogger(FileDeleteCommand.class);

  @Override
  public Object execute(CommandLine commandLine)
      throws Exception
  {
    String relativeFilePath = commandLine.getValue(Options.FILE_OPTION);
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    return fileDelete(relativeFilePath, projectName);
  }

  public Object fileDelete(String relativeFilePath, String projectName)
  {
    try {
      PathUtil.checkPathForEscaping(relativeFilePath);
    } catch (PathUtilException e) {
      return new CommandException(e, ErrorType.CLIENT_ERROR);
    }
    String absoluteFilePath;
    try {
      absoluteFilePath = PathUtil.getAbsolutePath(projectName, relativeFilePath);
    } catch (PathUtilException e) {
      return new CommandException(e, ErrorType.CLIENT_ERROR);
    }
    return deleteFileOnFileSystem(absoluteFilePath, relativeFilePath);
  }

  private Object deleteFileOnFileSystem(String absoluteFilePath, String filePath)
  {
    File file = new File(absoluteFilePath);
    if (!file.exists()) {
      String message = Services.getMessage("file.delete.not.found", filePath);
      logger.error(message);
      return new CommandException(message, ErrorType.CLIENT_ERROR);
    }
    if (file.isDirectory()) {
      org.eclim.util.file.FileUtils.deleteDirectory(file);
      return Services.getMessage("file.delete.directory.deleted", filePath);
    } else {
      if (file.delete()) {
        return Services.getMessage("file.delete.success", filePath);
      } else {
        String message = Services.getMessage("file.delete.error", filePath);
        logger.error(Services.getMessage("file.delete.error", filePath));
        return new CommandException(message, ErrorType.SYSTEM_ERROR);
      }
    }
  }
}
