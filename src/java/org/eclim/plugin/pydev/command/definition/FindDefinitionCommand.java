/**
 * Copyright (c) 2005 - 2008
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
    int offset = _commandLine.getIntValue(Options.OFFSET_OPTION);

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
