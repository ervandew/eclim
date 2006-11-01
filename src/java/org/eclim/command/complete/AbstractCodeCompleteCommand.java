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
package org.eclim.command.complete;

import java.util.ArrayList;
import java.util.List;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.command.complete.CodeCompleteResult;

import org.eclim.eclipse.jface.text.DummyTextViewer;

import org.eclim.util.ProjectUtils;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;

/**
 * Abstract command for code completion.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public abstract class AbstractCodeCompleteCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
  {
    try{
      String project = _commandLine.getValue(Options.PROJECT_OPTION);
      String file = _commandLine.getValue(Options.FILE_OPTION);
      int offset = Integer.parseInt(
          _commandLine.getValue(Options.OFFSET_OPTION));

      IContentAssistProcessor processor =
        getContentAssistProcessor(project, file);

      ITextViewer viewer = getTextViewer(project, file, offset);

      ICompletionProposal[] proposals =
        processor.computeCompletionProposals(viewer, offset);

      List results = new ArrayList();
      for (int ii = 0; ii < proposals.length; ii++){
        String description = null;
        if(proposals[ii].getAdditionalProposalInfo() != null){
          description = proposals[ii].getAdditionalProposalInfo().trim();
        }

        String completion = proposals[ii].getDisplayString();
        int index = completion.indexOf(" - ");
        if(index != -1){
          completion = completion.substring(0, index);
        }

        CodeCompleteResult result =
          new CodeCompleteResult(completion, description, null);
        if(!results.contains(result)){
          results.add(result);
        }
      }

      return filter(_commandLine, results);
    }catch(Exception e){
      return e;
    }
  }

  /**
   * Gets the IContentAssistProcessor to use.
   *
   * @param project The project where the file is located.
   * @param file The file to be processed.
   * @return The IContentAssistProcessor.
   */
  protected abstract IContentAssistProcessor getContentAssistProcessor (
      String project, String file)
    throws Exception;

  /**
   * Gets the text viewer passed to the content assist processor.
   *
   * @param project The project the file is in.
   * @param file The file.
   * @param offset The offset in the file.
   * @return The ITextViewer.
   */
  protected ITextViewer getTextViewer (String project, String file, int offset)
    throws Exception
  {
    return new DummyTextViewer(ProjectUtils.getDocument(file), offset, 1);
  }
}
