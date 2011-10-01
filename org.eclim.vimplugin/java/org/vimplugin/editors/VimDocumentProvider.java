/*
 * Vimplugin
 *
 * Copyright (c) 2007 by The Vimplugin Project.
 *
 * Released under the GNU General Public License
 * with ABSOLUTELY NO WARRANTY.
 *
 * See the file COPYING for more information.
 */
package org.vimplugin.editors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.editors.text.FileDocumentProvider;

public class VimDocumentProvider extends FileDocumentProvider {

  private IDocument document;

  protected IDocument createDocument(Object element) throws CoreException {
    document = super.createDocument(element);
    return document;
  }
}
