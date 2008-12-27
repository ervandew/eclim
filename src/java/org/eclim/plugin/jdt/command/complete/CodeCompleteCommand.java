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
package org.eclim.plugin.jdt.command.complete;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;

import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;

import org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;

/**
 * Command to handle java code completion requests.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class CodeCompleteCommand
  extends AbstractCommand
{
  private static final Comparator<CodeCompleteResult> COMPLETION_COMPARATOR =
    new CompletionComparator();

  /**
   * {@inheritDoc}
   */
  public String execute(CommandLine commandLine)
    throws Exception
  {
    ArrayList<CodeCompleteResult> results = new ArrayList<CodeCompleteResult>();
    String project = commandLine.getValue(Options.PROJECT_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);
    int offset = getOffset(commandLine);

    ICompilationUnit src = JavaUtils.getCompilationUnit(project, file);

    CompletionProposalCollector collector =
      new CompletionProposalCollector(src);
    src.codeComplete(offset, collector);

    IJavaCompletionProposal[] proposals =
      collector.getJavaCompletionProposals();
    for(int ii = 0; ii < proposals.length; ii++){
      results.add(
          createCompletionResult(collector, ii, proposals[ii]));
    }
    Collections.sort(results, COMPLETION_COMPARATOR);
    return CodeCompleteFilter.instance.filter(commandLine, results);
  }

  /**
   * Create a CodeCompleteResult from the supplied CompletionProposal.
   *
   * @param collector The completion collector.
   * @param index The index of the proposal in the results.
   * @param proposal The proposal.
   *
   * @return The result.
   */
  protected CodeCompleteResult createCompletionResult(
      CompletionProposalCollector collector,
      int index,
      IJavaCompletionProposal proposal)
  {
    int offset = 0;
    int length = 0;
    String completion = null;
    String displayString = proposal.getDisplayString();

    if(proposal instanceof JavaCompletionProposal){
      JavaCompletionProposal lazy = (JavaCompletionProposal)proposal;
      offset = lazy.getReplacementOffset();
      length = lazy.getReplacementLength();
      completion = lazy.getReplacementString();
    }else if(proposal instanceof LazyJavaCompletionProposal){
      LazyJavaCompletionProposal lazy = (LazyJavaCompletionProposal)proposal;
      offset = lazy.getReplacementOffset();
      length = lazy.getReplacementLength();
      completion = lazy.getReplacementString();
    }

    int kind = collector.getProposal(index).getKind();
    switch(kind){
      case CompletionProposal.METHOD_REF:
        // trim off the trailing paren if the method takes any arguments.
        if (displayString.lastIndexOf(')') > displayString.lastIndexOf('(') + 1 &&
            (completion.length() > 0 &&
             completion.charAt(completion.length() - 1) == ')')){
          completion = completion.substring(0, completion.length() - 1);
        }
        break;
      case CompletionProposal.TYPE_REF:
        // trim off package info.
        int idx = completion.lastIndexOf('.');
        if(idx != -1){
          completion = completion.substring(idx + 1);
        }
        break;
    }

    // TODO:
    // hopefully Bram will take my advice to add lazy retrieval of
    // completion 'info' so that I can provide this text without the
    // overhead involved with retrieving it for every completion regardless
    // of whether the user ever views it.
    /*return new CodeCompleteResult(
        kind, completion,
        proposal.getAdditionalProposalInfo(), displayString,
        offset, offset + length);*/
    if("class".equals(completion)){
      kind = CompletionProposal.KEYWORD;
    }
    return new CodeCompleteResult(
        kind, completion, null, displayString, offset, offset + length);
  }
}
