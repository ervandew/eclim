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
package org.eclim.plugin.jdt.command.src;

import java.util.ArrayList;

import org.eclim.annotation.Command;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclim.util.StringUtils;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;

/**
 * Command to retrieve the project's root package names.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "java_package_names",
  options = "REQUIRED p project ARG"
)
public class PackageNamesCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   * @see org.eclim.command.Command#execute(CommandLine)
   */
  public String execute(CommandLine commandLine)
    throws Exception
  {
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    IJavaProject javaProject = JavaUtils.getJavaProject(projectName);

    ArrayList<String> names = new ArrayList<String>();
    IPackageFragmentRoot[] roots = javaProject.getPackageFragmentRoots();
    for (IPackageFragmentRoot root : roots){
      if (root.getKind() == IPackageFragmentRoot.K_SOURCE){
        for (IJavaElement child : root.getChildren()){
          String name = child.getElementName();
          if (!name.equals("") && !names.contains(name)){
            names.add(name);
          }
        }
      }
    }

    return StringUtils.join(names, "\n");
  }
}
