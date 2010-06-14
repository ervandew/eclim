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
import javax.swing.SwingUtilities;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import javax.swing.text.BadLocationException;

import org.formic.util.CommandExecutor;

import foxtrot.Task;
import foxtrot.Worker;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.io.FilenameUtils;

import org.apache.commons.lang.SystemUtils;

import org.apache.tools.ant.taskdefs.condition.Os;

import org.formic.Installer;

import org.formic.util.dialog.gui.GuiDialogs;

import org.formic.wizard.form.GuiForm;
import org.formic.wizard.form.Validator;

import org.formic.wizard.form.gui.component.FileChooser;

import org.formic.wizard.form.validator.ValidatorBuilder;

import org.formic.wizard.step.AbstractGuiStep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Step for specifying location of eclipse installation.
 *
 * @author Eric Van Dewoestine
 */
public class EclipseStep
  extends AbstractGuiStep
{
  private static final Logger logger = LoggerFactory.getLogger(EclipseStep.class);

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

  private boolean initializePanelAdded = false;
  private JCheckBox overridePluginsCheckBox;

  /**
   * Constructs the welcome step.
   */
  public EclipseStep(String name, Properties properties)
  {
    super(name, properties);
  }

  /**
   * {@inheritDoc}
   * @see org.formic.wizard.step.GuiStep#init()
   */
  public Component init()
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

    Validator validator = new ValidatorBuilder()
      .required()
      .isDirectory()
      .validator(new EclipseHomeValidator())
      .validator();

    form.bind(home, eclipseHomeChooser.getTextField(),
        new ValidatorBuilder()
        .validator(validator)
        .validator(new EclipseHomeWritableValidator())
        .validator());

    String eclipseHomeDefault = getDefaultEclipseHome();

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

      // button to initialize user local eclipse config.
      final JPanel initializePanel = new JPanel(new FlowLayout());
      final JButton initializeButton =
        new JButton(new InitializeAction(eclipseHomeField, eclipseLocalField));
      initializePanel.add(
          new JLabel(Installer.getString("eclipse.initialize")));
      initializePanel.add(initializeButton);

      eclipseLocalField.setEnabled(false);
      overridePluginsCheckBox.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
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
            initializeButton.setEnabled(false);
          }else{
            eclipseLocalField.grabFocus();
            final File eclipseLocalPath = getDefaultEclipseLocalPath();
            if(eclipseLocalPath != null){
              setEclipseLocalPath(eclipseLocalField, eclipseLocalPath);
            }else{
              if(!initializePanelAdded){
                initializePanelAdded = true;
                panel.add(initializePanel, "span");
              }
              initializeButton.setEnabled(true);
            }
          }
        }
      });

      eclipseHomeField.getDocument().addDocumentListener(
          new EclipseHomeListener(
            eclipseHomeField, eclipseLocalField, overridePluginsCheckBox, validator));

      panel.add(overridePluginsCheckBox, "span");
      panel.add(new JLabel(Installer.getString(local)));
      panel.add(eclipseLocalChooser);

      // see if plugins are installed in user's home directory instead.
      File eclipseLocalPath = getDefaultEclipseLocalPath();
      if(eclipseLocalPath != null){
        overridePluginsCheckBox.doClick();
      }
    }

    eclipseHomeChooser.getTextField().setText(eclipseHomeDefault);

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
  private String getDefaultEclipseHome()
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
    final File dotEclipse = new File(
        Installer.getProject().replaceProperties("${user.home}/.eclipse"));
    if(dotEclipse.exists()){
      File[] contents = dotEclipse.listFiles(new FilenameFilter(){
        public boolean accept(File dir, String name){
          File configuration = new File(dir + "/" + name + "/configuration");
          if (configuration.exists() && configuration.isDirectory()){
            return true;
          }
          return false;
        }
      });
      if(contents.length > 0){
        if(contents.length > 1){
          logger.warn(
              "Found more than one possible local eclipse dir: " +
                Arrays.toString(contents));
        }
        Arrays.sort(contents);
        File dir = contents[contents.length - 1];
        return dir;
      }
    }
    return null;
  }

  private void setEclipseLocalPath(JTextField eclipseLocalField, File path)
  {
    eclipseLocalField.setText(path.getAbsolutePath());
    eclipseLocalField.setCaretPosition(0);
  }

  private class EclipseHomeValidator
    implements Validator
  {
    public boolean isValid(Object value) {
      String folder = (String)value;
      if(folder != null && folder.trim().length() > 0){
        File plugins = new File(FilenameUtils.concat(folder, "plugins"));

        return plugins.exists() && plugins.isDirectory();
      }
      return true;
    }

    public String getErrorMessage() {
      return getName() + ".home.invalid";
    }
  }

  private class EclipseHomeWritableValidator
    implements Validator
  {
    public boolean isValid(Object value) {
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

    public String getErrorMessage() {
      return getName() + ".home.writable";
    }
  }

  private class EclipseHomeListener
    implements DocumentListener
  {
    private JTextField eclipseHome;
    private JTextField eclipseLocal;
    private JCheckBox hasLocal;
    private final Validator eclipseHomeValidator;

    public EclipseHomeListener(
        JTextField eclipseHome,
        JTextField eclipseLocal,
        JCheckBox hasLocal,
        Validator eclipseHomeValidator)
    {
      this.eclipseHome = eclipseHome;
      this.eclipseLocal = eclipseLocal;
      this.hasLocal = hasLocal;
      this.eclipseHomeValidator = eclipseHomeValidator;
    }

    public void insertUpdate(DocumentEvent e) {
      pathUpdated(e);
    }

    public void removeUpdate(DocumentEvent e) {
      pathUpdated(e);
    }

    public void changedUpdate(DocumentEvent e) {
      pathUpdated(e);
    }

    private void pathUpdated(DocumentEvent e)
    {
      final String path = eclipseHome.getText();

      if(!hasLocal.isSelected()){
        eclipseLocal.setText(path);
      }

      if (path.length() > 0){
        final File plugins =
          new org.formic.util.File(FilenameUtils.concat(path, "plugins"));

        SwingUtilities.invokeLater(new Runnable(){
          public void run(){
            if (eclipseHomeValidator.isValid(path) && !plugins.canWrite()){
              if (!hasLocal.isSelected()){
                hasLocal.doClick();
              }
              hasLocal.setEnabled(false);
            }else{
              hasLocal.setEnabled(true);
            }
          }
        });
      }
    }
  }

  private class InitializeAction
    extends AbstractAction
  {
    private JTextField eclipseHomeField;
    private JTextField eclipseLocalField;

    public InitializeAction(JTextField eclipseHomeField, JTextField eclipseLocalField)
    {
      super("Initialize");
      this.eclipseHomeField = eclipseHomeField;
      this.eclipseLocalField = eclipseLocalField;
    }

    public void actionPerformed(ActionEvent e){
      setBusy(true);
      try{
        Worker.post(new Task(){
          public Object run () throws Exception {
            String eclipseHome = eclipseHomeField.getText();
            String eclipse = EclipseUtils.findEclipse(eclipseHome);

            if (eclipse == null){
              throw new RuntimeException(
                "Could not find eclipse executable for path: " + eclipseHome);
            }

            CommandExecutor executor = CommandExecutor.execute(
              new String[]{eclipse, "-nosplash", "-initialize"}, 60000);
            if(executor.getReturnCode() != 0){
              throw new RuntimeException(
                  "error: " + executor.getErrorMessage() +
                  " out: " + executor.getResult());
            }

            return null;
          }
        });
        ((JButton)e.getSource()).setEnabled(false);
        final File path = getDefaultEclipseLocalPath();
        if (path != null){
          SwingUtilities.invokeLater(new Runnable(){
            public void run(){
              setEclipseLocalPath(eclipseLocalField, path);
              new File(eclipseLocalField.getText()).mkdirs();
              // force re-validation
              eclipseLocalField.grabFocus();
            }
          });
        }else{
          GuiDialogs.showError("Unable to locate initialized user local eclipse dir.");
        }
      }catch(Exception ex){
        GuiDialogs.showError("Error initializing user local eclipse configuration.", ex);
      }finally{
        setBusy(false);
      }
    }
  }
}
