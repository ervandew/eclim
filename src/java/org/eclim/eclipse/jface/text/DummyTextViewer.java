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
package org.eclim.eclipse.jface.text;

import org.eclipse.jface.text.IAutoIndentStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IEventConsumer;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.TextSelection;

import org.eclipse.jface.viewers.ISelectionProvider;

import org.eclipse.swt.custom.StyledText;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;

/**
 * Dummy ITextViewer implementation that provides basic functionality for
 * eclipse classes that require an ITextViewer.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class DummyTextViewer
  implements ITextViewer
{
  public IDocument document;
  public ISelectionProvider selectionProvider;

  public DummyTextViewer (IDocument _document, int _offset, int _length)
  {
    document = _document;
    selectionProvider = new DummySelectionProvider(
        new TextSelection(_document, _offset, _length));
  }

  public StyledText getTextWidget()
  {
    return null;
  }

  public void setUndoManager(IUndoManager undoManager)
  {
  }

  public void setTextDoubleClickStrategy(
      ITextDoubleClickStrategy strategy, String contentType)
  {
  }

  public void setAutoIndentStrategy(IAutoIndentStrategy strategy, String contentType)
  {
  }

  public void setTextHover(ITextHover textViewerHover, String contentType)
  {
  }

  public void activatePlugins()
  {
  }

  public void resetPlugins()
  {
  }

  public void addViewportListener(IViewportListener listener)
  {
  }

  public void removeViewportListener(IViewportListener listener)
  {
  }

  public void addTextListener(ITextListener listener)
  {
  }

  public void removeTextListener(ITextListener listener)
  {
  }

  public void addTextInputListener(ITextInputListener listener)
  {
  }

  public void removeTextInputListener(ITextInputListener listener)
  {
  }

  public void setDocument(IDocument document)
  {
  }

  public IDocument getDocument()
  {
    return document;
  }

  public void setEventConsumer(IEventConsumer consumer)
  {
  }

  public void setEditable(boolean editable)
  {
  }

  public boolean isEditable()
  {
    return true;
  }

  public void setDocument(IDocument document, int modelRangeOffset, int modelRangeLength)
  {
  }

  public void setVisibleRegion(int offset, int length)
  {
  }

  public void resetVisibleRegion()
  {
  }

  public IRegion getVisibleRegion()
  {
    return null;
  }

  public boolean overlapsWithVisibleRegion(int offset, int length)
  {
    return false;
  }

  public void changeTextPresentation(TextPresentation presentation, boolean controlRedraw)
  {
  }

  public void invalidateTextPresentation()
  {
  }

  public void setTextColor(Color color)
  {
  }

  public void setTextColor(Color color, int offset, int length, boolean controlRedraw)
  {
  }

  public ITextOperationTarget getTextOperationTarget()
  {
    return null;
  }

  public IFindReplaceTarget getFindReplaceTarget()
  {
    return null;
  }

  public void setDefaultPrefixes(String[] defaultPrefixes, String contentType)
  {
  }

  public void setIndentPrefixes(String[] indentPrefixes, String contentType)
  {
  }

  public void setSelectedRange(int offset, int length)
  {
  }

  public Point getSelectedRange()
  {
    return null;
  }

  public ISelectionProvider getSelectionProvider()
  {
    return selectionProvider;
  }

  public void revealRange(int offset, int length)
  {
  }

  public void setTopIndex(int index)
  {
  }

  public int getTopIndex()
  {
    return -1;
  }

  public int getTopIndexStartOffset()
  {
    return -1;
  }

  public int getBottomIndex()
  {
    return -1;
  }

  public int getBottomIndexEndOffset()
  {
    return -1;
  }

  public int getTopInset()
  {
    return -1;
  }
}
