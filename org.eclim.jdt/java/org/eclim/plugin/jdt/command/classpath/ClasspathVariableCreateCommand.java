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
package org.eclim.plugin.jdt.command.classpath;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclipse.core.runtime.Path;

import org.eclipse.jdt.core.JavaCore;

/**
 * Command to add a classpath variable.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "java_classpath_variable_create",
  options =
    "REQUIRED n name ARG," +
    "REQUIRED p path ARG"
)
public class ClasspathVariableCreateCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String name = commandLine.getValue(Options.NAME_OPTION);
    String path = commandLine.getValue(Options.PATH_OPTION);

    JavaCore.setClasspathVariable(name, new Path(path), null);

    return Services.getMessage("classpath.variable.created", name);
  }
}
