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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

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
      name = "file_save",
      options =
        "REQUIRED f relativeFilePath ARG," +
        "REQUIRED p project ARG," +
        "REQUIRED c content ARG"
)
/**
 * Command to save a file with <code>content</code> c.
 *
 * The content can either be a String or a InputStream.
 *
 * The <code>relativeFilePath</code> specifies the path relative to the
 * <code>project</code> where the <code>content</code> should be saved.
 *
 * If there is no file at the specified location the file gets created
 * (including parent directories). If there is already a file the content of the
 * file will be overwritten.
 *
 * Warning: you might have to update the <code>project</code> after calling this
 * command.
 *
 * @author Lukas Roth
 *
 */
public class FileSaveCommand extends AbstractCommand
{
  private static final Logger logger = Logger.getLogger(FileSaveCommand.class);

  @Override
  public Object execute(CommandLine commandLine)
      throws Exception
  {
    String relativeFilePath = commandLine.getValue(Options.FILE_OPTION);
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    Object fileIn = commandLine.getRawValue(Options.CONTENT_OPTION);
    InputStream fileStream;
    if (fileIn == null) {
      fileStream = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
    } else if (fileIn instanceof String) {
      fileStream = new ByteArrayInputStream(
          ((String) fileIn).getBytes(StandardCharsets.UTF_8));
    } else if (fileIn instanceof InputStream) {
      fileStream = (InputStream) fileIn;
    } else {
      String message = Services.getMessage("file.save.content.wrong.type");
      logger.error(message);
      return new CommandException(message, ErrorType.SYSTEM_ERROR);
    }
    return fileSave(relativeFilePath, projectName, fileStream);
  }

  public Object fileSave(String relativeFilePath, String projectName,
      InputStream fileContent)
  {
    try {
      PathUtil.checkPathForEscaping(relativeFilePath);
    } catch (PathUtilException e) {
      logger.error("File path is not valid", e);
      return new CommandException(e, ErrorType.CLIENT_ERROR);
    }
    String absoluteFilePath;
    try {
      absoluteFilePath = PathUtil.getAbsolutePath(projectName, relativeFilePath);
    } catch (PathUtilException e) {
      logger.error("Could not get the absolute path", e);
      return new CommandException(e, ErrorType.CLIENT_ERROR);
    }
    try {
      writeToFileSystem(absoluteFilePath, fileContent);
      return Services.getMessage("file.save.success", relativeFilePath);
    } catch (IOException e) {
      String message = Services.getMessage("file.save.error.io", relativeFilePath);
      logger.error(message, e);
      return new CommandException(message, ErrorType.SYSTEM_ERROR);
    }
  }

  private void writeToFileSystem(String absoluteFilePath, InputStream fileContent)
      throws IOException
  {
    File file = new File(absoluteFilePath);
    if (file.exists()) {
      logger.debug("Overwriting file at " + absoluteFilePath);
    }
    file.getParentFile().mkdirs();
    copyInputStreamToFile(fileContent, file);
  }

  // copied from
  // http://stackoverflow.com/questions/43157/easy-way-to-write-contents-of-a-java-inputstream-to-an-outputstream
  private void copyInputStreamToFile(InputStream in, File file)
      throws IOException
  {
    OutputStream out = new FileOutputStream(file);
    byte[] buf = new byte[1024];
    int len;
    while ((len = in.read(buf)) > 0) {
      out.write(buf, 0, len);
    }
    out.close();
    in.close();
  }
}
