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
package org.vimplugin.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.vimplugin.VimPlugin;

/**
 * Vimplugin Preference Page. The fields are explained in
 * {@link org.vimplugin.preferences.PreferenceConstants PreferenceConstants}.
 */
public class VimPreferences
  extends FieldEditorPreferencePage
  implements IWorkbenchPreferencePage
{
  /**
   * Initializes the preference store and sets a description for the dialog.
   */
  public VimPreferences() {
    super(FieldEditorPreferencePage.GRID);

    VimPlugin plugin = VimPlugin.getDefault();
    setPreferenceStore(plugin.getPreferenceStore());
    setDescription(plugin.getMessage("preferences.description"));
  }

  /**
   * Adds the fields.
   * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
   */
  @Override
  public void createFieldEditors() {
    VimPlugin plugin = VimPlugin.getDefault();
    addField(new BooleanFieldEditor(
          PreferenceConstants.P_EMBED,
          plugin.getMessage("preference.embed"),
          getFieldEditorParent()));
    addField(new StringFieldEditor(
          PreferenceConstants.P_PORT,
          plugin.getMessage("preference.port"),
          getFieldEditorParent()));
    addField(new FileFieldEditor(
          PreferenceConstants.P_GVIM,
          plugin.getMessage("preference.gvim"),
          true,
          getFieldEditorParent()));
    addField(new StringFieldEditor(
          PreferenceConstants.P_OPTS,
          plugin.getMessage("preference.gvim.args"),
          getFieldEditorParent()));
  }

  /**
   * does nothing.
   *
   * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
   */
  public void init(IWorkbench workbench) {
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.preference.IPreferencePage#performOk()
   */
  public boolean performOk()
  {
    boolean result = super.performOk();
    VimPlugin.getDefault().resetGvimState();
    return result;
  }
}
