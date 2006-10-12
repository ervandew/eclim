package org.eclim.installer.step;

import java.io.File;

import java.util.Properties;

import org.apache.commons.io.FilenameUtils;

import org.apache.tools.ant.taskdefs.condition.Os;

import org.formic.Installer;

import org.formic.form.Validator;

import org.formic.form.gui.GuiForm;

import org.formic.form.validator.AggregateValidator;

import org.formic.wizard.step.FileChooserStep;

/**
 * Step for specifying location of eclipse installation.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class EclipseStep
  extends FileChooserStep
{
  private static final String ICON = "/resources/images/eclipse.png";

  /**
   * Constructs the welcome step.
   */
  public EclipseStep (String name)
  {
    super(name);
  }

  /**
   * {@inheritDoc}
   * @see org.formic.wizard.WizardStep#initProperties(Properties)
   */
  public void initProperties (Properties properties)
  {
    properties.put(PROPERTY, "eclipseHome");
    properties.put("selectionMode", "directories");
    super.initProperties(properties);
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
   * @see FileChooserStep#initGuiForm()
   */
  public GuiForm initGuiForm ()
  {
    GuiForm form = super.initGuiForm();

    String home = Installer.getEnvironmentVariable("ECLIPSE_HOME");
    if(home != null){
      getGuiFileChooser().getTextField().setText(home);
    }

    return form;
  }

  /**
   * {@inheritDoc}
   * @see FileChooserStep#getValidator()
   */
  protected Validator getValidator ()
  {
    AggregateValidator validator = new AggregateValidator();
    validator.addValidator(super.getValidator());
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
        String exe = null;
        if(Os.isFamily("windows")){
          exe = FilenameUtils.concat(folder, "eclipse.exe");
        }else{
          exe = FilenameUtils.concat(folder, "eclipse");
        }

        File file = new File(exe);
        return file.exists() && file.isFile();
      }
      return true;
    }

    /**
     * {@inheritDoc}
     * @see Validator#getErrorMessage()
     */
    public String getErrorMessage ()
    {
      return getName() + ".eclipseHome.invalid";
    }
  }
}
