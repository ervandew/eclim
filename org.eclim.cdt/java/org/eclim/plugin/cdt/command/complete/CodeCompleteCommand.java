/**
 * Copyright (C) 2005 - 2017  Eric Van Dewoestine
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;

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
import org.eclipse.ui.PartInitException;

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
    "REQUIRED e encoding ARG," +
    "REQUIRED l layout ARG"
)
public class CodeCompleteCommand
  extends AbstractCodeCompleteCommand
{
  @Override
  protected ICompletionProposal[] getCompletionProposals(
      CommandLine commandLine, String projectName, String file, int offset)
  {
    IProject project = ProjectUtils.getProject(projectName);

    CEditor editor = new CEditor();
    IEditorInput input =
      new FileEditorInput(ProjectUtils.getFile(project, file));
    try{
      editor.init(new EclimEditorSite(), input);
    }catch(PartInitException pie){
      throw new RuntimeException(pie);
    }
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
    try{
      Method computeCompletionProposals =
        ContentAssistant.class.getDeclaredMethod(
            "computeCompletionProposals", ITextViewer.class, Integer.TYPE);
      computeCompletionProposals.setAccessible(true);

      return (ICompletionProposal[])
        computeCompletionProposals.invoke(ca, viewer, offset);
    }catch(NoSuchMethodException nsme){
      throw new RuntimeException(nsme);
    }catch(IllegalAccessException iae){
      throw new RuntimeException(iae);
    }catch(InvocationTargetException ite){
      throw new RuntimeException(ite);
    }
  }

  @Override
  protected String getCompletion(ICompletionProposal proposal)
  {
    if (proposal instanceof CCompletionProposal){
      String completion = ((CCompletionProposal)proposal).getReplacementString();
      if (completion.length() > 0 &&
          completion.lastIndexOf(')') > completion.lastIndexOf('(') + 1 &&
          completion.charAt(completion.length() - 1) == ')')
      {
        completion = completion.substring(0, completion.lastIndexOf('(') + 1);

      // include completions
      } else if (completion.endsWith("/>")){
        completion = completion.substring(0, completion.length() - 1);
      }
      return completion;
    }
    return super.getCompletion(proposal);
  }

  @Override
  protected String getMenu(ICompletionProposal proposal)
  {
    String menu = proposal.getDisplayString();
    return menu != null ? menu : StringUtils.EMPTY;
  }

  @Override
  protected String getInfo(ICompletionProposal proposal)
  {
    String info = proposal.getAdditionalProposalInfo();
    if (info == null){
      String display = proposal.getDisplayString();
      // only use the display if it has something more than just the name
      if (display != null && display.matches(".*\\W.*")){
        info = display;
      }
    }
    if (info == null){
      info = StringUtils.EMPTY;
    }
    return info;
  }
}
