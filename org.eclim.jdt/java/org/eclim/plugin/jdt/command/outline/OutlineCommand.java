/**
 * Copyright (C) 2005 - 2018  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.outline;

import java.util.ArrayList;
import java.util.List;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Command to retrieve the outline of a Java file.
 *
 * @author G0dj4ck4l
 */
@Command(
  name = "java_outline",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG"
)
public class OutlineCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    return extractOutlineFromElements(
        JavaUtils
        .getCompilationUnit(
          commandLine.getValue(Options.PROJECT_OPTION),
          commandLine.getValue(Options.FILE_OPTION))
        .getChildren());
  }

  private List<OutlineNode> extractOutlineFromElements(
      IJavaElement[] javaElements)
    throws JavaModelException
  {
    List<OutlineNode> outlineNodes = new ArrayList<OutlineNode>();

    for(IJavaElement javaElement : javaElements) {
      if(javaElement instanceof IMember) {
        if(!javaElement.getElementName().isEmpty()) {
          outlineNodes.add(new OutlineNode(
              OutlineUtil.getSignature((IMember)javaElement),
              OutlineUtil.getMemberFilePosition((IMember)javaElement),
              extractOutlineFromElements(((IMember)javaElement).getChildren())));
        }
      }
    }

    return outlineNodes;
  }
}
