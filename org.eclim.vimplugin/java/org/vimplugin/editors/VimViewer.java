/**
 * Copyright (C) 2005 - 2017  Eric Van Dewoestine
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

  @Override
  public void configure(SourceViewerConfiguration configuration)
  {
    // no-op
  }

  @Override
  public void setAnnotationHover(IAnnotationHover annotationHover)
  {
    // no-op
    //System.out.println("## setAnnotationHover: " + annotationHover);
  }

  @Override
  public void setDocument(IDocument document, IAnnotationModel annotationModel)
  {
    //System.out.println("## setDocument: " + annotationModel);
    setDocument(document);
  }

  @Override
  public void setDocument(
      IDocument document, IAnnotationModel annotationModel,
      int modelRangeOffset, int modelRangeLength)
  {
    //System.out.println("## setDocument: " + annotationModel);
    setDocument(document, modelRangeOffset, modelRangeLength);
  }

  @Override
  public IAnnotationModel getAnnotationModel()
  {
    return null;
  }

  @Override
  public void setRangeIndicator(Annotation rangeIndicator)
  {
    // FIXME
    //System.out.println("## setRangeIndicator: " + rangeIndicator);
  }

  @Override
  public void setRangeIndication(int offset, int length, boolean moveCursor)
  {
    // FIXME
  }

  @Override
  public IRegion getRangeIndication()
  {
    // FIXME
    return null;
  }

  @Override
  public void removeRangeIndication()
  {
  }

  @Override
  public void showAnnotations(boolean show)
  {
  }

  // ITextViewer

  @Override
  public StyledText getTextWidget()
  {
    return textWidget;
  }

  @Override
  public void setUndoManager(IUndoManager manager)
  {
    // no-op
  }

  @Override
  public void setTextDoubleClickStrategy(
      ITextDoubleClickStrategy strategy, String contentType)
  {
    // no-op
  }

  @Override
  @SuppressWarnings("deprecation")
  public void setAutoIndentStrategy(
      org.eclipse.jface.text.IAutoIndentStrategy strategy, String contentType)
  {
    // no-op
  }

  @Override
  public void setTextHover(ITextHover textViewerHover, String contentType)
  {
    // FIXME
    //System.out.println("## setTextHover: " + textViewerHover);
  }

  @Override
  public void activatePlugins()
  {
    // no-op
  }

  @Override
  public void resetPlugins()
  {
    // no-op
  }

  @Override
  public void addViewportListener(IViewportListener listener)
  {
    // FIXME
  }

  @Override
  public void removeViewportListener(IViewportListener listener)
  {
    // FIXME
  }

  @Override
  public void addTextListener(ITextListener listener)
  {
    // FIXME
  }

  @Override
  public void removeTextListener(ITextListener listener)
  {
    // FIXME
  }

  @Override
  public void addTextInputListener(ITextInputListener listener)
  {
    // FIXME
  }

  @Override
  public void removeTextInputListener(ITextInputListener listener)
  {
    // FIXME
  }

  @Override
  public void setDocument(IDocument document)
  {
    this.document = document;
  }

  @Override
  public IDocument getDocument()
  {
    return document;
  }

  @Override
  public void setEventConsumer(IEventConsumer consumer)
  {
    // FIXME
  }

  @Override
  public void setEditable(boolean editable)
  {
    // FIXME
    //System.out.println("## setEditable: " + editable);
  }

  @Override
  public boolean isEditable()
  {
    // FIXME
    return true;
  }

  @Override
  public void setDocument(
      IDocument document, int modelRangeOffset, int modelRangeLength)
  {
    // FIXME
    setDocument(document);
    //System.out.println("## setDocument: " + modelRangeOffset + ':' + modelRangeLength);
  }

  @Override
  public void setVisibleRegion(int offset, int length)
  {
    // FIXME
    //System.out.println("## setVisibleRegion: " + offset + ':' + length);
  }

  @Override
  public void resetVisibleRegion()
  {
    // FIXME
  }

  @Override
  public IRegion getVisibleRegion()
  {
    // FIXME
    // new Region(offset, length);
    return null;
  }

  @Override
  public boolean overlapsWithVisibleRegion(int offset, int length)
  {
    // FIXME
    return false;
  }

  @Override
  public void changeTextPresentation(
      TextPresentation presentation, boolean controlRedraw)
  {
    // no-op
  }

  @Override
  public void invalidateTextPresentation()
  {
    // no-op
  }

  @Override
  public void setTextColor(Color color)
  {
    // no-op
  }

  @Override
  public void setTextColor(
      Color color, int offset, int length, boolean controlRedraw)
  {
    // no-op
  }

  @Override
  public ITextOperationTarget getTextOperationTarget()
  {
    // FIXME
    return null;
  }

  @Override
  public IFindReplaceTarget getFindReplaceTarget()
  {
    // FIXME
    return null;
  }

  @Override
  public void setDefaultPrefixes(String[] defaultPrefixes, String contentType)
  {
    // FIXME
  }

  @Override
  public void setIndentPrefixes(String[] indentPrefixes, String contentType)
  {
    // FIXME
  }

  @Override
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

  @Override
  public Point getSelectedRange()
  {
    // FIXME
    return null;
  }

  @Override
  public ISelectionProvider getSelectionProvider()
  {
    // FIXME
    return null;
  }

  @Override
  public void revealRange(int offset, int length)
  {
    // FIXME
    //System.out.println("## revealRange: " + offset + ':' + length);
  }

  @Override
  public void setTopIndex(int index)
  {
    // FIXME
    //System.out.println("## setTopIndex: " + index);
  }

  @Override
  public int getTopIndex()
  {
    // FIXME
    return 0;
  }

  @Override
  public int getTopIndexStartOffset()
  {
    // FIXME
    return 0;
  }

  @Override
  public int getBottomIndex()
  {
    // FIXME
    return 0;
  }

  @Override
  public int getBottomIndexEndOffset()
  {
    // FIXME
    return 0;
  }

  @Override
  public int getTopInset()
  {
    // FIXME
    return 0;
  }

  // ITextViewerExtension

  @Override
  public void prependVerifyKeyListener(VerifyKeyListener listener)
  {
  }

  @Override
  public void appendVerifyKeyListener(VerifyKeyListener listener)
  {
  }

  @Override
  public void removeVerifyKeyListener(VerifyKeyListener listener)
  {
  }

  @Override
  public Control getControl()
  {
    return getTextWidget();
  }

  @Override
  public void setMark(int arg0)
  {
  }

  @Override
  public int getMark()
  {
    return -1;
  }

  @Override
  public void setRedraw(boolean redraw)
  {
  }

  @Override
  public IRewriteTarget getRewriteTarget()
  {
    return null;
  }
}
