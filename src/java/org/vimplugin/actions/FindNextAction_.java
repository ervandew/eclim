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
package org.vimplugin.actions;

import org.eclipse.ui.texteditor.TextEditorAction;
import org.vimplugin.VimPluginStrings;

public class FindNextAction_ extends TextEditorAction {

  /**
   * Constructs and updates the action.
   */
  public FindNextAction_() {
    super(VimPluginStrings.getResourceBundle(), "FindNext.", null); //$NON-NLS-1$
    update();
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.action.Action#run()
   */
  @Override
  public void run() {
    /*
     * ITextEditor editor= getTextEditor(); editor.resetHighlightRange();
     * boolean show= editor.showsHighlightRangeOnly(); setChecked(!show);
     * editor.showHighlightRangeOnly(!show);
     */
    System.out.println("Find Next Pressed\n");
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.texteditor.TextEditorAction#update()
   */
  @Override
  public void update() {
    // setChecked(getTextEditor() != null &&
    // getTextEditor().showsHighlightRangeOnly());
    setEnabled(true);
  }
}
