/**
 * Copyright (C) 2005 - 2020  Eric Van Dewoestine
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
package org.eclim.plugin.core.command.complete;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.eclipse.jface.text.DummyTextViewer;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.command.complete.CodeCompleteResult;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclipse.jface.text.ITextViewer;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;

import org.eclipse.jface.text.source.ISourceViewer;

/**
 * Abstract command for code completion.
 *
 * @author Eric Van Dewoestine
 */
public abstract class AbstractCodeCompleteCommand
  extends AbstractCommand
{
  private static String COMPACT = "compact";
  //private static String STANDARD = "standard";

  @Override
  public Object execute(final CommandLine commandLine)
    throws Exception
  {
    String project = commandLine.getValue(Options.PROJECT_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);
    int offset = getOffset(commandLine);

    List<CodeCompleteResult> results =
      getCompletionResults(commandLine, project, file, offset);

    String layout = commandLine.getValue(Options.LAYOUT_OPTION);
    if(COMPACT.equals(layout) && results.size() > 0){
      results = compact(results);
    }
    Collections.sort(results);
    return getResponse(results);
  }

  protected Object getResponse(List<CodeCompleteResult> results)
  {
    return results;
  }

  /**
   * Gets the list of CodeCompletionResult objects.
   *
   * @param commandLine The current command line.
   * @param project The project name.
   * @param file  The project relative file name.
   * @param offset The offset in the file.
   * @return The completion results.
   */
  protected List<CodeCompleteResult> getCompletionResults(
      CommandLine commandLine, String project, String file, int offset)
  {
    ICompletionProposal[] proposals =
      getCompletionProposals(commandLine, project, file, offset);

    ArrayList<CodeCompleteResult> results = new ArrayList<CodeCompleteResult>();

    if(proposals != null){
      for (ICompletionProposal proposal : proposals){
        if(acceptProposal(proposal)){
          CodeCompleteResult ccresult = createCodeCompletionResult(proposal);
          if(ccresult != null && !results.contains(ccresult)){
            results.add(ccresult);
          }
        }
      }
    }
    return results;
  }

  /**
   * Gets the ICompletionProposal results.
   *
   * @param commandLine The current command line.
   * @param project The project name.
   * @param file  The project relative file name.
   * @param offset The offset in the file.
   * @return The completion results.
   */
  protected ICompletionProposal[] getCompletionProposals(
      CommandLine commandLine, String project, String file, int offset)
  {
    IContentAssistProcessor processor =
      getContentAssistProcessor(commandLine, project, file);

    ITextViewer viewer = getTextViewer(commandLine, project, file);

    if (processor != null && viewer != null){
      return processor.computeCompletionProposals(viewer, offset);
    }
    return new ICompletionProposal[0];
  }

  /**
   * Gets the IContentAssistProcessor to use.
   *
   * @param commandLine The current command line.
   * @param project The project where the file is located.
   * @param file The file to be processed.
   * @return The IContentAssistProcessor.
   */
  protected IContentAssistProcessor getContentAssistProcessor(
      CommandLine commandLine, String project, String file)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Gets the text viewer passed to the content assist processor.
   *
   * @param commandLine The current command line.
   * @param project The project the file is in.
   * @param file The file.
   * @return The ISourceViewer.
   */
  protected ISourceViewer getTextViewer(
      CommandLine commandLine, String project, String file)
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
   * Constructs a new CodeCompleteResult for the supplied proposal.
   *
   * @param proposal The ICompletionProposal.
   * @return The CodeCompleteResult.
   */
  protected CodeCompleteResult createCodeCompletionResult(
      ICompletionProposal proposal)
  {
    return new CodeCompleteResult(
        getCompletion(proposal), getMenu(proposal), getInfo(proposal));
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
   * Get the menu text from the proposal.
   *
   * @param proposal The ICompletionProposal.
   * @return The menu text.
   */
  protected String getMenu(ICompletionProposal proposal)
  {
    return StringUtils.EMPTY;
  }

  /**
   * Get the info details from the proposal.
   *
   * @param proposal The ICompletionProposal.
   * @return The info.
   */
  protected String getInfo(ICompletionProposal proposal)
  {
    String info = proposal.getAdditionalProposalInfo();
    if(info != null){
      info = info.trim();
    }else{
      info = StringUtils.EMPTY;
    }
    return info;
  }

  /**
   * Compact overloaded methods into one completion result.
   *
   * @param results The original completion results.
   * @return The compacted results.
   */
  protected ArrayList<CodeCompleteResult> compact(
      List<CodeCompleteResult> results)
  {
    ArrayList<CodeCompleteResult> compactResults =
      new ArrayList<CodeCompleteResult>();

    CodeCompleteResult first = results.get(0);
    String lastWord = first.getCompletion();
    String lastType = first.getType();
    ArrayList<CodeCompleteResult> overloaded = new ArrayList<CodeCompleteResult>();

    for(CodeCompleteResult result : results){
      if (!result.getCompletion().equals(lastWord) ||
          !result.getType().equals(lastType))
      {
        compactResults.add(compactOverloaded(overloaded));
        overloaded.clear();
      }
      overloaded.add(result);
      lastWord = result.getCompletion();
      lastType = result.getType();
    }

    if (overloaded.size() > 0){
      compactResults.add(compactOverloaded(overloaded));
    }

    return compactResults;
  }

  private CodeCompleteResult compactOverloaded(
      ArrayList<CodeCompleteResult> overloaded)
  {
    CodeCompleteResult r = overloaded.get(0);
    if (overloaded.size() == 1){
      return r;
    }

    StringBuffer info = new StringBuffer();
    for (CodeCompleteResult o : overloaded){
      if(info.length() > 0){
        info.append("<br/>");
      }
      info.append(o.getMenu());
    }

    return new CodeCompleteResult(
        r.getCompletion(),
        "Overloaded, see preview...",
        info.toString(),
        r.getType());
  }
}
