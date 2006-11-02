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

import org.eclim.command.CommandLine;

import org.eclim.command.complete.AbstractCodeCompleteCommand;

import org.eclim.eclipse.EclimPlugin;

import org.eclim.util.ProjectUtils;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import org.eclipse.core.runtime.IPath;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;

import org.eclipse.jface.text.contentassist.IContentAssistProcessor;

import org.eclipse.wst.javascript.ui.internal.common.contentassist.JavaScriptContentAssistProcessor;

import org.eclipse.wst.sse.core.StructuredModelManager;

import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;

import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;

import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;

/**
 * Command to handle css code completion requests.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class JavascriptCodeCompleteCommand
  extends AbstractCodeCompleteCommand
{
  /**
   * {@inheritDoc}
   * @see AbstractCodeCompleteCommand#getContentAssistProcessor(CommandLine,String,String)
   */
  protected IContentAssistProcessor getContentAssistProcessor (
      CommandLine commandLine, String project, String file)
    throws Exception
  {
    return new JavaScriptContentAssistProcessor();
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

org.eclipse.wst.sse.core.internal.FileBufferModelManager.getInstance();
    IStructuredDocument document =
      StructuredModelManager.getModelManager().createStructuredDocumentFor(ifile);
ITextFileBuffer buffer = FileBuffers.getTextFileBufferManager().getTextFileBuffer(ifile.getFullPath());
System.out.println("### document = " + document);
System.out.println("### document model = " + org.eclipse.wst.sse.core.internal.FileBufferModelManager.getInstance().getModel(document));
System.out.println("### model = " + StructuredModelManager.getModelManager().getModelForRead(document));
debug(ifile);

    StructuredTextViewer viewer =
      new StructuredTextViewer(EclimPlugin.getShell(), null, null, false, 0);
    viewer.setDocument(model.getStructuredDocument());
    return viewer;
  }

  private void debug (IFile file)
    throws Exception
  {
    IStructuredModel model = null;
    ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
    // see TextFileDocumentProvider#createFileInfo about why we use
    // IFile#getFullPath
    // here, not IFile#getLocation.
    IPath location = file.getFullPath();
    if (location != null) {
      bufferManager.connect(location, null);
System.out.println("### bufferManager = " + bufferManager);
      ITextFileBuffer buffer = bufferManager.getTextFileBuffer(location);
      if (buffer != null) {
        //DocumentInfo info = (DocumentInfo) fDocumentMap.get(buffer.getDocument());
        //if (info != null) {
          /*
           * Note: "info" being null at this point is a slight
           * error.
           *
           * The connect call from above (or at some time
           * earlier in the session) would have notified the
           * FileBufferMapper of the creation of the
           * corresponding text buffer and created the
           * DocumentInfo object for IStructuredDocuments.
           */
        //  info.selfConnected = true;
        //}
        /*
         * Check the document type. Although returning null for
         * unknown documents would be fair, try to get a model if
         * the document is at least a valid type.
         */
        IDocument bufferDocument = buffer.getDocument();
System.out.println("#### bufferDocument = " + bufferDocument);
        if (bufferDocument instanceof IStructuredDocument) {
System.out.println("### instance of IStructuredDocument");
          //model = getModel((IStructuredDocument) bufferDocument);
        }else{
System.out.println("### NOT instance of IStructuredDocument");
        }
      }
    }
  }
}
