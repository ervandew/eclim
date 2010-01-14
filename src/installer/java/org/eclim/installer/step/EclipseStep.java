/**
 * Copyright (C) 2005 - 2010  Eric Van Dewoestine
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
package org.eclim.installer.step;

import java.awt.Component;
import java.awt.FlowLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.FilenameFilter;

import java.util.Arrays;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.io.FilenameUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;

import org.apache.tools.ant.taskdefs.condition.Os;

import org.formic.Installer;

import org.formic.util.dialog.gui.GuiDialogs;

import org.formic.wizard.form.GuiForm;
import org.formic.wizard.form.Validator;

import org.formic.wizard.form.gui.component.FileChooser;

import org.formic.wizard.form.validator.ValidatorBuilder;

import org.formic.wizard.step.AbstractGuiStep;

/**
 * Step for specifying location of eclipse installation.
 *
 * @author Eric Van Dewoestine
 */
public class EclipseStep
  extends AbstractGuiStep
{
  private static final String[] WINDOWS_ECLIPSES = {
    "C:/eclipse",
    "C:/Program Files/eclipse",
  };

  private static final String[] UNIX_ECLIPSES = {
    "/opt/eclipse",
    "/usr/lib/eclipse",
    "/usr/lib/eclipse-3.3",
    "/usr/local/eclipse",
    "/usr/share/eclipse",
    SystemUtils.USER_HOME + "/eclipse",
    "/Applications/eclipse"
  };

  private boolean createMissingPanelAdded = false;
  private JCheckBox overridePluginsCheckBox;

  /**
   * Constructs the welcome step.
   */
  public EclipseStep (String name, Properties properties)
  {
    super(name, properties);
  }

  /**
   * {@inheritDoc}
   * @see org.formic.wizard.step.GuiStep#init()
   */
  public Component init ()
  {
    final JPanel panel =
      new JPanel(new MigLayout("wrap 2, fillx", "[growprio 0] [fill]"));
    GuiForm form = createForm();

    String home = fieldName("home");

    final FileChooser eclipseHomeChooser =
      new FileChooser(JFileChooser.DIRECTORIES_ONLY);

    panel.add(form.createMessagePanel(), "span");
    panel.add(new JLabel(Installer.getString(home)));
    panel.add(eclipseHomeChooser);

    form.bind(home, eclipseHomeChooser.getTextField(),
        new ValidatorBuilder()
        .required()
        .isDirectory()
        .validator(new EclipseHomeValidator())
        .validator(new EclipseHomeWritableValidator())
        .validator());
    String eclipseHomeDefault = getDefaultEclipseHome();
    eclipseHomeChooser.getTextField().setText(eclipseHomeDefault);

    // On systems where eclispe was installed via a package manger, the user may
    // need to install the plugins in an alternate location.
    if(!Os.isFamily(Os.FAMILY_WINDOWS)){
      String local = fieldName("local");
      String eclipseLocalDefault = eclipseHomeDefault;
      final FileChooser eclipseLocalChooser =
         new FileChooser(JFileChooser.DIRECTORIES_ONLY);
      form.bind(local, eclipseLocalChooser.getTextField(),
        new ValidatorBuilder().required().isDirectory().isWritable().validator());

      final JTextField eclipseHomeField = eclipseHomeChooser.getTextField();
      final JTextField eclipseLocalField = eclipseLocalChooser.getTextField();
      eclipseLocalField.setText(eclipseLocalDefault);
      eclipseLocalChooser.getButton().setEnabled(false);

      String hasLocal = fieldName("hasLocal");
      overridePluginsCheckBox = new JCheckBox(Installer.getString(hasLocal));

      final JPanel createMissingPanel = new JPanel(new FlowLayout());
      final JButton createMissingButton =
        new JButton(new CreatePluginDirsActions(eclipseLocalField));
      String label = Installer.getString("eclipse.createMissing");
      createMissingPanel.add(new JLabel(label));
      createMissingPanel.add(createMissingButton);

      eclipseLocalField.setEnabled(false);
      overridePluginsCheckBox.addActionListener(new ActionListener(){
        public void actionPerformed (ActionEvent e){
          boolean selected = ((JCheckBox)e.getSource()).isSelected();
          JTextField eclipseLocalField = eclipseLocalChooser.getTextField();
          eclipseLocalField.setEnabled(selected);
          eclipseLocalChooser.getButton().setEnabled(selected);

          // force home field to re-validate
          JTextField eclipseHomeField = eclipseHomeChooser.getTextField();
          eclipseHomeField.setText(eclipseHomeField.getText());

          if(!selected){
            // force eclipse local text field to update itself
            eclipseHomeField = eclipseHomeChooser.getTextField();
            eclipseHomeField.setText(eclipseHomeField.getText());
            createMissingButton.setEnabled(false);
          }else{
            eclipseLocalField.grabFocus();
            final File eclipseLocalPath = getDefaultEclipseLocalPath();
            if(eclipseLocalPath != null){
              final String[] path = {"configuration", "eclipse"};

              final String eclipseLocalFieldText =
                eclipseLocalPath.getAbsolutePath() + '/' +
              StringUtils.join(path, '/');
                eclipseLocalField.setText(eclipseLocalFieldText);
                eclipseLocalField.setCaretPosition(0);

              // see if any of the path parts are missing on the filesystem
              boolean missing = false;
              String missingPath = eclipseLocalPath.getAbsolutePath();
              for (int ii = 0; ii < path.length; ii++){
                if(!new File(missingPath + '/' + path[ii]).exists()){
                  missing = true;
                  missingPath =
                    eclipseLocalFieldText.substring(missingPath.length() + 1);
                  break;
                }
                missingPath += '/' + path[ii];
              }

              if(missing){
                if(!createMissingPanelAdded){
                  createMissingPanelAdded = true;
                  panel.add(createMissingPanel, "span");
                }
                createMissingButton.setEnabled(true);
              }
            }
          }
        }
      });
      eclipseHomeField.getDocument().addDocumentListener(
          new EclipseHomeListener(
            eclipseHomeField, eclipseLocalField, overridePluginsCheckBox));

      panel.add(overridePluginsCheckBox, "span");
      panel.add(new JLabel(Installer.getString(local)));
      panel.add(eclipseLocalChooser);

      // see if plugins are installed in user's home directory instead.
      File eclipseLocalPath = getDefaultEclipseLocalPath();
      if(eclipseLocalPath != null){
        overridePluginsCheckBox.doClick();
      }
    }

    return panel;
  }

  /**
   * {@inheritDoc}
   * @see org.formic.wizard.WizardStep#proceed()
   */
  public boolean proceed()
  {
    boolean proceed = super.proceed();
    String home = (String)Installer.getContext().getValue("eclipse.home");
    home = FilenameUtils.normalizeNoEndSeparator(home).replace('\\', '/');
    Installer.getContext().setValue("eclipse.home", home);

    if (Os.isFamily(Os.FAMILY_WINDOWS)){
      Installer.getContext().setValue("eclipse.local", home);
    }else{
      String local = (String)Installer.getContext().getValue("eclipse.local");
      local = FilenameUtils.normalizeNoEndSeparator(local);
      Installer.getContext().setValue("eclipse.local", local);

      if (!new org.formic.util.File(local).canWrite()){
        proceed = false;
        GuiDialogs.showWarning(
            "You do not have write permissions on the directory:\n" +
            "  " + local + "");
      }
    }
    return proceed;
  }

  /**
   * Gets the default value to use for the eclipse home if any.
   *
   * @return The default value or null if none could be determined.
   */
  private String getDefaultEclipseHome ()
  {
    String home = Installer.getEnvironmentVariable("ECLIPSE_HOME");
    if(home == null || home.trim().length() == 0){
      if(Os.isFamily(Os.FAMILY_WINDOWS)){
        for (int ii = 0; ii < WINDOWS_ECLIPSES.length; ii++){
          if(new File(WINDOWS_ECLIPSES[ii]).exists()){
            home = WINDOWS_ECLIPSES[ii];
            break;
          }
        }
      }else{
        for (int ii = 0; ii < UNIX_ECLIPSES.length; ii++){
          if(new File(UNIX_ECLIPSES[ii]).exists()){
            home = UNIX_ECLIPSES[ii];
            break;
          }
        }
      }
    }
    return home;
  }

  /**
   * Gets the default default user local eclipse path if one can be found.
   *
   * @return The default user local eclipse path (~/.eclipse/org.eclipse...).
   */
  private File getDefaultEclipseLocalPath()
  {
    File dotEclipse = new File(
        Installer.getProject().replaceProperties("${user.home}/.eclipse"));
    if(dotEclipse.exists()){
      File[] contents = dotEclipse.listFiles(new FilenameFilter(){
        public boolean accept(File dir, String name){
          if(name.startsWith("org.eclipse.platform_")){
            return true;
          }
          return false;
        }
      });
      if(contents.length > 0){
        Arrays.sort(contents);
        File dir = contents[contents.length - 1];
        return dir;
      }
    }
    return null;
  }

  private class EclipseHomeValidator
    implements Validator
  {
    public boolean isValid (Object value) {
      String folder = (String)value;
      if(folder != null && folder.trim().length() > 0){
        File plugins = new File(FilenameUtils.concat(folder, "plugins"));

        return plugins.exists() && plugins.isDirectory();
      }
      return true;
    }

    public String getErrorMessage () {
      return getName() + ".home.invalid";
    }
  }

  private class EclipseHomeWritableValidator
    implements Validator
  {
    public boolean isValid (Object value) {
      String folder = (String)value;
      if(folder != null && folder.trim().length() > 0){
        File plugins =
          new org.formic.util.File(FilenameUtils.concat(folder, "plugins"));

        return (overridePluginsCheckBox != null &&
            overridePluginsCheckBox.isSelected()) ||
          plugins.canWrite();
      }
      return true;
    }

    public String getErrorMessage () {
      return getName() + ".home.writable";
    }
  }

  private class EclipseHomeListener
    implements DocumentListener
  {
    private JTextField eclipseHome;
    private JTextField eclipseLocal;
    private JCheckBox hasLocal;

    public EclipseHomeListener (
        JTextField eclipseHome,
        JTextField eclipseLocal,
        JCheckBox hasLocal)
    {
      this.eclipseHome = eclipseHome;
      this.eclipseLocal = eclipseLocal;
      this.hasLocal = hasLocal;
    }

    public void insertUpdate (DocumentEvent e) {
      pathUpdated(e);
    }

    public void removeUpdate (DocumentEvent e) {
      pathUpdated(e);
    }

    public void changedUpdate (DocumentEvent e) {
      pathUpdated(e);
    }

    private void pathUpdated (DocumentEvent e)
    {
      if(!hasLocal.isSelected()){
        String path = eclipseHome.getText();
        eclipseLocal.setText(path);
      }
    }
  }

  private class CreatePluginDirsActions
    extends AbstractAction
  {
    private JTextField eclipseLocalField;

    public CreatePluginDirsActions (JTextField eclipseLocalField)
    {
      super("Create");
      this.eclipseLocalField = eclipseLocalField;
    }

    public void actionPerformed (ActionEvent e){
      try{
        boolean created = new File(eclipseLocalField.getText()).mkdirs();
        if (created){
          ((JButton)e.getSource()).setEnabled(false);
          eclipseLocalField.setText(eclipseLocalField.getText());
        }else{
          GuiDialogs.showError("Unable to create missing directories.");
        }
      }catch(Exception ex){
        GuiDialogs.showError("Error creating missing directories.", ex);
      }
    }
  }
}
