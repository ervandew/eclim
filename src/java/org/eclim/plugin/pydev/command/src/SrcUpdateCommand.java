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
package org.eclim.plugin.pydev.command.src;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

import org.apache.log4j.Logger;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.util.ProjectUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.python.pydev.builder.pylint.PyLintVisitor;

/**
 * Command to update and optionally validate a python src file.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class SrcUpdateCommand
  extends AbstractCommand
{
  private static final Logger logger = Logger.getLogger(SrcUpdateCommand.class);

  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
  {
    try{
      String file = _commandLine.getValue(Options.FILE_OPTION);
      String projectName = _commandLine.getValue(Options.PROJECT_OPTION);

      IProject project = ProjectUtils.getProject(projectName);
      IFile ifile = ProjectUtils.getFile(project, file);
      ifile.refreshLocal(IResource.DEPTH_INFINITE, null);

      // validate the src file.
      if(_commandLine.hasOption(Options.VALIDATE_OPTION)){
        PyLintThread pylint = new PyLintThread(
            ifile, ProjectUtils.getDocument(file), ifile.getRawLocation());
        pylint.start();
        pylint.join();

        ArrayList errors = new ArrayList();

IMarker[] markers = ifile.findMarkers(
    PyLintVisitor.PYLINT_PROBLEM_MARKER, true, IResource.DEPTH_ZERO);
for (int ii = 0; ii < markers.length; ii++){
  logger.info("### marker = " + markers[ii]);
  logger.info("### message = " + markers[ii].getAttribute(IMarker.MESSAGE));
  logger.info("### line = " + markers[ii].getAttribute(IMarker.LINE_NUMBER));
}
        return super.filter(_commandLine,
            (Error[])errors.toArray(new Error[errors.size()]));
      }
    }catch(Exception e){
      return e;
    }
    return StringUtils.EMPTY;
  }
}
