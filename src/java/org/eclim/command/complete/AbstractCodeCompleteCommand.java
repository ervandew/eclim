/**
 * Copyright (C) 2005 - 2009  Eric Van Dewoestine
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

import org.eclipse.swt.widgets.Display;

/**
 * Abstract command for code completion.
 *
 * @author Eric Van Dewoestine
 */
public abstract class AbstractCodeCompleteCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public String execute(final CommandLine commandLine)
    throws Exception
  {
    final String project = commandLine.getValue(Options.PROJECT_OPTION);
    final String file = commandLine.getValue(Options.FILE_OPTION);
    final int offset = getOffset(commandLine);

    final ICompletionProposal[][] result = new ICompletionProposal[1][];
    Display.getDefault().syncExec(new Runnable(){
      public void run()
      {
        try{
          IContentAssistProcessor processor =
            getContentAssistProcessor(commandLine, project, file);

          ITextViewer viewer = getTextViewer(commandLine, project, file);
          result[0] = processor.computeCompletionProposals(viewer, offset);
        }catch(Exception e){
          throw new RuntimeException(e);
        }
      }
    });

    ICompletionProposal[] proposals = result[0];

    ArrayList<CodeCompleteResult> results = new ArrayList<CodeCompleteResult>();

    if(proposals != null){
      for (int ii = 0; ii < proposals.length; ii++){
        if(acceptProposal(proposals[ii])){
          CodeCompleteResult ccresult = new CodeCompleteResult(
              getCompletion(proposals[ii]),
              getDescription(proposals[ii]),
              getShortDescription(proposals[ii]));

          if(!results.contains(ccresult)){
            results.add(ccresult);
          }
        }
      }
    }

    return CodeCompleteFilter.instance.filter(commandLine, results);
  }

  /**
   * Gets the IContentAssistProcessor to use.
   *
   * @param commandLine The current command line.
   * @param project The project where the file is located.
   * @param file The file to be processed.
   * @return The IContentAssistProcessor.
   */
  protected abstract IContentAssistProcessor getContentAssistProcessor(
      CommandLine commandLine, String project, String file)
    throws Exception;

  /**
   * Gets the text viewer passed to the content assist processor.
   *
   * @param commandLine The current command line.
   * @param project The project the file is in.
   * @param file The file.
   * @return The ITextViewer.
   */
  protected ITextViewer getTextViewer(
      CommandLine commandLine, String project, String file)
    throws Exception
  {
    int offset = getOffset(commandLine);
    return new DummyTextViewer(
        ProjectUtils.getDocument(project, file), offset, 1);
  }

  /**
   * Determines if the supplied proposal will be accepted as a result.
   *
   * @param proposal The ICompletionProposal.
   * @return true if the proposal is accepted, false otherwise.
   */
  protected boolean acceptProposal(ICompletionProposal proposal)
  {
    return true;
  }

  /**
   * Get the completion from the proposal.
   *
   * @param proposal The ICompletionProposal.
   * @return The completion.
   */
  protected String getCompletion(ICompletionProposal proposal)
  {
    return proposal.getDisplayString();
  }

  /**
   * Get the description from the proposal.
   *
   * @param proposal The ICompletionProposal.
   * @return The description.
   */
  protected String getDescription(ICompletionProposal proposal)
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
  protected String getShortDescription(ICompletionProposal proposal)
  {
    return null;
  }
}
