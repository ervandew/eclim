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
package org.vimplugin;

import org.eclipse.jface.action.*;
import org.eclipse.ui.*;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.texteditor.*;

import org.vimplugin.actions.*;

/**
 * Contributes interesting Java actions to the desktop's Edit menu and the tool
 * bar. <i>TODO: develpment sleeps here. More work needed or delete it!</i>
 */
public class VimActionContributor extends EditorActionBarContributor {

  protected RetargetTextEditorAction fContentAssistProposal;
  protected RetargetTextEditorAction fContentAssistTip;

  /**
   * Default constructor.
   */
  public VimActionContributor() {
    super();
    /*
     * fContentAssistProposal= new
     * RetargetTextEditorAction(VimEditorMessages.getResourceBundle(),
     * "ContentAssistProposal."); //$NON-NLS-1$
     * fContentAssistProposal.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
     * fContentAssistTip= new
     * RetargetTextEditorAction(VimEditorMessages.getResourceBundle(),
     * "ContentAssistTip."); //$NON-NLS-1$
     * fContentAssistTip.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_CONTEXT_INFORMATION);
     */
    // ActionFactory.IWorkbenchAction act =
    // ActionFactory.FIND.create(this.getPage().getWorkbenchWindow());
  }

  /*
   * @see IEditorActionBarContributor#init(IActionBars)
   */
  public void init(IActionBars bars) {
    super.init(bars);
    IMenuManager menuManager = bars.getMenuManager();
    IMenuManager editMenu = menuManager
        .findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
    if (editMenu != null) {
      editMenu.add(new Separator());
      // editMenu.add(fContentAssistProposal);
      // editMenu.add(fContentAssistTip);
    }
    IActionBars actionBars = getActionBars();
    actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(),
        new UndoAction());
    actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(),
        new UndoAction());
    actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(),
        new RedoAction());
    actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(),
        new CopyAction());
    actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(),
        new PasteAction());
    actionBars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(),
        new UndoAction());
    /*
     * IToolBarManager toolBarManager= bars.getToolBarManager(); if
     * (toolBarManager != null) { toolBarManager.add(new Separator());
     * toolBarManager.add(fTogglePresentation); }
     */
  }

  /**
   * Do the actual work for our part of {@link #setActiveEditor(IEditorPart)}.
   *
   * @param part The Editor
   */
  private void doSetActiveEditor(IEditorPart part) {
    super.setActiveEditor(part);
    // ITextEditor editor = null;
    // if (part instanceof ITextEditor)
    //  editor = (ITextEditor) part;
    // fContentAssistProposal.setAction(getAction(editor,
    // "ContentAssistProposal")); //$NON-NLS-1$
    // fContentAssistTip.setAction(getAction(editor, "ContentAssistTip"));
    // //$NON-NLS-1$
  }

  /*
   * @see IEditorActionBarContributor#setActiveEditor(IEditorPart)
   */
  public void setActiveEditor(IEditorPart part) {
    doSetActiveEditor(part);
  }

  /*
   * @see IEditorActionBarContributor#dispose()
   */
  public void dispose() {
    doSetActiveEditor(null);
    super.dispose();
  }
}
