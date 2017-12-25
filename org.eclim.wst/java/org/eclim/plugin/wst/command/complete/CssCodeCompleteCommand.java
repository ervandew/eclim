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

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;

import org.eclim.eclipse.EclimPlugin;

import org.eclim.plugin.core.command.complete.AbstractCodeCompleteCommand;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.text.contentassist.IContentAssistProcessor;

import org.eclipse.jface.text.source.ISourceViewer;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.wst.css.ui.internal.contentassist.CSSContentAssistProcessor;

import org.eclipse.wst.sse.core.StructuredModelManager;

import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;

import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;

/**
 * Command to handle css code completion requests.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "css_complete",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "REQUIRED o offset ARG," +
    "REQUIRED e encoding ARG"
)
public class CssCodeCompleteCommand
  extends WstCodeCompleteCommand
{
  private static StructuredTextViewer viewer;

  @Override
  protected IContentAssistProcessor getContentAssistProcessor(
      CommandLine commandLine, String project, String file)
  {
    return new CSSContentAssistProcessor();
  }

  @Override
  protected ISourceViewer getTextViewer(
      CommandLine commandLine, String project, String file)
  {
    IFile ifile = ProjectUtils.getFile(
        ProjectUtils.getProject(project, true), file);
    try{
      ifile.refreshLocal(IResource.DEPTH_INFINITE, null);
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }

    IStructuredModel model = null;
    try{
      model = StructuredModelManager.getModelManager().getModelForRead(ifile);
    }catch(IOException ioe){
      throw new RuntimeException(ioe);
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }

    if (viewer == null) {
      viewer = new StructuredTextViewer(
          EclimPlugin.getShell(), null, null, false, 0){
        protected void createControl(Composite parent, int styles)
        {
          // no-op to prevent possible deadlock in native method on windows.
        }
      };
    }
    viewer.setDocument(model.getStructuredDocument());
    return viewer;
  }
}
