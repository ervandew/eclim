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

public class CopyAction extends Action {

  /**
   * Constructs and updates the action.
   */
  public CopyAction() {
    super();
    update();
  }

  /*
   * (non-Javadoc) Method declared on IAction
   */
  public void run() {
    //System.out.println("Copy Pressed\n");
  }

  /*
   * (non-Javadoc) Method declared on TextEditorAction
   */
  public void update() {
    // setChecked(getTextEditor() != null &&
    // getTextEditor().showsHighlightRangeOnly());
    setEnabled(true);
  }
}
