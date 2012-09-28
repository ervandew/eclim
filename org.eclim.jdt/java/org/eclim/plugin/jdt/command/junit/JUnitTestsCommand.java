/**
 * Copyright (C) 2012  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.junit;

import java.util.ArrayList;
import java.util.Collections;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;

import org.eclipse.jdt.junit.JUnitCore;

/**
 * Command which returns a list of all available junit tests class names.
 *
 * @author Eric Van Dewoestine
 */
@Command(name = "java_junit_tests", options = "REQUIRED p project ARG")
public class JUnitTestsCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    IJavaProject javaProject = JavaUtils.getJavaProject(projectName);

    ArrayList<String> names = new ArrayList<String>();
    IType[] types = JUnitCore.findTestTypes(javaProject, null);
    for (IType type : types) {
      names.add(type.getFullyQualifiedName());
    }
    Collections.sort(names);

    return names;
  }
}
