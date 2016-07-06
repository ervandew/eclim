/**
 * Copyright (C) 2005 - 2014  Eric Van Dewoestine
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

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;

import org.eclim.plugin.core.command.complete.AbstractCodeCompleteCommand;
import org.eclim.plugin.core.command.complete.CodeCompleteResult;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;

import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal;

import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;

/**
 * Command to handle java code completion requests.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "java_complete",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "REQUIRED o offset ARG," +
    "REQUIRED e encoding ARG," +
    "REQUIRED l layout ARG"
)
public class CodeCompleteCommand
  extends AbstractCodeCompleteCommand
{
  protected static final Comparator<CodeCompleteResult> COMPLETION_COMPARATOR =
    new CompletionComparator();

  private ThreadLocal<CompletionProposalCollector> collector =
    new ThreadLocal<CompletionProposalCollector>();

  @Override
  protected Object getResponse(List<CodeCompleteResult> results)
  {
    CompletionProposalCollector collector = this.collector.get();
    return new CodeCompleteResponse(
        results, collector.getError(), collector.getImports());
  }

  @Override
  protected List<CodeCompleteResult> getCompletionResults(
      CommandLine commandLine, String project, String file, int offset)
    throws Exception
  {
    ICompilationUnit src = JavaUtils.getCompilationUnit(project, file);

    CompletionProposalCollector collector =
      new CompletionProposalCollector(src);
    src.codeComplete(offset, collector);

    IJavaCompletionProposal[] proposals =
      collector.getJavaCompletionProposals();
    ArrayList<CodeCompleteResult> results = new ArrayList<CodeCompleteResult>();
    for(IJavaCompletionProposal proposal : proposals){
      results.add(createCompletionResult(proposal));
    }
    Collections.sort(results, COMPLETION_COMPARATOR);

    this.collector.set(collector);

    return results;
  }

  /**
   * Create a CodeCompleteResult from the supplied CompletionProposal.
   *
   * @param proposal The proposal.
   *
   * @return The result.
   */
  protected CodeCompleteResult createCompletionResult(
      IJavaCompletionProposal proposal)
    throws Exception
  {
    String completion = null;
    String menu = proposal.getDisplayString();
    Integer offset = null;

    int kind = -1;
    if(proposal instanceof JavaCompletionProposal){
      JavaCompletionProposal lazy = (JavaCompletionProposal)proposal;
      completion = lazy.getReplacementString();
      offset = lazy.getReplacementOffset();
    }else if(proposal instanceof LazyJavaCompletionProposal){
      LazyJavaCompletionProposal lazy = (LazyJavaCompletionProposal)proposal;
      completion = lazy.getReplacementString();
      offset = lazy.getReplacementOffset();
      Method getProposal = LazyJavaCompletionProposal.class
        .getDeclaredMethod("getProposal");
      getProposal.setAccessible(true);
      CompletionProposal cproposal = (CompletionProposal)getProposal.invoke(lazy);
      if (cproposal != null){
        kind = cproposal.getKind();
      }
    }

    switch(kind){
      case CompletionProposal.METHOD_REF:
        int length = completion.length();
        if (length == 0){
          break;
        }
        if (completion.charAt(length - 1) == ';'){
          completion = completion.substring(0, length - 1);
          length--;
        }
        // trim off the trailing paren if the method takes any arguments.
        // Note: using indexOf instead of lastIndexOf to account for groovy
        // completion menu text.
        if (menu.indexOf(')') > menu.indexOf('(') + 1 &&
            completion.charAt(length - 1) == ')')
        {
          completion = completion.substring(0, completion.lastIndexOf('(') + 1);
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

    if("class".equals(completion)){
      kind = CompletionProposal.KEYWORD;
    }

    String type = "";
    switch(kind){
      case CompletionProposal.TYPE_REF:
        type = CodeCompleteResult.TYPE;
        break;
      case CompletionProposal.FIELD_REF:
      case CompletionProposal.LOCAL_VARIABLE_REF:
        type = CodeCompleteResult.VARIABLE;
        break;
      case CompletionProposal.METHOD_REF:
        type = CodeCompleteResult.FUNCTION;
        break;
      case CompletionProposal.KEYWORD:
        type = CodeCompleteResult.KEYWORD;
        break;
    }

    // TODO:
    // hopefully Bram will take my advice to add lazy retrieval of
    // completion 'info' so that I can provide this text without the
    // overhead involved with retrieving it for every completion regardless
    // of whether the user ever views it.
    /*return new CodeCompleteResult(
        kind, completion, menu, proposal.getAdditionalProposalInfo());*/
    CodeCompleteResult result = new CodeCompleteResult(completion, menu, menu, type, offset);
    result.setRelevance(proposal.getRelevance());

    return result;
  }
}
