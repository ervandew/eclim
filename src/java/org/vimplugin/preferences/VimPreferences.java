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
import org.vimplugin.VimConnection;
import org.vimplugin.VimPlugin;

/**
 * Vimplugin Preference Page. The fields are explained in
 * {@link org.vimplugin.preferences.PreferenceConstants PreferenceConstants}. The
 * preferecences have to be adjusted to the settings vim was started with (e.g.
 *
 * <pre>
 * vim -nb:{hostname}:{addr}:{password}
 * </pre> ).
 */

// TODO: Move all strings to a properties file.
public class VimPreferences extends FieldEditorPreferencePage implements
    IWorkbenchPreferencePage {

  //private final StringFieldEditor[] hotkeys;
  //private final ComboFieldEditor[] combos;

  /**
   * Initializes the preference store and sets a description for the dialog.
   */
  public VimPreferences() {
    super(FieldEditorPreferencePage.GRID);
    //hotkeys = new StringFieldEditor[5];
    //combos = new ComboFieldEditor[5];

    setPreferenceStore(VimPlugin.getDefault().getPreferenceStore());
    setDescription("General Settings");
  }

  /**
   * Adds the fields.
   * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
   */
  @Override
  public void createFieldEditors() {
    addField(new BooleanFieldEditor(PreferenceConstants.P_EMBD,
        "Embed Vim: (Vim 7.1 on Linux and Windows only)",
        getFieldEditorParent()));
    addField(new StringFieldEditor(PreferenceConstants.P_PORT, "Port:",
        getFieldEditorParent()));
    addField(new StringFieldEditor(PreferenceConstants.P_HOST, "Host:",
        getFieldEditorParent()));
    addField(new StringFieldEditor(PreferenceConstants.P_PASS, "Password:",
        getFieldEditorParent()));
    addField(new FileFieldEditor(PreferenceConstants.P_GVIM,
        "Path to gvim:", true, getFieldEditorParent()));
    addField(new StringFieldEditor(PreferenceConstants.P_OPTS,
        "additional Parameters:", getFieldEditorParent()));
    addField(new BooleanFieldEditor(PreferenceConstants.P_DEBUG,
        "Debug to stdout:", getFieldEditorParent()));

    /*for (int i = 0; i < 5; i++) {
      hotkeys[i] = new StringFieldEditor(PreferenceConstants.P_KEYS[i],
          "Hotkey "+i, getFieldEditorParent());
      combos[i] = new ComboFieldEditor(PreferenceConstants.P_COMMANDS[i],
          "Command "+i, getCommands(),getFieldEditorParent());
    }

    for (int i = 0; i < 5; i++) {
      addField(hotkeys[i]);
      addField(combos[i]);
    }*/

  }

  /**
   * calculate defined commands
   * @return an array suitable for the {@link ComboFieldEditor} in the preferencepage.
   */
  /*private String[][] getCommands() {
    ICommandService com = (ICommandService) PlatformUI.getWorkbench()
        .getService(ICommandService.class);

    Command[] commands = com.getDefinedCommands();

    //filter out duplicates
    HashSet<String> nodupes = new HashSet<String>();

    for (int i = 0; i < commands.length; i++) {
      Command command = commands[i];
      if (command.getId().startsWith("org.eclipse.ui.project")) {
        nodupes.add(command.getId());
      }
    }

    String[][] commandpairs = new String[nodupes.size()][2];
    Object[] a = nodupes.toArray();
    System.out.println("These are the command ids: "+Arrays.toString(a));

    for (int i = 0; i<a.length;i++) {
      commandpairs[i][0]=(String)a[i];
      commandpairs[i][1]=(String)a[i];
    }

    return commandpairs;
  }*/

  /**
   * does nothing.
   *
   * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
   */
  public void init(IWorkbench workbench) {
  }

  @Override
  public boolean performOk() {
    int vimid = VimPlugin.getDefault().getDefaultVimServer();
    VimConnection vc = VimPlugin.getDefault().getVimserver(vimid).getVc();

    String key, command = null;
    for (int i = 0; i < 5; i++) {
      key = VimPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.P_KEYS[i]);
      command = VimPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.P_COMMANDS[i]);
      vc.getRegistry().setEclipseCommandHandler(vc, key, command,i);
    }

    return super.performOk();
  }
}
