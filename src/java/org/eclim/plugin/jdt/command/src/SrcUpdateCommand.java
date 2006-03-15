/**
 * Copyright (c) 2004 - 2005
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
package org.eclim.plugin.jdt.command.src;

import java.io.IOException;

import java.util.ArrayList;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclim.util.file.FileUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jdt.core.ICompilationUnit;

import org.eclipse.jdt.core.compiler.IProblem;

/**
 * Command that updates the requested java src file.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class SrcUpdateCommand
  extends AbstractCommand
{
  private static final String IGNORE_WARNINGS =
    "org.eclim.java.validation.ignore.warnings";

  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
    throws IOException
  {
    String file = _commandLine.getValue(Options.FILE_OPTION);
    String projectName = _commandLine.getValue(Options.PROJECT_OPTION);

    try{
      // JavaUtils refreshes the file when getting it.
      ICompilationUnit src = JavaUtils.getCompilationUnit(projectName, file);

      // validate the src file.
      if(_commandLine.hasOption(Options.VALIDATE_OPTION)){
        IProblem[] problems = JavaUtils.getProblems(src);

        IProject project =
          ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        boolean ignoreWarnings = Boolean.valueOf(
            getPreferences().getPreference(project, IGNORE_WARNINGS))
          .booleanValue();

        ArrayList errors = new ArrayList();
        String filename = src.getResource().getRawLocation().toOSString();
        for(int ii = 0; ii < problems.length; ii++){
          int[] lineColumn = FileUtils.offsetToLineColumn(
              filename, problems[ii].getSourceStart());
          if(ignoreWarnings && problems[ii].isWarning()){
            continue;
          }
          errors.add(new Error(
              problems[ii].getMessage(),
              filename,
              lineColumn[0],
              lineColumn[1],
              problems[ii].isWarning()));
        }

        return super.filter(_commandLine,
            (Error[])errors.toArray(new Error[errors.size()]));
      }
    }catch(Exception e){
      return e;
    }
    return "";
  }
}
