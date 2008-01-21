package org.eclim.installer.step;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.FilenameFilter;

import java.util.Arrays;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JTextField;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.layout.FormLayout;

import org.apache.commons.io.FilenameUtils;

import org.apache.commons.lang.SystemUtils;

import org.apache.tools.ant.taskdefs.condition.Os;

import org.formic.Installer;

import org.formic.form.Validator;

import org.formic.form.console.ConsoleForm;

import org.formic.form.gui.GuiComponentFactory;
import org.formic.form.gui.GuiFileChooser;
import org.formic.form.gui.GuiForm;
import org.formic.form.gui.GuiFormBuilder;

import org.formic.form.validator.AggregateValidator;
import org.formic.form.validator.ValidatorBuilder;

import org.formic.wizard.step.AbstractFormStep;

/**
 * Step for specifying location of eclipse installation.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class EclipseStep
  extends AbstractFormStep
{
  private static final String ICON = "/resources/images/eclipse.png";

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
  public EclipseStep (String name)
  {
    super(name);
  }

  /**
   * {@inheritDoc}
   * @see AbstractStep#getIconPath()
   */
  protected String getIconPath ()
  {
    return ICON;
  }

  /**
   * {@inheritDoc}
   * @see AbstractFormStep#initGuiForm()
   */
  public GuiForm initGuiForm ()
  {
    FormLayout layout = new FormLayout("pref, 4dlu, 150dlu");
    GuiFormBuilder builder = new GuiFormBuilder(getName(), layout);
    GuiComponentFactory factory = builder.getFactory();

    String eclipseHomeDefault = getDefaultEclipseHome();
    final GuiFileChooser eclipseHomeChooser = factory.createFileChooser(
        "home", eclipseHomeDefault, getEclipseHomeValidator());
    eclipseHomeChooser.getFileChooser().setFileSelectionMode(
        JFileChooser.DIRECTORIES_ONLY);

    builder.append(eclipseHomeChooser);

    // On some systems (notably fedora) the user may need to install the plugins
    // in an alternate location.
    if(!Os.isFamily("windows")){
      String eclipsePluginsDefault = eclipseHomeDefault != null ?
        eclipseHomeDefault + "/plugins" : null;
      Validator validator = new ValidatorBuilder()
        .required().isDirectory().validator();
      final GuiFileChooser eclipsePluginsChooser = factory.createFileChooser(
          "plugins", eclipsePluginsDefault, validator);
      eclipsePluginsChooser.getFileChooser().setFileSelectionMode(
          JFileChooser.DIRECTORIES_ONLY);

      JTextField eclipseHomeField = eclipseHomeChooser.getTextField();
      JTextField eclipsePluginsField = eclipsePluginsChooser.getTextField();
      JCheckBox overridePlugins = factory.createCheckBox("overridePlugins");
      eclipsePluginsField.setEnabled(false);
      eclipsePluginsChooser.getButton().setEnabled(false);
      overridePlugins.addActionListener(new ActionListener(){
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
            eclipseHomeField, eclipsePluginsField, overridePlugins));

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
          overridePlugins.doClick();
          eclipsePluginsField.setText(
              dir.getAbsolutePath() + "/configuration/eclipse/plugins");
          eclipsePluginsField.setCaretPosition(0);
        }
      }

      builder.nextRow();
      builder.append(overridePlugins, 3, 1);
      builder.nextRow();
      builder.append(eclipsePluginsChooser);
    }

    return builder.getForm();
  }

  /**
   * {@inheritDoc}
   * @see AbstractFormStep#initConsoleForm()
   */
  public ConsoleForm initConsoleForm ()
  {
    throw new UnsupportedOperationException("initConsoleForm()");
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

  /**
   * Get validator to use for eclipse home input.
   *
   * @return The Validator to use.
   */
  protected Validator getEclipseHomeValidator ()
  {
    AggregateValidator validator = new AggregateValidator();
    validator.addValidator(new ValidatorBuilder().required().validator());
    validator.addValidator(new EclipseHomeValidator());

    return validator;
  }

  /**
   * Validator for validating eclipse home directory.
   */
  private class EclipseHomeValidator
    implements Validator
  {
    /**
     * {@inheritDoc}
     * @see Validator#isValid(Object)
     */
    public boolean isValid (Object value)
    {
      String folder = (String)value;
      if(folder != null && folder.trim().length() > 0){
        File plugins = new File(FilenameUtils.concat(folder, "plugins"));

        return plugins.exists() && plugins.isDirectory();
      }
      return true;
    }

    /**
     * {@inheritDoc}
     * @see Validator#getErrorMessage()
     */
    public String getErrorMessage ()
    {
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

    /**
     * {@inheritDoc}
     * @see DocumentListener#insertUpdate(DocumentEvent)
     */
    public void insertUpdate (DocumentEvent e)
    {
      pathUpdated(e);
    }

    /**
     * {@inheritDoc}
     * @see DocumentListener#removeUpdate(DocumentEvent)
     */
    public void removeUpdate (DocumentEvent e)
    {
      pathUpdated(e);
    }

    /**
     * {@inheritDoc}
     * @see DocumentListener#changedUpdate(DocumentEvent)
     */
    public void changedUpdate (DocumentEvent e)
    {
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
