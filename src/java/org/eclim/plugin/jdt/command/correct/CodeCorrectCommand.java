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
package org.eclim.plugin.jdt.command.correct;

import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclim.Services;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclim.util.CollectionUtils;

import org.eclipse.jdt.core.ICompilationUnit;

import org.eclipse.jdt.core.compiler.IProblem;

import org.eclipse.jdt.internal.ui.text.correction.AssistContext;

import org.eclipse.jdt.internal.ui.text.correction.proposals.CUCorrectionProposal;
import org.eclipse.jdt.internal.ui.text.correction.proposals.CorrectPackageDeclarationProposal;

import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;

import org.eclipse.ltk.core.refactoring.TextChange;

import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * Handles requests for code correction.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class CodeCorrectCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public String execute(CommandLine commandLine)
    throws Exception
  {
    String file = commandLine.getValue(Options.FILE_OPTION);
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    int line = commandLine.getIntValue(Options.LINE_OPTION);
    int offset = getOffset(commandLine);

    // JavaUtils refreshes the file when getting it.
    ICompilationUnit src = JavaUtils.getCompilationUnit(projectName, file);

    IProblem problem = getProblem(src, line, offset);
    if(problem == null){
      String message = Services.getMessage("error.not.found", file, line);
      if(commandLine.hasOption(Options.APPLY_OPTION)){
        throw new RuntimeException(message);
      }
      return message;
    }

    List<IJavaCompletionProposal> proposals = getProposals(src, problem);
    if(commandLine.hasOption(Options.APPLY_OPTION)){
      IJavaCompletionProposal proposal = (IJavaCompletionProposal)
        proposals.get(commandLine.getIntValue(Options.APPLY_OPTION));

      // not working for some reason (silently does nothing).
      // probably because it's so heavily dependent on the ui.
      //proposal.apply(JavaUtils.getDocument(src));
      return proposal.toString();
    }
    List<CodeCorrectResult> corrections = getCorrections(proposals, problem);
    return CodeCorrectFilter.instance.filter(commandLine, corrections);
  }

  /**
   * Gets the requested problem.
   *
   * @param src The source file.
   * @param line The line number of the error.
   * @param offset The offset of the error.
   * @return The IProblem or null if none found.
   */
  protected IProblem getProblem(ICompilationUnit src, int line, int offset)
    throws Exception
  {
    IProblem[] problems = JavaUtils.getProblems(src);
    ArrayList<IProblem> errors = new ArrayList<IProblem>();
    for(int ii = 0; ii < problems.length; ii++){
      if(problems[ii].getSourceLineNumber() == line){
        errors.add(problems[ii]);
      }
    }

    IProblem problem = null;
    if(errors.size() == 0){
      return null;
    }else if(errors.size() > 0){
      for (IProblem p : errors){
        if(offset < p.getSourceStart() && offset <= p.getSourceEnd()){
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
   * @param src The src file.
   * @param problem The problem.
   * @return Returns a List of IJavaCompletionProposal.
   */
  protected List<IJavaCompletionProposal> getProposals(
      ICompilationUnit src, IProblem problem)
    throws Exception
  {
    ArrayList<IJavaCompletionProposal> results =
      new ArrayList<IJavaCompletionProposal>();
    int length = problem.getSourceEnd() - problem.getSourceStart();
    AssistContext context = new AssistContext(
        src, problem.getSourceStart(), length);

    IProblemLocation[] locations = new IProblemLocation[]{
      new ProblemLocation(problem)};
    IQuickFixProcessor[] processors = JavaUtils.getQuickFixProcessors(src);
    for(int ii = 0; ii < processors.length; ii++){
      if(processors[ii].hasCorrections(src, problem.getID())){
        IJavaCompletionProposal[] proposals =
          processors[ii].getCorrections(context, locations);
        if(proposals != null){
          for (IJavaCompletionProposal proposal : proposals){
            // hack to fix off by one issue with some corrections in eclipse.
            if (proposal instanceof CorrectPackageDeclarationProposal){
              TextChange change = ((CUCorrectionProposal)proposal).getTextChange();
              TextEdit edit = change.getEdit();
              if (edit instanceof MultiTextEdit){
                Field fChildren = TextEdit.class.getDeclaredField("fChildren");
                fChildren.setAccessible(true);
                List children = (List)fChildren.get(edit);
                edit = (TextEdit)children.get(children.size() - 1);
              }
              Field flength = TextEdit.class.getDeclaredField("fLength");
              flength.setAccessible(true);
              flength.setInt(edit, edit.getLength() + 1);
            }
          }
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
   * @param proposals List of IJavaCompletionProposal.
   * @param problem The problem the proposals are associated w/.
   * @return Array of CodeCorrectResult.
   */
  protected List<CodeCorrectResult> getCorrections(
      List<IJavaCompletionProposal> proposals, IProblem problem)
    throws Exception
  {
    ArrayList<CodeCorrectResult> corrections = new ArrayList<CodeCorrectResult>();
    Iterator<IJavaCompletionProposal> iterator = proposals.iterator();
    for(int ii = 0; iterator.hasNext(); ii++){
      IJavaCompletionProposal proposal = iterator.next();
      String info = null;
      /*if (proposal instanceof CorrectPackageDeclarationProposal){
        info = getAdditionalProposalInfo((CUCorrectionProposal)proposal);
      }else{*/
        info = proposal.getAdditionalProposalInfo();
      //}
      corrections.add(new CodeCorrectResult(
            ii, problem, proposal.getDisplayString(), info));
    }
    return corrections;
  }
}
