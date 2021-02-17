/**
 * Copyright (C) 2005 - 2021  Eric Van Dewoestine
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.command.refactoring.ResourceChangeListener;

import org.eclim.plugin.jdt.command.include.ImportUtils;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.jdt.core.ICompilationUnit;

import org.eclipse.jdt.core.compiler.IProblem;

import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;

import org.eclipse.jdt.core.formatter.CodeFormatter;

import org.eclipse.jdt.core.refactoring.CompilationUnitChange;

import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.jdt.internal.ui.text.correction.CorrectionMessages;
import org.eclipse.jdt.internal.ui.text.correction.ReorgCorrectionsSubProcessor.ClasspathFixCorrectionProposal;

import org.eclipse.jdt.internal.ui.text.correction.proposals.NewCUUsingWizardProposal;
import org.eclipse.jdt.internal.ui.text.correction.proposals.NewVariableCorrectionProposal;

import org.eclipse.jdt.ui.text.java.CompletionProposalComparator;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;

import org.eclipse.jdt.ui.text.java.correction.ASTRewriteCorrectionProposal;
import org.eclipse.jdt.ui.text.java.correction.ChangeCorrectionProposal;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.ltk.core.refactoring.TextFileChange;

import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

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
  private static HashSet<Class<? extends IJavaCompletionProposal>> IGNORE_BY_TYPE =
    new HashSet<Class<? extends IJavaCompletionProposal>>();
  static {
    IGNORE_BY_TYPE.add(NewCUUsingWizardProposal.class);
    IGNORE_BY_TYPE.add(ClasspathFixCorrectionProposal.class);
  }

  private static HashSet<String> IGNORE_BY_INFO = new HashSet<String>();
  static {
    IGNORE_BY_INFO.add(CorrectionMessages
        .LocalCorrectionsSubProcessor_InferGenericTypeArguments_description);
    IGNORE_BY_INFO.add(CorrectionMessages
        .GetterSetterCorrectionSubProcessor_additional_info);
  }

  @Override
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

    List<ChangeCorrectionProposal> proposals = getProposals(src, problem);
    if(commandLine.hasOption(Options.APPLY_OPTION)){
      ChangeCorrectionProposal proposal =
        proposals.get(commandLine.getIntValue(Options.APPLY_OPTION));
      return apply(src, proposal);
    }

    HashMap<String, Object> result = new HashMap<String, Object>();
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
  private IProblem getProblem(ICompilationUnit src, int line, int offset)
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
   * @return Returns a List of ChangeCorrectionProposal.
   */
  private List<ChangeCorrectionProposal> getProposals(
      ICompilationUnit src, IProblem problem)
    throws Exception
  {
    IProject project = src.getJavaProject().getProject();

    ArrayList<ChangeCorrectionProposal> results =
      new ArrayList<ChangeCorrectionProposal>();
    int length = (problem.getSourceEnd() + 1) - problem.getSourceStart();
    AssistContext context = new AssistContext(
        src, problem.getSourceStart(), length);

    IProblemLocation location;
    switch(problem.getID()){
      case IProblem.MissingSerialVersion:
        location = new
          org.eclipse.jdt.internal.ui.text.correction.ProblemLocation(problem);
        break;
      default:
        location = new ProblemLocation(problem);
    }

    IProblemLocation[] locations = new IProblemLocation[]{location};
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
            if (!(proposal instanceof ChangeCorrectionProposal)){
              continue;
            }

            // skip proposal requiring gui dialogs, etc.
            if (IGNORE_BY_TYPE.contains(proposal.getClass()) ||
                IGNORE_BY_INFO.contains(proposal.getAdditionalProposalInfo()))
            {
              continue;
            }

            // honor the user's import exclusions
            if (proposal instanceof ASTRewriteCorrectionProposal){
              ImportRewrite rewrite =
                ((ASTRewriteCorrectionProposal)proposal).getImportRewrite();
              if (rewrite != null && (
                    rewrite.getAddedImports().length != 0 ||
                    rewrite.getAddedStaticImports().length != 0))
              {
                boolean exclude = true;
                for(String fqn : rewrite.getAddedImports()){
                  if (!ImportUtils.isImportExcluded(project, fqn)){
                    exclude = false;
                    break;
                  }
                }
                for(String fqn : rewrite.getAddedStaticImports()){
                  if (!ImportUtils.isImportExcluded(project, fqn)){
                    exclude = false;
                    break;
                  }
                }

                if (exclude){
                  continue;
                }
              }
            }

            results.add((ChangeCorrectionProposal)proposal);
          }
        }
      }
    }

    Collections.sort(results, new CompletionProposalComparator());
    return results;
  }

  /**
   * Converts the supplied list of IJavaCompletionProposal(s) to array of
   * CodeCorrectResult.
   *
   * @param proposals List of IJavaCompletionProposal.
   * @return Array of CodeCorrectResult.
   */
  private List<CodeCorrectResult> getCorrections(
      List<ChangeCorrectionProposal> proposals)
    throws Exception
  {
    ArrayList<CodeCorrectResult> corrections = new ArrayList<CodeCorrectResult>();
    int index = 0;
    for(ChangeCorrectionProposal proposal : proposals){
      String preview = proposal.getAdditionalProposalInfo();
      if (preview != null){
        preview = preview
          .replaceAll("<br>", "\n")
          .replaceAll("<.+?>", "")
          .replaceAll("&lt;", "<")
          .replaceAll("&gt;", ">");
      }
      corrections.add(new CodeCorrectResult(
            index, proposal.getDisplayString(), preview));
      index++;
    }
    return corrections;
  }

  /**
   * Apply the supplied correction proposal.
   *
   * @param src The ICompilationUnit where the change is initiated from.
   * @param proposal The ChangeCorrectionProposal to apply.
   * @return A list of changed files or a map containing a list of errors.
   */
  private Object apply(ICompilationUnit src, ChangeCorrectionProposal proposal)
    throws Exception
  {
    Change change = null;
    try {
      NullProgressMonitor monitor = new NullProgressMonitor();
      change = proposal.getChange();
      change.initializeValidationData(monitor);
      RefactoringStatus status = change.isValid(monitor);
      if (status.hasFatalError()){
        List<String> errors = new ArrayList<String>();
        for (RefactoringStatusEntry entry : status.getEntries()){
          String message = entry.getMessage();
          if (!errors.contains(message) &&
              !message.startsWith("Found potential matches"))
          {
            errors.add(message);
          }
        }
        HashMap<String, List<String>> result = new HashMap<String, List<String>>();
        result.put("errors", errors);
        return result;
      }

      ResourceChangeListener rcl = new ResourceChangeListener();
      IWorkspace workspace = ResourcesPlugin.getWorkspace();
      workspace.addResourceChangeListener(rcl);
      try{
        TextEdit[] edits = new TextEdit[0];
        if (change instanceof TextFileChange){
          TextFileChange fileChange = (TextFileChange)change;
          fileChange.setSaveMode(TextFileChange.FORCE_SAVE);
          TextEdit edit = fileChange.getEdit();
          if (edit instanceof MultiTextEdit){
            edits = ((MultiTextEdit)edit).getChildren();
          }else{
            edits = new TextEdit[]{edit};
          }
        }

        PerformChangeOperation changeOperation = new PerformChangeOperation(change);
        changeOperation.setUndoManager(
            RefactoringCore.getUndoManager(), proposal.getName());
        changeOperation.run(monitor);

        if (edits.length > 0 &&
            change instanceof CompilationUnitChange &&
            src.equals(((CompilationUnitChange)change).getCompilationUnit()))
        {
          for (TextEdit edit : edits){
            int offset = edit.getOffset();
            int length = edit.getLength();

            // for "Create field" and "Create local" the edit length includes
            // additional existing code that we don't want to reformat.
            if (proposal instanceof NewVariableCorrectionProposal) {
              String text = src.getBuffer()
                .getText(edit.getOffset(), edit.getLength());
              int index = text.indexOf('\n');
              if (index != -1){
                length = index;
                // include the white space up to the next bit of code
                while(length < text.length()){
                  char next = text.charAt(length);
                  if (next == '\t' || next == '\n' || next == ' '){
                    length += 1;
                    continue;
                  }
                  break;
                }
              }
            }

            JavaUtils.format(
                src, CodeFormatter.K_COMPILATION_UNIT, offset, length);
          }
        }

        // if the proposal change touched the imports, then run our import
        // grouping edit after it.
        if (proposal instanceof ASTRewriteCorrectionProposal){
          ASTRewriteCorrectionProposal astProposal =
            (ASTRewriteCorrectionProposal)proposal;
          if (astProposal.getImportRewrite() != null){
            TextEdit groupingEdit =
              ImportUtils.importGroupingEdit(src, getPreferences());
            if (groupingEdit != null){
              JavaModelUtil.applyEdit(src, groupingEdit, true, null);
              if (src.isWorkingCopy()) {
                src.commitWorkingCopy(false, null);
              }
            }
          }
        }

        return rcl.getChangedFiles();
      }finally{
        workspace.removeResourceChangeListener(rcl);
      }
    }finally{
      if (change != null) {
        change.dispose();
      }
    }
  }
}
