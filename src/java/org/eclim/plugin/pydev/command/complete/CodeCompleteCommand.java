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
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
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
    int offset = _commandLine.getIntValue(Options.OFFSET_OPTION);

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
