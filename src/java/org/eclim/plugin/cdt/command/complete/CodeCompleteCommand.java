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
package org.eclim.plugin.cdt.command.complete;

import java.lang.reflect.Method;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;

import org.eclim.eclipse.EclimPlugin;

import org.eclim.eclipse.ui.EclimEditorSite;

import org.eclim.plugin.core.command.complete.AbstractCodeCompleteCommand;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.CSourceViewer;

import org.eclipse.cdt.internal.ui.text.CTextTools;

import org.eclipse.cdt.internal.ui.text.contentassist.CCompletionProposal;

import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.ui.text.CSourceViewerConfiguration;

import org.eclipse.core.resources.IProject;

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.jface.text.ITextViewer;

import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

import org.eclipse.swt.SWT;

import org.eclipse.ui.IEditorInput;

import org.eclipse.ui.part.FileEditorInput;

/**
 * Command to handle c/cpp code completion.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "c_complete",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "REQUIRED o offset ARG," +
    "REQUIRED e encoding ARG"
)
public class CodeCompleteCommand
  extends AbstractCodeCompleteCommand
{
  /**
   * {@inheritDoc}
   * @see AbstractCodeCompleteCommand#getCompletionProposals(CommandLine,String,String,int)
   */
  @Override
  protected ICompletionProposal[] getCompletionProposals(
      CommandLine commandLine, String projectName, String file, int offset)
    throws Exception
  {
    IProject project = ProjectUtils.getProject(projectName);

    CEditor editor = new CEditor();
    IEditorInput input =
      new FileEditorInput(ProjectUtils.getFile(project, file));
    editor.init(new EclimEditorSite(), input);
    editor.setInput(input);

    CTextTools textTools = CUIPlugin.getDefault().getTextTools();
    IPreferenceStore store = CUIPlugin.getDefault().getCombinedPreferenceStore();
    CSourceViewerConfiguration config =
      new CSourceViewerConfiguration(
          textTools.getColorManager(),
          store,
          editor,
          textTools.getDocumentPartitioning());

    CSourceViewer viewer = new CSourceViewer(
        EclimPlugin.getShell(), null, null, false,
        SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.FULL_SELECTION,
        CUIPlugin.getDefault().getPreferenceStore());
    viewer.setDocument(ProjectUtils.getDocument(project, file));

    ContentAssistant ca = (ContentAssistant)config.getContentAssistant(viewer);
    Method computeCompletionProposals =
      ContentAssistant.class.getDeclaredMethod(
          "computeCompletionProposals", ITextViewer.class, Integer.TYPE);
    computeCompletionProposals.setAccessible(true);

    return (ICompletionProposal[])
      computeCompletionProposals.invoke(ca, viewer, offset);
  }

  /**
   * {@inheritDoc}
   * @see AbstractCodeCompleteCommand#getCompletion(ICompletionProposal)
   */
  @Override
  protected String getCompletion(ICompletionProposal proposal)
  {
    if (proposal instanceof CCompletionProposal){
      String displayString = proposal.getDisplayString();
      String completion = ((CCompletionProposal)proposal).getReplacementString();
      if (displayString.lastIndexOf(')') > displayString.lastIndexOf('(') + 1 &&
          (completion.length() > 0 &&
           completion.charAt(completion.length() - 1) == ')')){
        completion = completion.substring(0, completion.length() - 1);
      } else if (completion.charAt(0) == '<'){
        completion = completion.substring(1, completion.length());
      }
      return completion;
    }
    return super.getCompletion(proposal);
  }

  /**
   * {@inheritDoc}
   * @see AbstractCodeCompleteCommand#getDescription(ICompletionProposal)
   */
  @Override
  protected String getDescription(ICompletionProposal proposal)
  {
    return proposal.getAdditionalProposalInfo();
  }

  /**
   * {@inheritDoc}
   * @see AbstractCodeCompleteCommand#getShortDescription(ICompletionProposal)
   */
  @Override
  protected String getShortDescription(ICompletionProposal proposal)
  {
    return proposal.getDisplayString();
  }
}
