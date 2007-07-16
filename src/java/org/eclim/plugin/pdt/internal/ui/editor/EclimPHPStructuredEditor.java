/**
 * Copyright (c) 2005 - 2007
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
package org.eclim.plugin.pdt.internal.ui.editor;

import org.eclim.eclipse.EclimPlugin;

import org.eclipse.core.resources.IResource;

import org.eclipse.jface.text.IDocument;

import org.eclipse.php.internal.ui.editor.PHPStructuredEditor;
import org.eclipse.php.internal.ui.editor.PHPStructuredTextViewer;

import org.eclipse.wst.sse.ui.internal.StructuredResourceMarkerAnnotationModel;
import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;

/**
 * Extension to eclipse PHPStructuredEditor.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class EclimPHPStructuredEditor
  extends PHPStructuredEditor
{
  private StructuredTextViewer viewer;
  private IResource resource;
  private IDocument document;

  public EclimPHPStructuredEditor (IResource resource, IDocument document)
  {
    super();
    this.resource = resource;
    this.document = document;
  }

  public StructuredTextViewer getTextViewer ()
  {
    if(viewer == null && document != null){
      viewer = new PHPStructuredTextViewer(
          EclimPlugin.getShell(), null, null, false, 0);
      viewer.setDocument(
          document, new StructuredResourceMarkerAnnotationModel(resource));
    }
    return viewer;
  }
}
