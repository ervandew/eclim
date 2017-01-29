/**
 * Copyright (C) 2014 - 2016
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
package org.eclim.plugin.groovy.command.complete;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.groovy.eclipse.codeassist.requestor.GroovyCompletionProposalComputer;

import org.codehaus.groovy.eclipse.editor.GroovyEditor;

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;
import org.eclim.plugin.core.command.complete.CodeCompleteResult;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclipse.core.resources.IFile;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;

import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;

import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;

import org.eclipse.jface.text.contentassist.ICompletionProposal;

import org.eclipse.ui.ide.IDE;

/**
 * Command which provides code completion for groovy files.
 *
 * @param javaDoc
 *   If {@code javaDoc} is set, then each completion result will include an
 *   eclipse style javadoc URI which can supplied to the
 *   {@code java_element_doc} command to obtain the javadoc content.
 */
@Command(
  name = "groovy_complete",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "REQUIRED o offset ARG," +
    "REQUIRED e encoding ARG," +
    "REQUIRED l layout ARG," +
    "OPTIONAL j javaDoc NOARG"
)
public final class CodeCompleteCommand
  extends org.eclim.plugin.jdt.command.complete.CodeCompleteCommand
{
  @Override
  protected Object getResponse(List<CodeCompleteResult> results)
  {
    // bypass the super class jdt response
    return results;
  }

  @Override
  protected List<CodeCompleteResult> getCompletionResults(
      CommandLine commandLine, String project, String file, int offset)
    throws Exception
  {
    IFile ifile = ProjectUtils.getFile(
        ProjectUtils.getProject(project, true), file);
    ICompilationUnit unit = JavaCore.createCompilationUnitFrom(ifile);

    // ensure opens with Groovy editor
    if(unit instanceof GroovyCompilationUnit){
      unit.getResource().setPersistentProperty(
          IDE.EDITOR_KEY, GroovyEditor.EDITOR_ID);
    }

    JavaEditor editor = (JavaEditor)EditorUtility.openInEditor(unit);
    JavaSourceViewer viewer = (JavaSourceViewer)editor.getViewer();
    JavaContentAssistInvocationContext context =
      new JavaContentAssistInvocationContext(viewer, offset, editor);

    IJavaCompletionProposalComputer computer =
      new GroovyCompletionProposalComputer();
    List<ICompletionProposal> proposals =
      computer.computeCompletionProposals(context, null);

    editor.close(false);

    if(proposals == null){
      proposals = Collections.emptyList();
    }

    boolean javaDocEnabled = commandLine.hasOption(Options.JAVA_DOC_OPTION);
    ArrayList<CodeCompleteResult> results = new ArrayList<CodeCompleteResult>();
    for(ICompletionProposal proposal : proposals){
      results.add(createCompletionResult(
            (IJavaCompletionProposal)proposal, javaDocEnabled));
    }
    Collections.sort(results, COMPLETION_COMPARATOR);

    return results;
  }
}
