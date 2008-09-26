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
package org.eclim.plugin.pydev.command.definition;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.command.filter.LocationFilter;

import org.eclim.plugin.pydev.util.PyDevUtils;

import org.eclim.util.ProjectUtils;

import org.eclim.util.file.FileUtils;
import org.eclim.util.file.Location;

import org.eclipse.core.resources.IProject;

import org.eclipse.jface.text.IDocument;

import org.python.pydev.core.docutils.PySelection;

import org.python.pydev.editor.PyEdit;

import org.python.pydev.editor.model.ItemPointer;

import org.python.pydev.editor.refactoring.PyRefactoring;
import org.python.pydev.editor.refactoring.RefactoringRequest;

import org.python.pydev.plugin.nature.PythonNature;

/**
 * Command to find the definition of the element at the specified location in
 * the supplied file.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class FindDefinitionCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    String file = _commandLine.getValue(Options.FILE_OPTION);
    String projectName = _commandLine.getValue(Options.PROJECT_OPTION);
    int offset = getOffset(_commandLine);

    IProject project = ProjectUtils.getProject(projectName);

    IDocument document = ProjectUtils.getDocument(project, file);
    PythonNature nature = PythonNature.getPythonNature(project);
    PyEdit edit = PyDevUtils.getEditor(project, file);
    PySelection selection =
      new PySelection(document, offset);

    PyRefactoring refactor = new PyRefactoring();
    File theFile = new File(
        FileUtils.concat(ProjectUtils.getPath(project), file));
    RefactoringRequest request = new RefactoringRequest(
        theFile, selection, null, nature, edit);
    ItemPointer[] results = refactor.findDefinition(request);
    List locations = new ArrayList();
    if(results != null){
      for (int ii = 0; ii < results.length; ii++){
        locations.add(new Location(
              results[ii].file.toString(),
              results[ii].start.line + 1,
              results[ii].start.column + 1));
      }
    }

    return LocationFilter.instance.filter(_commandLine, locations);
  }
}
