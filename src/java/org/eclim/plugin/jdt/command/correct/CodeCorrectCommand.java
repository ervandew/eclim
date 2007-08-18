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
package org.eclim.plugin.jdt.command.correct;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import org.eclim.Services;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclipse.jdt.core.ICompilationUnit;

import org.eclipse.jdt.core.compiler.IProblem;

import org.eclipse.jdt.internal.ui.text.correction.AssistContext;

import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;

/**
 * Handles requests for code correction.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class CodeCorrectCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
  {
    try{
      String file = _commandLine.getValue(Options.FILE_OPTION);
      String projectName = _commandLine.getValue(Options.PROJECT_OPTION);
      int line = _commandLine.getIntValue(Options.LINE_OPTION);
      int offset = _commandLine.getIntValue(Options.OFFSET_OPTION);

      // JavaUtils refreshes the file when getting it.
      ICompilationUnit src = JavaUtils.getCompilationUnit(projectName, file);

      IProblem problem = getProblem(src, line, offset);
      if(problem == null){
        return Services.getMessage("error.not.found", file, line);
      }

      List<IJavaCompletionProposal> proposals = getProposals(src, problem);
      if(_commandLine.hasOption(Options.APPLY_OPTION)){
        IJavaCompletionProposal proposal = (IJavaCompletionProposal)
          proposals.get(_commandLine.getIntValue(Options.APPLY_OPTION));

        // not working for some reason (silently does nothing).
        // probably because it's so heavily dependent on the ui.
        //proposal.apply(JavaUtils.getDocument(src));
        return proposal.toString();
      }
      return super.filter(_commandLine, getCorrections(proposals, problem));
    }catch(Exception e){
      return e;
    }
  }

  /**
   * Gets the requested problem.
   *
   * @param _src The source file.
   * @param _line The line number of the error.
   * @param _offset The offset of the error.
   * @return The IProblem or null if none found.
   */
  protected IProblem getProblem (ICompilationUnit _src, int _line, int _offset)
    throws Exception
  {
    IProblem[] problems = JavaUtils.getProblems(_src);
    ArrayList<IProblem> errors = new ArrayList<IProblem>();
    for(int ii = 0; ii < problems.length; ii++){
      if(problems[ii].getSourceLineNumber() == _line){
        errors.add(problems[ii]);
      }
    }

    IProblem problem = null;
    if(errors.size() == 0){
      return null;
    }else if(errors.size() > 0){
      for (IProblem p : errors){
        if(_offset < p.getSourceStart() && _offset <= p.getSourceEnd()){
          problem = p;
        }
      }
    }
    if(problem == null){
      problem = (IProblem)errors.get(0);
    }

    return problem;
  }

  /**
   * Gets possible corrections for the supplied problem.
   *
   * @param _src The src file.
   * @param _problem The problem.
   * @return Returns a List of IJavaCompletionProposal.
   */
  protected List<IJavaCompletionProposal> getProposals (
      ICompilationUnit _src, IProblem _problem)
    throws Exception
  {
    ArrayList<IJavaCompletionProposal> results =
      new ArrayList<IJavaCompletionProposal>();
    int length = _problem.getSourceEnd() - _problem.getSourceStart();
    AssistContext context = new AssistContext(
        _src, _problem.getSourceStart(), length);

    IProblemLocation[] locations = new IProblemLocation[]{
      new ProblemLocation(_problem)};
    IQuickFixProcessor[] processors = JavaUtils.getQuickFixProcessors(_src);
    for(int ii = 0; ii < processors.length; ii++){
      if(processors[ii].hasCorrections(_src, _problem.getID())){
        IJavaCompletionProposal[] proposals =
          processors[ii].getCorrections(context, locations);
        if(proposals != null){
          CollectionUtils.addAll(results, proposals);
        }
      }
    }
    return results;
  }

  /**
   * Converts the supplied list of IJavaCompletionProposal(s) to array of
   * CodeCorrectResult.
   *
   * @param _proposals List of IJavaCompletionProposal.
   * @param _problem The problem the proposals are associated w/.
   * @return Array of CodeCorrectResult.
   */
  protected List<CodeCorrectResult> getCorrections (
      List<IJavaCompletionProposal> _proposals, IProblem _problem)
    throws Exception
  {
    ArrayList<CodeCorrectResult> corrections = new ArrayList<CodeCorrectResult>();
    Iterator<IJavaCompletionProposal> iterator = _proposals.iterator();
    for(int ii = 0; iterator.hasNext(); ii++){
      IJavaCompletionProposal proposal = iterator.next();
      corrections.add(new CodeCorrectResult(ii, _problem,
          proposal.getDisplayString(),
          proposal.getAdditionalProposalInfo()));
    }
    return corrections;
  }
}
