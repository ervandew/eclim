
package org.eclim.plugin.adt.command.complete;

import org.eclim.eclipse.EclimPlugin;

import org.eclim.eclipse.jface.text.DummySelectionProvider;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;

import org.eclipse.jface.viewers.ISelectionProvider;

import org.eclipse.swt.custom.StyledText;

import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;

import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;

public class DummyStructuredTextViewer
  extends StructuredTextViewer
{

  public IStructuredDocument document;
  public ISelectionProvider selectionProvider;

  public DummyStructuredTextViewer(IStructuredDocument doc, int offset, int length)
  {
    super(new StyledText(EclimPlugin.getShell(), 0), null, null, false, 0);

    this.document = doc;
    selectionProvider = new DummySelectionProvider(
        new TextSelection(document, offset, length));
  }

  @Override
  public IDocument getDocument()
  {
    return document;
  }

  @Override
  public ISelectionProvider getSelectionProvider()
  {
    return selectionProvider;
  }

}
