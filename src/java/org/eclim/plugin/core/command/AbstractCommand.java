/**
 * Copyright (C) 2005 - 2011  Eric Van Dewoestine
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
package org.eclim.plugin.core.command;

import org.eclim.command.Command;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.preference.Preferences;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.util.file.FileUtils;

import com.martiansoftware.nailgun.NGContext;

/**
 * Abstract implmentation of {@link Command}.
 *
 * @author Eric Van Dewoestine
 */
public abstract class AbstractCommand
  implements Command
{
  private NGContext context;

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
    if (project == null){
      // some commands use -n for the project name (like all the search commands)
      project = commandLine.getValue(Options.NAME_OPTION);
    }
    String file = commandLine.getValue(Options.FILE_OPTION);
    String encoding = commandLine.getValue(Options.ENCODING_OPTION);
    int offset = Integer.parseInt(
        commandLine.getValue(Options.OFFSET_OPTION));
    file = ProjectUtils.getFilePath(project, file);

    return FileUtils.byteOffsetToCharOffset(file, offset, encoding);
  }

  /**
   * {@inheritDoc}
   * @see Command#getContext()
   */
  public NGContext getContext()
  {
    return context;
  }

  /**
   * {@inheritDoc}
   * @see Command#setContext(NGContext)
   */
  public void setContext(NGContext context)
  {
    this.context = context;
  }

  public void println()
  {
    context.out.println();
  }

  public void println(boolean x)
  {
    context.out.println(x);
  }

  public void println(char x)
  {
    context.out.println(x);
  }

  public void println(char[] x)
  {
    context.out.println(x);
  }

  public void println(double x)
  {
    context.out.println(x);
  }

  public void println(float x)
  {
    context.out.println(x);
  }

  public void println(int x)
  {
    context.out.println(x);
  }

  public void println(long x)
  {
    context.out.println(x);
  }

  public void println(Object x)
  {
    context.out.println(x);
  }

  public void println(String x)
  {
    context.out.println(x);
  }
}
