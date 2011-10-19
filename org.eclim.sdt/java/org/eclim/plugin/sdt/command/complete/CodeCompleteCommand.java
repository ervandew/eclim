/**
 * Copyright (C) 2011  Eric Van Dewoestine
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
package org.eclim.plugin.sdt.command.complete;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.eclipse.EclimPlugin;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.command.complete.CodeCompleteComparator;
import org.eclim.plugin.core.command.complete.CodeCompleteResult;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.plugin.sdt.util.ScalaUtils;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.jdt.core.ICompilationUnit;

import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextViewer;

import scala.tools.eclipse.ScalaSourceFileEditor;

import scala.tools.eclipse.javaelements.ScalaSourceFile;

import scala.tools.eclipse.ui.ScalaCompletionProposal;
import scala.tools.eclipse.ui.ScalaCompletionProposalComputer;

/**
 * Command to handle scala code completion requests.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "scala_complete",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "REQUIRED o offset ARG," +
    "REQUIRED e encoding ARG," +
    "REQUIRED l layout ARG"
)
public class CodeCompleteCommand
  extends AbstractCommand
{
  private static final Comparator<CodeCompleteResult> COMPLETION_COMPARATOR =
    new CodeCompleteComparator();

  /**
   * {@inheritDoc}
   */
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String project = commandLine.getValue(Options.PROJECT_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);
    int offset = getOffset(commandLine);

    // FIXME: now that scala-ide completion code has been split up, rewrite this
    // in scala so we can make use of scala.tools.eclipse.completion.ScalaCompletions
    // directly instead of jumping through ui hoops.
    ScalaSourceFile src = ScalaUtils.getSourceFile(project, file);
    ITextViewer viewer = new TextViewer(EclimPlugin.getShell(), 0);
    viewer.setDocument(ProjectUtils.getDocument(project, file));
    ContentAssistInvocationContext context =
      new ContentAssistInvocationContext(viewer, src, offset);
    ScalaCompletionProposalComputer computer =
      new ScalaCompletionProposalComputer();
    @SuppressWarnings("unchecked")
    List<IJavaCompletionProposal> proposals = (List<IJavaCompletionProposal>)
      computer.computeCompletionProposals(context, new NullProgressMonitor());

    ArrayList<CodeCompleteResult> results = new ArrayList<CodeCompleteResult>();
    for (IJavaCompletionProposal prop : proposals){
      ScalaCompletionProposal proposal = (ScalaCompletionProposal)prop;

      String completion = proposal.completionString();
      String description = proposal.getDisplayString();
      // for short desc, shorten all fully qualified Types to just the type.
      String shortDescription = description.replaceAll(
          "[a-zA-Z]\\w*[\\w.]*\\.(\\w+[^.])", "$1");
      // add trailing open paren for methods that take args (having kind here
      // would be nice).
      if (completion.endsWith("()")){
        completion = completion.substring(0, completion.length() - 2);
      }
      if (description.matches("^\\Q" + completion + "\\E(\\[[^]]+\\])?\\([^)].*")) {
        completion += "(";
      }

      results.add(new CodeCompleteResult(
            completion, shortDescription, description));
    }

    Collections.sort(results, COMPLETION_COMPARATOR);
    return results;
  }

  /* Hacky dummy object to help get completion results */

  private class ContentAssistInvocationContext
    extends JavaContentAssistInvocationContext
  {
    private ICompilationUnit src;

    public ContentAssistInvocationContext(
        ITextViewer viewer, ICompilationUnit src, int offset)
    {
      super(viewer, offset, new ScalaSourceFileEditor());
      this.src = src;
    }

    public ICompilationUnit getCompilationUnit()
    {
      return src;
    }
  }
}
