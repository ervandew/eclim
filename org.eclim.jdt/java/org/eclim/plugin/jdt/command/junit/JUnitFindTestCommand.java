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

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclim.util.file.Position;

import org.eclipse.core.resources.IProject;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;

import org.eclipse.jdt.internal.junit.launcher.JUnit4TestFinder;

/**
 * Command to handle execution of junit tests.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "java_junit_find_test",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "REQUIRED o offset ARG," +
    "REQUIRED e encoding ARG"
)
public class JUnitFindTestCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);
    int offset = getOffset(commandLine);

    IProject project = ProjectUtils.getProject(projectName);
    IJavaProject javaProject = JavaUtils.getJavaProject(project);
    JUnit4TestFinder finder = new JUnit4TestFinder();

    ICompilationUnit src = JavaUtils.getCompilationUnit(javaProject, file);
    ICompilationUnit result = null;
    if (finder.isTest(src.getTypes()[0])){
      result = JUnitUtils.findClass(javaProject, src.getTypes()[0]);
      if (result == null){
        return Services.getMessage("junit.testing.class.not.found");
      }
    } else {
      result = JUnitUtils.findTest(javaProject, src.getTypes()[0]);
      if (result == null){
        return Services.getMessage("junit.testing.test.not.found");
      }
    }

    IType resultType = result.getTypes()[0];
    String name = resultType.getElementName();
    ISourceReference ref = resultType;
    ISourceRange docRange = resultType.getJavadocRange();

    IJavaElement element = src.getElementAt(offset);
    if(element != null && element.getElementType() == IJavaElement.METHOD){
      IMethod method = null;
      if (finder.isTest(src.getTypes()[0])){
        method = JUnitUtils.findClassMethod(result, (IMethod)element);
      }else{
        method = JUnitUtils.findTestMethod(result, (IMethod)element);
      }
      if (method != null){
        name = method.getElementName();
        ref = method;
        docRange = method.getJavadocRange();
      }
    }

    String lineDelim = result.findRecommendedLineSeparator();
    int docLength = docRange != null ?
      docRange.getLength() + lineDelim.length() : 0;
    return Position.fromOffset(
        result.getResource().getLocation().toOSString(), name,
        ref.getSourceRange().getOffset() + docLength, 0);
  }
}
