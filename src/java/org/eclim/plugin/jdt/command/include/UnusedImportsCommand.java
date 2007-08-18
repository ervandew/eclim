/**
 * Copyright (c) 2005 - 2006
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
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
