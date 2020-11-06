/**
 * Copyright (C) 2005 - 2020  Eric Van Dewoestine
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
package org.eclim.plugin.wst.command.complete;

import java.io.IOException;

import java.lang.reflect.InvocationTargetException;

import org.eclim.command.CommandLine;

import org.eclim.eclipse.EclimPlugin;

import org.eclim.plugin.core.command.complete.AbstractCodeCompleteCommand;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclipse.core.resources.IFile;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.text.BadLocationException;

import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;

import org.eclipse.swt.graphics.Point;

import org.eclipse.wst.sse.core.StructuredModelManager;

import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;

import org.eclipse.wst.sse.ui.StructuredTextViewerConfiguration;

import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;

/**
 * Abstract base for code completion using the wst.
 *
 * @author Eric Van Dewoestine
 */
public abstract class WstCodeCompleteCommand
  extends AbstractCodeCompleteCommand
{
  @Override
  protected ICompletionProposal[] getCompletionProposals(
      CommandLine commandLine, String project, String file, int offset)
  {
    IFile ifile = ProjectUtils.getFile(
        ProjectUtils.getProject(project, true), file);

    IStructuredModel model = null;
    try{
      model = StructuredModelManager.getModelManager().getModelForRead(ifile);
    }catch(CoreException|IOException e){
      throw new RuntimeException(e);
    }

    if (model != null){
      StructuredTextViewer viewer = new StructuredTextViewer(
          EclimPlugin.getShell(), null, null, false, 0){
        private Point point;
        public Point getSelectedRange()
        {
          return point;
        }
        public void setSelectedRange(int x, int y)
        {
          point = new Point(x, y);
        }
      };
      viewer.setDocument(model.getStructuredDocument());

      Class<? extends StructuredTextViewerConfiguration> configClass =
        getViewerConfigurationClass();

      try{
        StructuredTextViewerConfiguration configuration =
          configClass.getDeclaredConstructor().newInstance();

        ContentAssistant assistant = (ContentAssistant)
          configuration.getContentAssistant(viewer);

        viewer.configure(configuration);
        // note: non-zero length can break html completion.
        viewer.setSelectedRange(offset, 0);

        String partitionType = viewer.getDocument().getPartition(offset).getType();
        IContentAssistProcessor processor =
          assistant.getContentAssistProcessor(partitionType);

        ICompletionProposal[] proposals =
          processor.computeCompletionProposals(viewer, offset);

        model.releaseFromRead();

        return proposals;
      }catch(
          BadLocationException|
          IllegalAccessException|
          InstantiationException|
          InvocationTargetException|
          NoSuchMethodException ex)
      {
        throw new RuntimeException(ex);
      }
    }
    return new ICompletionProposal[0];
  }

  /**
   * Gets the StructuredTextViewerConfiguration class to use.
   *
   * @return The Class of type StructuredTextViewerConfiguration.
   */
  protected Class<? extends StructuredTextViewerConfiguration>
    getViewerConfigurationClass()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  protected boolean acceptProposal(ICompletionProposal proposal)
  {
    // with language server installed, eclipse returns completions of type
    // org.eclipse.lsp4e.operations.completion.LSCompletionProposal, which for
    // some reason are less context aware, so filter those out and just return
    // the wst completions.
    // TODO: figure out if there is a way to prevent lsp completions in the
    // first place.
    if (proposal.getClass().getName().indexOf("lsp4e") != -1){
      return false;
    }
    return true;
  }
}
