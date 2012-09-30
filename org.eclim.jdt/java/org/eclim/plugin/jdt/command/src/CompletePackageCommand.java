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
package org.eclim.plugin.jdt.command.src;

import java.text.Collator;

import java.util.ArrayList;
import java.util.Collections;

import org.eclim.annotation.Command;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;

/**
 * Command to provide possible completions for a package name.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "java_complete_package",
  options =
    "REQUIRED p project ARG," +
    "OPTIONAL n name ARG"
)
public class CompletePackageCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String project = commandLine.getValue(Options.PROJECT_OPTION);
    String name = commandLine.getValue(Options.NAME_OPTION);

    IJavaProject javaProject = JavaUtils.getJavaProject(project);
    IPackageFragment[] packs = javaProject.getPackageFragments();
    ArrayList<String> results = new ArrayList<String>();
    for (IPackageFragment pack : packs){
      if (pack.getKind() != IPackageFragmentRoot.K_SOURCE){
        continue;
      }
      String packName = pack.getElementName();
      if (packName.length() > 0 &&
          !results.contains(packName) &&
          (name == null || packName.startsWith(name)))
      {
        results.add(packName);
      }
    }
    Collections.sort(results, Collator.getInstance());
    return results;
  }
}
