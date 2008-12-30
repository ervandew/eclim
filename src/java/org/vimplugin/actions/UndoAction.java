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

import org.eclipse.jface.action.Action;

public class UndoAction extends Action {

  /**
   * Constructs and updates the action.
   */
  public UndoAction() {
    super();
    update();
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.action.Action#run()
   */
  public void run() {
    //System.out.println("Undo Pressed\n");
  }

  public void update() {
    // setChecked(getTextEditor() != null &&
    // getTextEditor().showsHighlightRangeOnly());
    setEnabled(true);
  }
}
