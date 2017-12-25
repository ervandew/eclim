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
package org.eclim.plugin.jdt.command.hierarchy;

import java.util.ArrayList;
import java.util.List;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.jdt.util.JavaUtils;
import org.eclim.plugin.jdt.util.TypeInfo;
import org.eclim.plugin.jdt.util.TypeUtils;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;

/**
 * Command to retrieve the hierarchy of a class or interface.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "java_hierarchy",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "REQUIRED o offset ARG," +
    "REQUIRED e encoding ARG"
)
public class HierarchyCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String project = commandLine.getValue(Options.PROJECT_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);

    ICompilationUnit src = JavaUtils.getCompilationUnit(project, file);
    int offset = commandLine.getIntValue(Options.OFFSET_OPTION);
    if (offset != -1){
      offset = getOffset(commandLine);
    }

    IType type = TypeUtils.getType(src, offset);

    HierarchyNode hierarchy = new HierarchyNode(type, createChildNodes(type));

    return hierarchy;
  }

  private List<HierarchyNode> createChildNodes(IType type)
    throws Exception
  {
    ArrayList<HierarchyNode> nodes = new ArrayList<HierarchyNode>();

    TypeInfo parentClassInfo = TypeUtils.getSuperClass(type);
    String jlo = "java.lang.Object";
    if (parentClassInfo != null){
      IType parentClass = parentClassInfo.getType();
      if(!jlo.equals(JavaUtils.getFullyQualifiedName(parentClass))){
        nodes.add(new HierarchyNode(parentClass, createChildNodes(parentClass)));
      }
    }

    IType[] parentInterfaces = TypeUtils.getSuperInterfaces(type);
    if(parentInterfaces.length != 0){
      for(IType parentInterface : parentInterfaces){
        nodes.add(new HierarchyNode(
              parentInterface, createChildNodes(parentInterface)));
      }
    }

    return nodes;
  }
}
