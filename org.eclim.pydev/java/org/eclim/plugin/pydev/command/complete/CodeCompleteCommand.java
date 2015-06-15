/**
 * Copyright (C) 2013 - 2015  Eric Van Dewoestine
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
package org.eclim.plugin.pydev.command.complete;

import java.io.File;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;

import org.eclim.plugin.core.command.complete.AbstractCodeCompleteCommand;
import org.eclim.plugin.core.command.complete.CodeCompleteResult;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclipse.core.resources.IProject;

import org.eclipse.jface.text.contentassist.ICompletionProposal;

import org.eclipse.jface.text.source.ISourceViewer;

import org.python.pydev.core.IIndentPrefs;
import org.python.pydev.core.IPythonNature;
import org.python.pydev.core.MisconfigurationException;

import org.python.pydev.core.docutils.PySelection;

import org.python.pydev.editor.IPySyntaxHighlightingAndCodeCompletionEditor;
import org.python.pydev.editor.PyEditConfigurationWithoutEditor;

import org.python.pydev.editor.codecompletion.CompletionError;
import org.python.pydev.editor.codecompletion.PyContentAssistant;
import org.python.pydev.editor.codecompletion.PythonCompletionProcessor;

import org.python.pydev.plugin.nature.PythonNature;

import org.python.pydev.ui.ColorAndStyleCache;

import com.python.pydev.analysis.CtxInsensitiveImportComplProposal;

/**
 * Command to perform python code completion.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "python_complete",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "REQUIRED o offset ARG," +
    "REQUIRED e encoding ARG"
)
public class CodeCompleteCommand
  extends AbstractCodeCompleteCommand
{
  @Override
  protected ICompletionProposal[] getCompletionProposals(
      CommandLine commandLine, String projectName, String fileName, int offset)
    throws Exception
  {
    ISourceViewer viewer = getTextViewer(commandLine, projectName, fileName);
    File file = new File(ProjectUtils.getFilePath(projectName, fileName));
    IProject project = ProjectUtils.getProject(projectName);
    IPySyntaxHighlightingAndCodeCompletionEditor editor =
      new PyEditor(project, file, viewer);
    PyContentAssistant assistant = new PyContentAssistant();
    PythonCompletionProcessor processor =
      new PythonCompletionProcessor(editor, assistant);
    ICompletionProposal[] results =
      processor.computeCompletionProposals(viewer, offset);
    return results;
  }

  @Override
  protected CodeCompleteResult createCodeCompletionResult(
      ICompletionProposal proposal)
  {
    if (proposal instanceof CompletionError){
      return null;
    }
    if (proposal instanceof CtxInsensitiveImportComplProposal){
      return null;
    }
    return super.createCodeCompletionResult(proposal);
  }

  @Override
  protected String getCompletion(ICompletionProposal proposal)
  {
    String completion = proposal.getDisplayString();
    int open = completion.indexOf('(');
    if (open != -1){
      int close = completion.indexOf(')');
      if (close != open + 1){
        completion = completion.substring(0, open + 1);
      }
    }
    return completion;
  }

  static class PyEditor
    implements IPySyntaxHighlightingAndCodeCompletionEditor
  {
    private IProject project;
    private File file;
    private ISourceViewer viewer;

    public PyEditor(IProject project, File file, ISourceViewer viewer)
    {
      this.project = project;
      this.file = file;
    }

    @Override
    public File getEditorFile()
    {
      return file;
    }

    @Override
    public IPythonNature getPythonNature()
      throws MisconfigurationException
    {
      return PythonNature.getPythonNature(project);
    }

    @Override
    public ISourceViewer getEditorSourceViewer()
    {
      return viewer;
    }

    @Override
    public void resetForceTabs()
    {
    }

    @Override
    public void resetIndentPrefixes()
    {
    }

    @Override
    public IIndentPrefs getIndentPrefs()
    {
     return null;
    }

    @Override
    public PyEditConfigurationWithoutEditor getEditConfiguration()
    {
      return null;
    }

    @Override
    public ColorAndStyleCache getColorCache()
    {
      return null;
    }

    @Override
    public PySelection createPySelection()
    {
      return null;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Object getAdapter(Class adapter)
    {
      return null;
    }

    @Override
    public int getGrammarVersion()
      throws MisconfigurationException
    {
      return getPythonNature().getGrammarVersion();
    }
  }
}
