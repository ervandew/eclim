/**
 * Copyright (C) 2005 - 2008  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.include;

import java.util.ArrayList;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;

import org.eclipse.jdt.core.compiler.IProblem;

/**
 * Command which reports all unused imports for the specified file.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class UnusedImportsCommand
  extends AbstractCommand
{
  private static final int[] UNUSED_IMPORTS = {IProblem.UnusedImport};

  /**
   * {@inheritDoc}
   */
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    String file = _commandLine.getValue(Options.FILE_OPTION);
    String projectName = _commandLine.getValue(Options.PROJECT_OPTION);

    ICompilationUnit src = JavaUtils.getCompilationUnit(projectName, file);

    IProblem[] problems = JavaUtils.getProblems(src, UNUSED_IMPORTS);
    ArrayList<String> results = new ArrayList<String>();
    for(int ii = 0; ii < problems.length; ii++){
      IJavaElement element = src.getElementAt(problems[ii].getSourceStart());
      if(element != null){
        results.add(element.getElementName());
      }
    }
    return UnusedImportsFilter.instance.filter(_commandLine, results);
  }
}
