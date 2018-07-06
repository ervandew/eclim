/**
 * Copyright (C) 2013 - 2018 Eric Van Dewoestine
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
package org.eclim.plugin.pydev.command.search;

import java.io.File;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.util.file.Position;

import org.eclipse.core.resources.IProject;

import org.eclipse.jface.text.IDocument;

import org.python.pydev.ast.item_pointer.ItemPointer;

import org.python.pydev.ast.refactoring.AbstractPyRefactoring;
import org.python.pydev.ast.refactoring.IPyRefactoring;
import org.python.pydev.ast.refactoring.IPyRefactoring2;
import org.python.pydev.ast.refactoring.RefactoringRequest;

import org.python.pydev.core.docutils.PySelection;

import org.python.pydev.editor.PyEdit;

import org.python.pydev.parser.visitors.scope.ASTEntry;

import org.python.pydev.plugin.nature.PythonNature;

import org.python.pydev.shared_core.string.CoreTextSelection;

import org.python.pydev.shared_core.structure.Tuple;

/**
 * Command to handle scala search requests.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "python_search",
  options =
    "REQUIRED n project ARG," +
    "REQUIRED f file ARG," +
    "REQUIRED o offset ARG," +
    "REQUIRED l length ARG," +
    "REQUIRED e encoding ARG," +
    "OPTIONAL x context ARG"
)
public class SearchCommand
  extends AbstractCommand
{
  public static final String CONTEXT_DECLARATIONS = "declarations";
  public static final String CONTEXT_REFERENCES = "references";

  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String projectName = commandLine.getValue(Options.NAME_OPTION);
    String fileName = commandLine.getValue(Options.FILE_OPTION);
    int length = commandLine.getIntValue(Options.LENGTH_OPTION);
    int offset = getOffset(commandLine);

    IProject project = ProjectUtils.getProject(projectName);
    IDocument doc = ProjectUtils.getDocument(project, fileName);
    final File file = new File(ProjectUtils.getFilePath(project, fileName));

    PythonNature nature = PythonNature.getPythonNature(project);
    PySelection selection = new PySelection(
        doc, new CoreTextSelection(doc, offset, length));

    // needed for findAllOccurrences
    PyEdit pyEdit = new PyEdit(){
      public File getEditorFile() {
        return file;
      }
    };
    RefactoringRequest request = new RefactoringRequest(
        file, selection, null, nature, pyEdit);

    ArrayList<Position> results = new ArrayList<Position>();

    String context = commandLine.getValue(Options.CONTEXT_OPTION);
    // find references
    if (CONTEXT_REFERENCES.equals(context)){
      // for some reason the pydev project's path isn't initialized properly
      // leading to findAllOccurrences only finding results from the current
      // file. This seems to be plenty fast for my reasonably sized project, so
      // hopefully it won't be a bottleneck for others.
      PythonNature pythonNature = PythonNature.getPythonNature(project);
      pythonNature.rebuildPath();

      request.fillInitialNameAndOffset();
      IPyRefactoring2 pyRefactoring = (IPyRefactoring2)
        AbstractPyRefactoring.getPyRefactoring();
      Map<Tuple<String,File>, HashSet<ASTEntry>> refs =
        pyRefactoring.findAllOccurrences(request);
      for (Tuple<String,File> tuple : refs.keySet()){
        for (ASTEntry entry : refs.get(tuple)){
          Position position = Position.fromLineColumn(
            tuple.o2.toString().replace('\\', '/'),
            entry.getName(),
            entry.node.beginLine,
            entry.node.beginColumn);
          if (!results.contains(position)){
            results.add(position);
          }
        }
      }

    // find declaration
    }else{
      IPyRefactoring pyRefactoring = AbstractPyRefactoring.getPyRefactoring();
      ItemPointer[] defs = pyRefactoring.findDefinition(request);

      for (ItemPointer item : defs){
        if (item.file == null || item.file.toString().endsWith(".so")){
          continue;
        }
        Position position = Position.fromLineColumn(
          item.file.toString().replace('\\', '/'),
          item.definition.value,
          item.start.line + 1,
          item.start.column + 1);
        if (!results.contains(position)){
          results.add(position);
        }
      }
    }

    Collections.sort(results);
    return results;
  }
}
