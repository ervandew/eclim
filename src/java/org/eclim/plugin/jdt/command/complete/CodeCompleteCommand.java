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
package org.eclim.plugin.jdt.command.complete;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;

import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;

import org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaTypeCompletionProposal;

/**
 * Command to handle java code completion requests.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class CodeCompleteCommand
  extends AbstractCommand
{
  private static final Comparator COMPLETION_COMPARATOR =
    new CompletionComparator();

  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
    throws IOException
  {
    List results = new ArrayList();
    try{
      String project = _commandLine.getValue(Options.PROJECT_OPTION);
      String file = _commandLine.getValue(Options.FILE_OPTION);
      int offset = Integer.parseInt(_commandLine.getValue(Options.OFFSET_OPTION));

      ICompilationUnit src = JavaUtils.getCompilationUnit(project, file);

      String filename = src.getResource().getRawLocation().toOSString();

      CompletionProposalCollector collector =
        new CompletionProposalCollector(src);
      src.codeComplete(offset, collector);

      IJavaCompletionProposal[] proposals =
        collector.getJavaCompletionProposals();
      for(int ii = 0; ii < proposals.length; ii++){
        results.add(
            createCompletionResult(filename, collector, ii, proposals[ii]));
      }
      Collections.sort(results, COMPLETION_COMPARATOR);
      return filter(_commandLine, results);
    }catch(Exception e){
      return e;
    }
  }

  /**
   * Create a CodeCompleteResult from the supplied CompletionProposal.
   *
   * @param _filename The filename.
   * @param _collector The completion collector.
   * @param _proposal The proposal.
   *
   * @return The result.
   */
  protected CodeCompleteResult createCompletionResult (
      String _filename,
      CompletionProposalCollector _collector,
      int _index,
      IJavaCompletionProposal _proposal)
  {
    int offset = 0;
    int length = 0;
    String completion = null;
    String displayString = _proposal.getDisplayString();

    if(_proposal instanceof JavaCompletionProposal){
      JavaCompletionProposal lazy = (JavaCompletionProposal)_proposal;
      offset = lazy.getReplacementOffset();
      length = lazy.getReplacementLength();
      completion = lazy.getReplacementString();
    }else if(_proposal instanceof LazyJavaCompletionProposal){
      LazyJavaCompletionProposal lazy = (LazyJavaCompletionProposal)_proposal;
      offset = lazy.getReplacementOffset();
      length = lazy.getReplacementLength();
      completion = lazy.getReplacementString();
    }

    int kind = _collector.getProposal(_index).getKind();
    switch(kind){
      case CompletionProposal.METHOD_REF:
        // trim off the trailing paren if the method takes any arguments.
        if (displayString.lastIndexOf(')') > displayString.lastIndexOf('(') + 1 &&
            completion.charAt(completion.length() - 1) == ')')
        {
          completion = completion.substring(0, completion.length() - 1);
        }
        break;
      case CompletionProposal.TYPE_REF:
        // trim off package info.
        int index = completion.lastIndexOf('.');
        if(index != -1){
          completion = completion.substring(index + 1);
        }
        break;
    }

    // TODO:
    // hopefully Bram will take my advice to add lazy retrieval of
    // completion 'info' so that I can provide this text without the
    // overhead involved with retrieving it for every completion regardless
    // of whether the user ever views it.
    /*return new CodeCompleteResult(
        kind, _filename, completion, displayString,
        _proposal.getAdditionalProposalInfo(),
        offset, offset + length);*/
    return new CodeCompleteResult(
        kind, _filename, completion, displayString,
        null, offset, offset + length);
  }
}
