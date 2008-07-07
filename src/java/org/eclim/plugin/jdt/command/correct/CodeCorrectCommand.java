/**
 * Copyright (C) 2005 - 2008  Eric Van Dewoestine
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
import java.util.Iterator;
import java.util.List;

import org.eclim.Services;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclim.util.CollectionUtils;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.jdt.core.ICompilationUnit;

import org.eclipse.jdt.core.compiler.IProblem;

import org.eclipse.jdt.internal.corext.util.Strings;

import org.eclipse.jdt.internal.ui.text.correction.AssistContext;

import org.eclipse.jdt.internal.ui.text.correction.proposals.CUCorrectionProposal;
import org.eclipse.jdt.internal.ui.text.correction.proposals.CorrectPackageDeclarationProposal;

import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import org.eclipse.ltk.core.refactoring.TextChange;

import org.eclipse.text.edits.CopyTargetEdit;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MoveSourceEdit;
import org.eclipse.text.edits.MoveTargetEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditVisitor;

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
  public String execute (CommandLine _commandLine)
    throws Exception
  {
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
    List<CodeCorrectResult> corrections = getCorrections(proposals, problem);
    return CodeCorrectFilter.instance.filter(_commandLine, corrections);
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
      String info = null;
      if (proposal instanceof CorrectPackageDeclarationProposal){
        info = getAdditionalProposalInfo((CUCorrectionProposal)proposal);
      }else{
        info = proposal.getAdditionalProposalInfo();
      }
      corrections.add(new CodeCorrectResult(
            ii, _problem, proposal.getDisplayString(), info));
    }
    return corrections;
  }

  // Direct copy from (w/ changes noted):
  // org.eclipse.jdt.internal.ui.text.correction.proposals.CUCorrectionProposal
  private String getAdditionalProposalInfo (CUCorrectionProposal proposal)
    throws Exception
  {
    final StringBuffer buf= new StringBuffer();

    final TextChange change = proposal.getTextChange();

    change.setKeepPreviewEdits(true);
    final IDocument previewContent= change.getPreviewDocument(new NullProgressMonitor());
    final TextEdit rootEdit= change.getPreviewEdit(change.getEdit());

    class EditAnnotator extends TextEditVisitor {
      private int fWrittenToPos = 0;

      public void unchangedUntil(int pos) {
        if (pos > fWrittenToPos) {
          appendContent(previewContent, fWrittenToPos, pos, buf, true);
          fWrittenToPos = pos;
        }
      }

      public boolean visit(MoveTargetEdit edit) {
        return true; //rangeAdded(edit);
      }

      public boolean visit(CopyTargetEdit edit) {
        return true; //return rangeAdded(edit);
      }

      public boolean visit(InsertEdit edit) {
        return rangeAdded(edit);
      }

      public boolean visit(ReplaceEdit edit) {
        if (edit.getLength() > 0)
          return rangeAdded(edit);
        return rangeRemoved(edit);
      }

      public boolean visit(MoveSourceEdit edit) {
        return rangeRemoved(edit);
      }

      public boolean visit(DeleteEdit edit) {
        return rangeRemoved(edit);
      }

      private boolean rangeRemoved(TextEdit edit) {
        unchangedUntil(edit.getOffset());
        return false;
      }

      private boolean rangeAdded(TextEdit edit) {
        unchangedUntil(edit.getOffset());
        buf.append("<b>"); //$NON-NLS-1$
        appendContent(previewContent, edit.getOffset(), edit.getExclusiveEnd(), buf, false);
        buf.append("</b>"); //$NON-NLS-1$
        //fWrittenToPos = edit.getExclusiveEnd();
        // EV: without the '+ 1', the last character of the text removed will
        // remain (easily reproducable with a package name correction).
        fWrittenToPos = edit.getExclusiveEnd() + 1;
        return false;
      }
    }
    EditAnnotator ea = new EditAnnotator();
    rootEdit.accept(ea);

    // Final pre-existing region
    ea.unchangedUntil(previewContent.getLength());
    return buf.toString();
  }


  private final int surroundLines= 1;
  // Direct copy from:
  // org.eclipse.jdt.internal.ui.text.correction.proposals.CUCorrectionProposal
  private void appendContent(IDocument text, int startOffset, int endOffset, StringBuffer buf, boolean surroundLinesOnly) {
    try {
      int startLine= text.getLineOfOffset(startOffset);
      int endLine= text.getLineOfOffset(endOffset);

      boolean dotsAdded= false;
      if (surroundLinesOnly && startOffset == 0) { // no surround lines for the top no-change range
        startLine= Math.max(endLine - surroundLines, 0);
        buf.append("...<br>"); //$NON-NLS-1$
        dotsAdded= true;
      }

      for (int i= startLine; i <= endLine; i++) {
        if (surroundLinesOnly) {
          if ((i - startLine > surroundLines) && (endLine - i > surroundLines)) {
            if (!dotsAdded) {
              buf.append("...<br>"); //$NON-NLS-1$
              dotsAdded= true;
            } else if (endOffset == text.getLength()) {
              return; // no surround lines for the bottom no-change range
            }
            continue;
          }
        }

        IRegion lineInfo= text.getLineInformation(i);
        int start= lineInfo.getOffset();
        int end= start + lineInfo.getLength();

        int from= Math.max(start, startOffset);
        int to= Math.min(end, endOffset);
        String content= text.get(from, to - from);
        if (surroundLinesOnly && (from == start) && Strings.containsOnlyWhitespaces(content)) {
          continue; // ignore empty lines except when range started in the middle of a line
        }
        for (int k= 0; k < content.length(); k++) {
          char ch= content.charAt(k);
          if (ch == '<') {
            buf.append("&lt;"); //$NON-NLS-1$
          } else if (ch == '>') {
            buf.append("&gt;"); //$NON-NLS-1$
          } else {
            buf.append(ch);
          }
        }
        if (to == end && to != endOffset) { // new line when at the end of the line, and not end of range
          buf.append("<br>"); //$NON-NLS-1$
        }
      }
    } catch (BadLocationException e) {
      // ignore
    }
  }
}
