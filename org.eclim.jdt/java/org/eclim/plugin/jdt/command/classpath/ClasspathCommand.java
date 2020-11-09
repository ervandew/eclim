/**
 * Copyright (C) 2005 - 2020  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.classpath;

import java.io.File;

import org.apache.commons.lang3.SystemUtils;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.jdt.util.ClasspathUtils;
import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclim.util.StringUtils;

import org.eclipse.jdt.core.IJavaProject;

/**
 * Command which returns a list of classpath entries.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "java_classpath",
  options =
    "REQUIRED p project ARG," +
    "OPTIONAL d delimiter ARG"
)
public class ClasspathCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String name = commandLine.getValue(Options.PROJECT_OPTION);
    String delim = commandLine.getValue(
        Options.DELIMETER_OPTION, File.pathSeparator);
    IJavaProject javaProject = JavaUtils.getJavaProject(name);

    String[] paths = ClasspathUtils.getClasspath(javaProject);
    return StringUtils.join(paths, delim);
  }
}
