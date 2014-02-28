/**
 * Copyright (C) 2005 - 2013  Eric Van Dewoestine
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
package org.eclim.eclipse.jface.text;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IEventConsumer;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.TextSelection;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

import org.eclipse.jface.viewers.ISelectionProvider;

import org.eclipse.swt.custom.StyledText;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;

/**
 * Dummy ITextViewer implementation that provides basic functionality for
 * eclipse classes that require an ITextViewer.
 *
 * @author Eric Van Dewoestine
 */
public class DummyTextViewer
  implements ISourceViewer
{
  public IDocument document;
  public ISelectionProvider selectionProvider;

  public DummyTextViewer (IDocument document, int offset, int length)
  {
    this.document = document;
    selectionProvider = new DummySelectionProvider(
        new TextSelection(document, offset, length));
  }

  // ITextViewer

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

  @SuppressWarnings("deprecation")
  public void setAutoIndentStrategy(
      org.eclipse.jface.text.IAutoIndentStrategy strategy, String contentType)
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
    this.document = document;
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

  public void setDocument(
      IDocument document, int modelRangeOffset, int modelRangeLength)
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

  public void changeTextPresentation(
      TextPresentation presentation, boolean controlRedraw)
  {
  }

  public void invalidateTextPresentation()
  {
  }

  public void setTextColor(Color color)
  {
  }

  public void setTextColor(
      Color color, int offset, int length, boolean controlRedraw)
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
    return new Point(-1, -1);
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

  // ISourceViewer

  private IAnnotationModel annotationModel;

  public void configure(SourceViewerConfiguration configuration)
  {
  }

  public void setAnnotationHover(IAnnotationHover annotationHover)
  {
  }

  public void setDocument(IDocument document, IAnnotationModel annotationModel)
  {
    this.document = document;
    this.annotationModel = annotationModel;
  }

  public void setDocument(
      IDocument document, IAnnotationModel annotationModel,
      int modelRangeOffset, int modelRangeLength)
  {
    this.document = document;
    this.annotationModel = annotationModel;
  }

  public IAnnotationModel getAnnotationModel()
  {
    return annotationModel;
  }

  public void setRangeIndicator(Annotation rangeIndicator)
  {
  }

  public void setRangeIndication(int offset, int length, boolean moveCursor)
  {
  }

  public IRegion getRangeIndication()
  {
    return null;
  }

  public void removeRangeIndication()
  {
  }

  public void showAnnotations(boolean show){
  }
}
