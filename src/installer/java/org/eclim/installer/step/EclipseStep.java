package org.eclim.installer.step;

import java.awt.Component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.FilenameFilter;

import java.util.Arrays;
import java.util.Properties;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.io.FilenameUtils;

import org.apache.commons.lang.SystemUtils;

import org.apache.tools.ant.taskdefs.condition.Os;

import org.formic.Installer;

import org.formic.wizard.form.GuiForm;
import org.formic.wizard.form.Validator;

import org.formic.wizard.form.gui.component.FileChooser;

import org.formic.wizard.form.validator.ValidatorBuilder;

import org.formic.wizard.step.AbstractGuiStep;

/**
 * Step for specifying location of eclipse installation.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
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
    "/usr/local/eclipse",
    "/usr/share/eclipse",
    SystemUtils.USER_HOME + "/eclipse"
  };

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
    JPanel panel = new JPanel(new MigLayout("wrap 2, fillx", "[growprio 0] [fill]"));
    GuiForm form = createForm();

    String home = fieldName("home");
    String eclipseHomeDefault = getDefaultEclipseHome();
    final FileChooser eclipseHomeChooser =
       new FileChooser(JFileChooser.DIRECTORIES_ONLY);

    panel.add(new JLabel(Installer.getString(home)));
    panel.add(eclipseHomeChooser);

    form.bind(home, eclipseHomeChooser.getTextField(),
        new ValidatorBuilder().required()
        .validator(new EclipseHomeValidator()).validator());
    eclipseHomeChooser.getTextField().setText(eclipseHomeDefault);

    // On some systems (notably fedora) the user may need to install the plugins
    // in an alternate location.
    if(!Os.isFamily("windows")){
      String plugins = fieldName("plugins");
      String eclipsePluginsDefault = eclipseHomeDefault != null ?
        eclipseHomeDefault + "/plugins" : null;
      final FileChooser eclipsePluginsChooser =
         new FileChooser(JFileChooser.DIRECTORIES_ONLY);
      form.bind(plugins, eclipsePluginsChooser.getTextField(),
        new ValidatorBuilder().required().isDirectory().validator());

      JTextField eclipseHomeField = eclipseHomeChooser.getTextField();
      JTextField eclipsePluginsField = eclipsePluginsChooser.getTextField();
      eclipsePluginsField.setText(eclipsePluginsDefault);
      eclipsePluginsChooser.getButton().setEnabled(false);

      String overridePlugins = fieldName("overridePlugins");
      JCheckBox overridePluginsCheckBox =
        new JCheckBox(Installer.getString(overridePlugins));

      eclipsePluginsField.setEnabled(false);
      overridePluginsCheckBox.addActionListener(new ActionListener(){
        public void actionPerformed (ActionEvent e){
          boolean selected = ((JCheckBox)e.getSource()).isSelected();
          eclipsePluginsChooser.getTextField().setEnabled(selected);
          eclipsePluginsChooser.getButton().setEnabled(selected);
          if(!selected){
            // force eclipse plugins text field to update itself
            JTextField eclipseHomeField = eclipseHomeChooser.getTextField();
            eclipseHomeField.setText(eclipseHomeField.getText());
          }
        }
      });
      eclipseHomeField.getDocument().addDocumentListener(
          new EclipseHomeListener(
            eclipseHomeField, eclipsePluginsField, overridePluginsCheckBox));

      // for fedora (and possibly others) see if plugins are installed in user's
      // home directory instead.
      File dotEclipse = new File(
          Installer.getProject().replaceProperties("${user.home}/.eclipse"));
      if(dotEclipse.exists()){
        File[] contents = dotEclipse.listFiles(new FilenameFilter(){
          public boolean accept (File dir, String name){
            if(name.startsWith("org.eclipse.platform_")){
              return true;
            }
            return false;
          }
        });
        if(contents.length > 0){
          Arrays.sort(contents);
          File dir = contents[contents.length - 1];
          overridePluginsCheckBox.doClick();
          eclipsePluginsField.setText(
              dir.getAbsolutePath() + "/configuration/eclipse/plugins");
          eclipsePluginsField.setCaretPosition(0);
        }
      }

      panel.add(overridePluginsCheckBox, "span");
      panel.add(new JLabel(Installer.getString(plugins)));
      panel.add(eclipsePluginsChooser);
    }

    return panel;
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
      if(Os.isFamily("windows")){
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

  private class EclipseHomeListener
    implements DocumentListener
  {
    private JTextField eclipseHome;
    private JTextField eclipsePlugins;
    private JCheckBox overridePlugins;

    public EclipseHomeListener (
        JTextField eclipseHome,
        JTextField eclipsePlugins,
        JCheckBox overridePlugins)
    {
      this.eclipseHome = eclipseHome;
      this.eclipsePlugins = eclipsePlugins;
      this.overridePlugins = overridePlugins;
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
      if(!overridePlugins.isSelected()){
        String path = eclipseHome.getText();
        if (path.length() > 0){
          if(!path.endsWith("/")){
            path += '/';
          }
          path += "plugins";
        }
        eclipsePlugins.setText(path);
      }
    }
  }
}
