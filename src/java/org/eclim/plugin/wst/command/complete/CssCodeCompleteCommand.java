/**
 * Copyright (c) 2005 - 2008
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class CssCodeCompleteCommand
  extends WstCodeCompleteCommand
{
  private static StructuredTextViewer viewer;

  /**
   * {@inheritDoc}
   * @see org.eclim.command.complete.AbstractCodeCompleteCommand#getContentAssistProcessor(CommandLine,String,String)
   */
  protected IContentAssistProcessor getContentAssistProcessor (
      CommandLine commandLine, String project, String file)
    throws Exception
  {
    return new CSSContentAssistProcessor();
  }

  /**
   * {@inheritDoc}
   * @see AbstractCodeCompleteCommand#getTextViewer(CommandLine,String,String)
   */
  protected ITextViewer getTextViewer (
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
