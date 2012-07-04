/**
 * Copyright (C) 2005 - 2012  Eric Van Dewoestine
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

import java.text.Collator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclipse.jdt.core.ICompilationUnit;

import org.eclipse.jdt.core.compiler.IProblem;

import org.eclipse.jdt.internal.ui.text.correction.AssistContext;

import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;

import org.eclipse.jdt.ui.text.java.correction.CUCorrectionProposal;

/**
 * Handles requests for code correction.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "java_correct",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "REQUIRED l line ARG," +
    "REQUIRED o offset ARG," +
    "OPTIONAL e encoding ARG," +
    "OPTIONAL a apply ARG"
)
public class CodeCorrectCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public Object execute(CommandLine commandLine)
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

      // does not work since it is so deeply tied to the ui (grabbing the
      // editor, opening dialogs, etc.).
      //proposal.apply(JavaUtils.getDocument(src));
      return proposal.toString();
    }

    HashMap<String,Object> result = new HashMap<String,Object>();
    result.put("message", problem.getMessage());
    result.put("offset", problem.getSourceStart());
    result.put("corrections", getCorrections(proposals));
    return result;
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
    int length = (problem.getSourceEnd() + 1) - problem.getSourceStart();
    AssistContext context = new AssistContext(
        src, problem.getSourceStart(), length);

    IProblemLocation[] locations =
      new IProblemLocation[]{new ProblemLocation(problem)};
    IQuickFixProcessor[] processors = JavaUtils.getQuickFixProcessors(src);
    for(int ii = 0; ii < processors.length; ii++){
      if (processors[ii] != null &&
          processors[ii].hasCorrections(src, problem.getID()))
      {
        // we currently don't support the ajdt processor since it relies on
        // PlatformUI.getWorkbench().getActiveWorkbenchWindow() which is null
        // here.
        if (processors[ii].getClass().getName().equals(
              "org.eclipse.ajdt.internal.ui.editor.quickfix.QuickFixProcessor"))
        {
          continue;
        }

        IJavaCompletionProposal[] proposals =
          processors[ii].getCorrections(context, locations);
        if(proposals != null){
          for (IJavaCompletionProposal proposal : proposals){
            if (!(proposal instanceof CUCorrectionProposal)){
              continue;
            }

            CUCorrectionProposal cuProposal = (CUCorrectionProposal)proposal;

            // for now we aren't going to support changes to files other than the
            // current one.
            if (!src.equals(cuProposal.getCompilationUnit())){
              continue;
            }

            // filter out corrections that have no preview, since they can't be
            // applied in the same fashion as those that have previews.
            String preview = proposal.getAdditionalProposalInfo();
            if (preview == null ||
                preview.trim().equals("") ||
                preview.trim().startsWith("Start the") ||
                preview.trim().startsWith("Opens") ||
                preview.trim().startsWith("Evaluates") ||
                preview.trim().startsWith("<p>Move"))
            {
              continue;
            }

            results.add(cuProposal);
          }
        }
      }
    }
    final Collator collator = Collator.getInstance();
    Collections.sort(results, new Comparator<IJavaCompletionProposal>(){
      public int compare(IJavaCompletionProposal p1, IJavaCompletionProposal p2){
        int r1 = p1.getRelevance();
        int r2 = p2.getRelevance();
        if (r1 == r2){
          // used as a determanistic tie breaker.
          return collator.compare(p1.getDisplayString(), p2.getDisplayString());
        }
        // higher number is more relavant.
        return r2 - r1;
      }
      public boolean equals(Object obj){
        return false;
      }
    });
    return results;
  }

  /**
   * Converts the supplied list of IJavaCompletionProposal(s) to array of
   * CodeCorrectResult.
   *
   * @param proposals List of IJavaCompletionProposal.
   * @return Array of CodeCorrectResult.
   */
  protected List<CodeCorrectResult> getCorrections(
      List<IJavaCompletionProposal> proposals)
    throws Exception
  {
    ArrayList<CodeCorrectResult> corrections = new ArrayList<CodeCorrectResult>();
    Iterator<IJavaCompletionProposal> iterator = proposals.iterator();
    for(int ii = 0; iterator.hasNext(); ii++){
      IJavaCompletionProposal proposal = iterator.next();
      String preview = null;
      /*if (proposal instanceof CorrectPackageDeclarationProposal){
        preview = getAdditionalProposalInfo((CUCorrectionProposal)proposal);
      }else{*/
        preview = proposal.getAdditionalProposalInfo();
      //}

      preview = preview
        .replaceAll("<br>", "\n")
        .replaceAll("<.+?>", "");
      corrections.add(new CodeCorrectResult(
            ii, proposal.getDisplayString(), preview));
    }
    return corrections;
  }
}
