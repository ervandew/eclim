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
import java.util.LinkedList;
import java.util.List;

import org.eclim.Services;
import org.eclim.annotation.Command;
import org.eclim.command.CommandException;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;
import org.eclim.logging.Logger;
import org.eclim.plugin.core.command.AbstractCommand;
import org.eclim.plugin.core.util.PathUtil;
import org.eclim.plugin.core.util.PathUtilException;

@Command(
    name = "file_list",
    options =
    "REQUIRED f relativeFilePath ARG," +
    "REQUIRED p project ARG," +
    "OPTIONAL r recursiveFlag ARG"
)

/**
 * Command to get a list of all files and folders in the folder
 * {@code relativeFilePath} which is a relative path to the project
 * {@code project}.
 *
 * Example response:
 * 
 * myFolder/
 * mySecFolder/
 * myFile.txt
 *
 *
 * If the {@code recursiveFlag} argument is set to true the command traverses
 * all the folders inside the {@code relativeFilePath} recursively and returns
 * all the folders inclusive all subfolders and subfiles.
 *
 * Example response with {@code recursiveFlag} set to true:
 * 
 * myFolder/
 * myFolder/mySubFolder/
 * myFolder/mySubFolder/mySubFile.txt
 * mySecFolder/
 * myFile.txt
 *
 * @author Lukas Roth
 *
 */
public class FileListCommand extends AbstractCommand
{
  private static final Logger logger = Logger.getLogger(FileListCommand.class);
  private List<String> result;

  @Override
  public Object execute(CommandLine commandLine)
      throws Exception
  {
    String relativeFilePath = commandLine.getValue(Options.FILE_OPTION);
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    String recursiveFlag = commandLine.getValue(Options.RECURSIVE_OPTION);
    boolean recursive = Boolean.parseBoolean(recursiveFlag);
    try {
      return fileList(projectName, relativeFilePath, recursive);
    } catch (FileListCommandException e) {
      return new CommandException(e, CommandException.ErrorType.CLIENT_ERROR);
    }
  }

  public List<String> fileList(String projectName, String relativeFilePath,
      boolean recursive)
      throws FileListCommandException
  {
    validatePath(relativeFilePath);
    File baseFile = getRootPath(projectName, relativeFilePath);
    if (!baseFile.exists()) {
      throw new FileListCommandException(
          Services.getMessage("file.list.no.file", relativeFilePath, projectName));
    }
    result = new LinkedList<String>();
    traverseChildNodes(baseFile, null, recursive);
    return result;
  }

  private void validatePath(String path)
      throws FileListCommandException
  {
    try {
      PathUtil.checkPathForEscaping(path);
    } catch (PathUtilException e) {
      throw new FileListCommandException(
          Services.getMessage("file.path.error.illegal.path", path), e);
    }
  }

  private void traverseChildNodes(File baseFile, String path, boolean recursive)
  {
    File[] childNodes = baseFile.listFiles();
    for (File child : childNodes) {
      String childPath = (path == null) ? child.getName() :
          (path + "/" + child.getName());
      if (child.isFile()) {
        result.add(childPath);
      } else if (child.isDirectory()) {
        result.add(childPath + "/");
        if (recursive) {
          traverseChildNodes(child, childPath, recursive);
        }
      }
    }
  }

  private File getRootPath(String projectName, String relativeFilePath)
      throws FileListCommandException
  {
    try {
      return new File(PathUtil.getAbsolutePath(projectName, relativeFilePath));
    } catch (PathUtilException e) {
      throw new FileListCommandException(
          Services.getMessage("file.list.absolute.path.error", projectName), e);
    }
  }

}
