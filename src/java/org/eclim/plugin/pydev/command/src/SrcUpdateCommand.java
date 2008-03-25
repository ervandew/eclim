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
package org.eclim.plugin.pydev.command.src;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.command.filter.ErrorFilter;

import org.eclim.logging.Logger;

import org.eclim.util.ProjectUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.python.pydev.builder.pylint.PyLintVisitor;

/**
 * Command to update and optionally validate a python src file.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class SrcUpdateCommand
  extends AbstractCommand
{
  private static final Logger logger = Logger.getLogger(SrcUpdateCommand.class);

  /**
   * {@inheritDoc}
   */
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    String file = _commandLine.getValue(Options.FILE_OPTION);
    String projectName = _commandLine.getValue(Options.PROJECT_OPTION);

    IProject project = ProjectUtils.getProject(projectName);
    IFile ifile = ProjectUtils.getFile(project, file);
    ifile.refreshLocal(IResource.DEPTH_INFINITE, null);

    // validate the src file.
    if(_commandLine.hasOption(Options.VALIDATE_OPTION)){
      PyLintThread pylint = new PyLintThread(
          ifile, ProjectUtils.getDocument(project, file), ifile.getRawLocation());
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
      return ErrorFilter.instance.filter(_commandLine, errors);
    }
    return StringUtils.EMPTY;
  }
}
