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

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.command.complete.CodeCompleteResult;

import org.eclim.eclipse.jface.text.DummyTextViewer;

import org.eclim.util.ProjectUtils;

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
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    String project = _commandLine.getValue(Options.PROJECT_OPTION);
    String file = _commandLine.getValue(Options.FILE_OPTION);
    int offset = Integer.parseInt(
        _commandLine.getValue(Options.OFFSET_OPTION));

    IContentAssistProcessor processor =
      getContentAssistProcessor(_commandLine, project, file);

    ITextViewer viewer = getTextViewer(_commandLine, project, file);

    ICompletionProposal[] proposals =
      processor.computeCompletionProposals(viewer, offset);

    ArrayList<CodeCompleteResult> results = new ArrayList<CodeCompleteResult>();

    if(proposals != null){
      for (int ii = 0; ii < proposals.length; ii++){
        if(acceptProposal(proposals[ii])){
          CodeCompleteResult result = new CodeCompleteResult(
              getCompletion(proposals[ii]),
              getDescription(proposals[ii]),
              getShortDescription(proposals[ii]));

          if(!results.contains(result)){
            results.add(result);
          }
        }
      }
    }

    return CodeCompleteFilter.instance.filter(_commandLine, results);
  }

  /**
   * Gets the IContentAssistProcessor to use.
   *
   * @param _commandLine The current command line.
   * @param project The project where the file is located.
   * @param file The file to be processed.
   * @return The IContentAssistProcessor.
   */
  protected abstract IContentAssistProcessor getContentAssistProcessor (
      CommandLine _commandLine, String project, String file)
    throws Exception;

  /**
   * Gets the text viewer passed to the content assist processor.
   *
   * @param _commandLine The current command line.
   * @param project The project the file is in.
   * @param file The file.
   * @return The ITextViewer.
   */
  protected ITextViewer getTextViewer (
      CommandLine _commandLine, String project, String file)
    throws Exception
  {
    int offset = Integer.parseInt(_commandLine.getValue(Options.OFFSET_OPTION));
    return new DummyTextViewer(
        ProjectUtils.getDocument(project, file), offset, 1);
  }

  /**
   * Determines if the supplied proposal will be accepted as a result.
   *
   * @param proposal The ICompletionProposal.
   * @return true if the proposal is accepted, false otherwise.
   */
  protected boolean acceptProposal (ICompletionProposal proposal)
  {
    return true;
  }

  /**
   * Get the completion from the proposal.
   *
   * @param proposal The ICompletionProposal.
   * @return The completion.
   */
  protected String getCompletion (ICompletionProposal proposal)
  {
    return proposal.getDisplayString();
  }

  /**
   * Get the description from the proposal.
   *
   * @param proposal The ICompletionProposal.
   * @return The description.
   */
  protected String getDescription (ICompletionProposal proposal)
  {
    String description = proposal.getAdditionalProposalInfo();
    if(description != null){
      description = description.trim();
    }
    return description;
  }

  /**
   * Get the short description from the proposal.
   *
   * @param proposal The ICompletionProposal.
   * @return The short description.
   */
  protected String getShortDescription (ICompletionProposal proposal)
  {
    return null;
  }
}
