/*
 * Vimplugin
 *
 * Copyright (c) 2007 - 2017 by The Vimplugin Project.
 *
 * Released under the GNU General Public License
 * with ABSOLUTELY NO WARRANTY.
 *
 * See the file COPYING for more information.
 */
package org.vimplugin.preferences;

import org.apache.tools.ant.taskdefs.condition.Os;

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
    if (!Os.isFamily(Os.FAMILY_MAC)){
      addField(new BooleanFieldEditor(
            PreferenceConstants.P_EMBED,
            plugin.getMessage("preference.embed"),
            getFieldEditorParent()));
    }
    addField(new BooleanFieldEditor(
          PreferenceConstants.P_TABBED,
          plugin.getMessage("preference.tabbed"),
          getFieldEditorParent()));
    addField(new BooleanFieldEditor(
          PreferenceConstants.P_START_ECLIMD,
          plugin.getMessage("preference.eclimd.start"),
          getFieldEditorParent()));
    addField(new BooleanFieldEditor(
          PreferenceConstants.P_FOCUS_AUTO_CLICK,
          plugin.getMessage("preference.focus.click"),
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

  @Override
  public boolean performOk()
  {
    boolean result = super.performOk();
    VimPlugin.getDefault().resetGvimState();
    return result;
  }
}
