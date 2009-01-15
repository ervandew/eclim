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
package org.eclim.command;

import org.eclim.preference.Preferences;

import org.eclim.util.ProjectUtils;

import org.eclim.util.file.FileUtils;

/**
 * Abstract implmentation of {@link Command}.
 *
 * @author Eric Van Dewoestine
 */
public abstract class AbstractCommand
  implements Command
{
  /**
   * Gets the preferences.
   *
   * @return Preferences.
   */
  public Preferences getPreferences()
  {
    return Preferences.getInstance();
  }

  /**
   * Convenience method which uses the standard project, file, offset, and
   * encoding options to determine the character offset in the file.
   *
   * @param commandLine The command line instance.
   * @return The char offset.
   */
  public int getOffset(CommandLine commandLine)
    throws Exception
  {
    String project = commandLine.getValue(Options.PROJECT_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);
    String encoding = commandLine.getValue(Options.ENCODING_OPTION);
    int offset = Integer.parseInt(
        commandLine.getValue(Options.OFFSET_OPTION));
    file = ProjectUtils.getFilePath(project, file);

    return FileUtils.byteOffsetToCharOffset(file, offset, encoding);
  }
}
