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
package org.eclim.plugin.wst.command.complete;

import org.eclim.command.CommandLine;

import org.eclim.command.complete.AbstractCodeCompleteCommand;

import org.eclim.eclipse.EclimPlugin;

import org.eclim.util.ProjectUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import org.eclipse.jface.text.ITextViewer;

import org.eclipse.jface.text.contentassist.IContentAssistProcessor;

import org.eclipse.wst.css.ui.internal.contentassist.CSSContentAssistProcessor;

import org.eclipse.wst.sse.core.StructuredModelManager;

import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;

import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;

/**
 * Command to handle css code completion requests.
 *
 * @author Eric Van Dewoestine
 */
public class CssCodeCompleteCommand
  extends WstCodeCompleteCommand
{
  private static StructuredTextViewer viewer;

  /**
   * {@inheritDoc}
   * @see org.eclim.command.complete.AbstractCodeCompleteCommand#getContentAssistProcessor(CommandLine,String,String)
   */
  protected IContentAssistProcessor getContentAssistProcessor(
      CommandLine commandLine, String project, String file)
    throws Exception
  {
    return new CSSContentAssistProcessor();
  }

  /**
   * {@inheritDoc}
   * @see AbstractCodeCompleteCommand#getTextViewer(CommandLine,String,String)
   */
  protected ITextViewer getTextViewer(
      CommandLine commandLine, String project, String file)
    throws Exception
  {
    IFile ifile = ProjectUtils.getFile(
        ProjectUtils.getProject(project, true), file);
    ifile.refreshLocal(IResource.DEPTH_INFINITE, null);

    IStructuredModel model =
      StructuredModelManager.getModelManager().getModelForRead(ifile);

    if (viewer == null) {
      viewer = new StructuredTextViewer(
          EclimPlugin.getShell(), null, null, false, 0);
    }
    viewer.setDocument(model.getStructuredDocument());
    return viewer;
  }
}
