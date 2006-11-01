/**
 * Copyright (c) 2005 - 2006
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

import org.eclim.command.complete.AbstractCodeCompleteCommand;

import org.eclim.eclipse.EclimPlugin;

import org.eclim.util.ProjectUtils;

import org.eclipse.core.resources.IFile;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;

import org.eclipse.jface.text.contentassist.IContentAssistProcessor;

import org.eclipse.wst.css.ui.internal.contentassist.CSSContentAssistProcessor;

import org.eclipse.wst.sse.core.StructuredModelManager;

import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;

import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;

/**
 * Command to handle css code completion requests.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class CssCodeCompleteCommand
  extends AbstractCodeCompleteCommand
{
  /**
   * {@inheritDoc}
   * @see AbstractCodeCompleteCommand#getContentAssistProcessor(String,String)
   */
  protected IContentAssistProcessor getContentAssistProcessor (
      String project, String file)
    throws Exception
  {
    return new CSSContentAssistProcessor();
  }

  /**
   * {@inheritDoc}
   * @see AbstractCodeCompleteCommand#getTextViewer(String, String,int)
   */
  protected ITextViewer getTextViewer (String project, String file, int offset)
    throws Exception
  {
    IFile ifile = ProjectUtils.getFile(ProjectUtils.getProject(project), file);
    IStructuredModel model =
      StructuredModelManager.getModelManager().getModelForRead(ifile);
//System.out.println("### file.exists = " + ifile.exists());
//System.out.println("### file.fullPath = " + ifile.getFullPath());
//System.out.println("### model = " + model);
    StructuredTextViewer viewer =
      new StructuredTextViewer(EclimPlugin.getShell(), null, null, false, 0);
    viewer.setDocument(model.getStructuredDocument());
    return viewer;
  }
}
