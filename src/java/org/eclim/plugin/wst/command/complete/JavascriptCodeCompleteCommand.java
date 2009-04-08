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

import org.eclim.annotation.Command;

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
 * @author Eric Van Dewoestine
 */
@Command(
  name = "javascript_complete",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "REQUIRED o offset ARG," +
    "REQUIRED e encoding ARG"
)
public class JavascriptCodeCompleteCommand
  extends AbstractCodeCompleteCommand
{
  /**
   * {@inheritDoc}
   * @see AbstractCodeCompleteCommand#getContentAssistProcessor(CommandLine,String,String)
   */
  protected IContentAssistProcessor getContentAssistProcessor(
      CommandLine commandLine, String project, String file)
    throws Exception
  {
    return new JavaScriptContentAssistProcessor();
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

  private void debug(IFile file)
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
