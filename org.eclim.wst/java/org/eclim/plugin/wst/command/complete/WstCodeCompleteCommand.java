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
package org.eclim.plugin.wst.command.complete;

import java.io.IOException;

import org.eclim.command.CommandLine;

import org.eclim.eclipse.EclimPlugin;

import org.eclim.plugin.core.command.complete.AbstractCodeCompleteCommand;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclipse.core.resources.IFile;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;

import org.eclipse.swt.graphics.Point;

import org.eclipse.wst.sse.core.StructuredModelManager;

import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;

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
    IContentAssistProcessor processor =
      getContentAssistProcessor(commandLine, project, file);

    IFile ifile = ProjectUtils.getFile(
        ProjectUtils.getProject(project, true), file);

    IStructuredModel model = null;
    try{
      model = StructuredModelManager.getModelManager().getModelForRead(ifile);
    }catch(IOException ioe){
      throw new RuntimeException(ioe);
    }catch(CoreException ce){
      throw new RuntimeException(ce);
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
      // note: non-zero length can break html completion.
      viewer.setSelectedRange(offset, 0);

      ICompletionProposal[] proposals =
        processor.computeCompletionProposals(viewer, offset);
      model.releaseFromRead();
      return proposals;
    }
    return new ICompletionProposal[0];
  }
}
