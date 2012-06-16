/**
 * Copyright (C) 2005 - 2012  Eric Van Dewoestine
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
package org.vimplugin.editors;

import java.io.ByteArrayInputStream;

import org.eclim.logging.Logger;

import org.eclim.util.file.FileUtils;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IEventConsumer;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.TextPresentation;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

import org.eclipse.jface.viewers.ISelectionProvider;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.vimplugin.VimConnection;

/**
 * Source viewer implementation for gvim.
 *
 * @author Eric Van Dewoestine
 */
public class VimViewer
  implements ISourceViewer, ITextViewerExtension
{
  private static final Logger logger = Logger.getLogger(VimViewer.class);

  private int bufferID;
  private IDocument document;
  private VimText textWidget;
  private VimConnection vimConnection;

  public VimViewer(
      int bufferID, VimConnection vimConnection, Composite parent, int styles)
  {
    this.textWidget = new VimText(parent, styles);
    this.bufferID = bufferID;
    this.vimConnection = vimConnection;
  }

  // ISourceViewer

  /**
   * {@inheritDoc}
   * @see ISourceViewer#configure(SourceViewerConfiguration)
   */
  public void configure(SourceViewerConfiguration configuration)
  {
    // no-op
  }

  /**
   * {@inheritDoc}
   * @see ISourceViewer#setAnnotationHover(IAnnotationHover)
   */
  public void setAnnotationHover(IAnnotationHover annotationHover)
  {
    // no-op
    //System.out.println("## setAnnotationHover: " + annotationHover);
  }

  /**
   * {@inheritDoc}
   * @see ISourceViewer#setDocument(IDocument,IAnnotationModel)
   */
  public void setDocument(IDocument document, IAnnotationModel annotationModel)
  {
    //System.out.println("## setDocument: " + annotationModel);
    setDocument(document);
  }

  /**
   * {@inheritDoc}
   * @see ISourceViewer#setDocument(IDocument,IAnnotationModel,int,int)
   */
  public void setDocument(
      IDocument document, IAnnotationModel annotationModel,
      int modelRangeOffset, int modelRangeLength)
  {
    //System.out.println("## setDocument: " + annotationModel);
    setDocument(document, modelRangeOffset, modelRangeLength);
  }

  /**
   * {@inheritDoc}
   * @see ISourceViewer#getAnnotationModel()
   */
  public IAnnotationModel getAnnotationModel()
  {
    return null;
  }

  /**
   * {@inheritDoc}
   * @see ISourceViewer#setRangeIndicator(Annotation)
   */
  public void setRangeIndicator(Annotation rangeIndicator)
  {
    // FIXME
    //System.out.println("## setRangeIndicator: " + rangeIndicator);
  }

  /**
   * {@inheritDoc}
   * @see ISourceViewer#setRangeIndication(int,int,boolean)
   */
  public void setRangeIndication(int offset, int length, boolean moveCursor)
  {
    // FIXME
  }

  /**
   * {@inheritDoc}
   * @see ISourceViewer#getRangeIndication()
   */
  public IRegion getRangeIndication()
  {
    // FIXME
    return null;
  }

  /**
   * {@inheritDoc}
   * @see ISourceViewer#removeRangeIndication()
   */
  public void removeRangeIndication()
  {
  }

  /**
   * {@inheritDoc}
   * @see ISourceViewer#showAnnotations(boolean)
   */
  public void showAnnotations(boolean show)
  {
  }

  // ITextViewer

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#getTextWidget()
   */
  public StyledText getTextWidget()
  {
    return textWidget;
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#setUndoManager(IUndoManager)
   */
  public void setUndoManager(IUndoManager manager)
  {
    // no-op
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#setTextDoubleClickStrategy(ITextDoubleClickStrategy,String)
   */
  public void setTextDoubleClickStrategy(
      ITextDoubleClickStrategy strategy, String contentType)
  {
    // no-op
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#setAutoIndentStrategy(IAutoIndentStrategy,String)
   */
  @SuppressWarnings("deprecation")
  public void setAutoIndentStrategy(
      org.eclipse.jface.text.IAutoIndentStrategy strategy, String contentType)
  {
    // no-op
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#setTextHover(ITextHover,String)
   */
  public void setTextHover(ITextHover textViewerHover, String contentType)
  {
    // FIXME
    //System.out.println("## setTextHover: " + textViewerHover);
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#activatePlugins()
   */
  public void activatePlugins()
  {
    // no-op
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#resetPlugins()
   */
  public void resetPlugins()
  {
    // no-op
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#addViewportListener(IViewportListener)
   */
  public void addViewportListener(IViewportListener listener)
  {
    // FIXME
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#removeViewportListener(IViewportListener)
   */
  public void removeViewportListener(IViewportListener listener)
  {
    // FIXME
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#addTextListener(ITextListener)
   */
  public void addTextListener(ITextListener listener)
  {
    // FIXME
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#removeTextListener(ITextListener)
   */
  public void removeTextListener(ITextListener listener)
  {
    // FIXME
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#addTextInputListener(ITextInputListener)
   */
  public void addTextInputListener(ITextInputListener listener)
  {
    // FIXME
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#removeTextInputListener(ITextInputListener)
   */
  public void removeTextInputListener(ITextInputListener listener)
  {
    // FIXME
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#setDocument(IDocument)
   */
  public void setDocument(IDocument document)
  {
    this.document = document;
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#getDocument()
   */
  public IDocument getDocument()
  {
    return document;
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#setEventConsumer(IEventConsumer)
   */
  public void setEventConsumer(IEventConsumer consumer)
  {
    // FIXME
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#setEditable(boolean)
   */
  public void setEditable(boolean editable)
  {
    // FIXME
    //System.out.println("## setEditable: " + editable);
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#isEditable()
   */
  public boolean isEditable()
  {
    // FIXME
    return true;
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#setDocument(IDocument,int,int)
   */
  public void setDocument(
      IDocument document, int modelRangeOffset, int modelRangeLength)
  {
    // FIXME
    setDocument(document);
    //System.out.println("## setDocument: " + modelRangeOffset + ':' + modelRangeLength);
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#setVisibleRegion(int,int)
   */
  public void setVisibleRegion(int offset, int length)
  {
    // FIXME
    //System.out.println("## setVisibleRegion: " + offset + ':' + length);
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#resetVisibleRegion()
   */
  public void resetVisibleRegion()
  {
    // FIXME
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#getVisibleRegion()
   */
  public IRegion getVisibleRegion()
  {
    // FIXME
    // new Region(offset, length);
    return null;
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#overlapsWithVisibleRegion(int,int)
   */
  public boolean overlapsWithVisibleRegion(int offset, int length)
  {
    // FIXME
    return false;
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#changeTextPresentation(TextPresentation,boolean)
   */
  public void changeTextPresentation(
      TextPresentation presentation, boolean controlRedraw)
  {
    // no-op
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#invalidateTextPresentation()
   */
  public void invalidateTextPresentation()
  {
    // no-op
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#setTextColor(Color)
   */
  public void setTextColor(Color color)
  {
    // no-op
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#setTextColor(Color,int,int,boolean)
   */
  public void setTextColor(
      Color color, int offset, int length, boolean controlRedraw)
  {
    // no-op
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#getTextOperationTarget()
   */
  public ITextOperationTarget getTextOperationTarget()
  {
    // FIXME
    return null;
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#getFindReplaceTarget()
   */
  public IFindReplaceTarget getFindReplaceTarget()
  {
    // FIXME
    return null;
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#setDefaultPrefixes(String[],String)
   */
  public void setDefaultPrefixes(String[] defaultPrefixes, String contentType)
  {
    // FIXME
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#setIndentPrefixes(String[],String)
   */
  public void setIndentPrefixes(String[] indentPrefixes, String contentType)
  {
    // FIXME
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#setSelectedRange(int,int)
   */
  public void setSelectedRange(int offset, int length)
  {
    try{
      int[] pos = FileUtils.offsetToLineColumn(
          new ByteArrayInputStream(document.get().getBytes()), offset);
      vimConnection.command(bufferID, "setDot", "" + pos[0] + '/' + pos[1]);
      vimConnection.remotesend("<esc>:doautocmd CursorMoved<cr>");
      vimConnection.remotesend("<esc>:redraw!<cr>");
    }catch(Exception e){
      logger.error("Error setting selected range", e);
    }
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#getSelectedRange()
   */
  public Point getSelectedRange()
  {
    // FIXME
    return null;
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#getSelectionProvider()
   */
  public ISelectionProvider getSelectionProvider()
  {
    // FIXME
    return null;
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#revealRange(int,int)
   */
  public void revealRange(int offset, int length)
  {
    // FIXME
    //System.out.println("## revealRange: " + offset + ':' + length);
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#setTopIndex(int)
   */
  public void setTopIndex(int index)
  {
    // FIXME
    //System.out.println("## setTopIndex: " + index);
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#getTopIndex()
   */
  public int getTopIndex()
  {
    // FIXME
    return 0;
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#getTopIndexStartOffset()
   */
  public int getTopIndexStartOffset()
  {
    // FIXME
    return 0;
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#getBottomIndex()
   */
  public int getBottomIndex()
  {
    // FIXME
    return 0;
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#getBottomIndexEndOffset()
   */
  public int getBottomIndexEndOffset()
  {
    // FIXME
    return 0;
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.text.ITextViewer#getTopInset()
   */
  public int getTopInset()
  {
    // FIXME
    return 0;
  }

  // ITextViewerExtension

  /**
   * {@inheritDoc}
   * @see ITextViewerExtension#prependVerifyKeyListener(VerifyKeyListener)
   */
  public void prependVerifyKeyListener(VerifyKeyListener listener)
  {
  }

  /**
   * {@inheritDoc}
   * @see ITextViewerExtension#appendVerifyKeyListener(VerifyKeyListener)
   */
  public void appendVerifyKeyListener(VerifyKeyListener listener)
  {
  }

  /**
   * {@inheritDoc}
   * @see ITextViewerExtension#removeVerifyKeyListener(VerifyKeyListener)
   */
  public void removeVerifyKeyListener(VerifyKeyListener listener)
  {
  }

  /**
   * {@inheritDoc}
   * @see ITextViewerExtension#getControl()
   */
  public Control getControl()
  {
    return getTextWidget();
  }

  /**
   * {@inheritDoc}
   * @see ITextViewerExtension#setMark(int)
   */
  public void setMark(int arg0)
  {
  }

  /**
   * {@inheritDoc}
   * @see ITextViewerExtension#getMark()
   */
  public int getMark()
  {
    return -1;
  }

  /**
   * {@inheritDoc}
   * @see ITextViewerExtension#setRedraw(boolean)
   */
  public void setRedraw(boolean redraw)
  {
  }

  /**
   * {@inheritDoc}
   * @see ITextViewerExtension#getRewriteTarget()
   */
  public IRewriteTarget getRewriteTarget()
  {
    return null;
  }
}
