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
package org.eclim.plugin.pydev.command.complete;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.command.complete.CodeCompleteFilter;
import org.eclim.command.complete.CodeCompleteResult;

import org.eclim.eclipse.jface.text.DummyTextViewer;

import org.eclim.util.ProjectUtils;

import org.eclipse.core.resources.IProject;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;

import org.eclipse.jface.text.contentassist.ICompletionProposal;

import org.python.pydev.core.IPythonNature;

import org.python.pydev.editor.codecompletion.CompletionRequest;
import org.python.pydev.editor.codecompletion.PyCodeCompletion;
import org.python.pydev.editor.codecompletion.PyCodeCompletionUtils;

import org.python.pydev.plugin.nature.PythonNature;

/**
 * Command to perform python code completion.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class CodeCompleteCommand
  extends AbstractCommand
{
  private static final PyCodeCompletion PY_CC = new PyCodeCompletion();

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
    ITextViewer viewer =
      new DummyTextViewer(document, offset, 1);

    IPythonNature nature = PythonNature.getPythonNature(project);
    CompletionRequest request = new CompletionRequest(
        new File(file), nature, document, offset, PY_CC);

    List results = new ArrayList();
    ICompletionProposal[] proposals = PyCodeCompletionUtils.onlyValidSorted(
        PY_CC.getCodeCompletionProposals(viewer, request),
        request.qualifier, request.isInCalltip);
    for (int ii = 0; ii < proposals.length; ii++){
      ICompletionProposal proposal = proposals[ii];
      String description = proposal.getAdditionalProposalInfo().trim();
      String shortDescription = proposal.getDisplayString();
      String completion = shortDescription;

      int open = completion.indexOf('(');
      int close = completion.indexOf(')');
      if(close > open + 1){
        completion = completion.substring(0, open + 1);
      }

      CodeCompleteResult result =
        new CodeCompleteResult(completion, description, shortDescription);
      if(!results.contains(result)){
        results.add(result);
      }
    }

    return CodeCompleteFilter.instance.filter(_commandLine, results);
  }
}
