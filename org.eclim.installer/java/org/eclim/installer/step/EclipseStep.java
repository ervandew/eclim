/**
 * Copyright (C) 2005 - 2017  Eric Van Dewoestine
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

import java.io.File;

import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.io.FilenameUtils;

import org.apache.commons.lang.SystemUtils;

import org.apache.tools.ant.taskdefs.condition.Os;

import org.formic.Installer;

import org.formic.wizard.form.GuiForm;
import org.formic.wizard.form.Validator;

import org.formic.wizard.form.gui.component.FileChooser;

import org.formic.wizard.form.validator.ValidatorBuilder;

import org.formic.wizard.step.AbstractGuiStep;

import net.miginfocom.swing.MigLayout;

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
    "C:/Program Files (x86)/eclipse",
  };

  private static final String[] UNIX_ECLIPSES = {
    "/opt/eclipse",
    "/usr/lib/eclipse",
    "/usr/local/eclipse",
    "/usr/share/eclipse",
    SystemUtils.USER_HOME + "/eclipse",
    "/Applications/eclipse",
  };

  private FileChooser eclipseHomeChooser;

  /**
   * Constructs the welcome step.
   */
  public EclipseStep(String name, Properties properties)
  {
    super(name, properties);
  }

  @Override
  public Component init()
  {
    final JPanel panel =
      new JPanel(new MigLayout("wrap 2, fillx", "[growprio 0] [fill]"));
    GuiForm form = createForm();

    String home = fieldName("home");

    eclipseHomeChooser =
      new FileChooser(JFileChooser.DIRECTORIES_ONLY);

    panel.add(form.createMessagePanel(), "span");
    panel.add(new JLabel(Installer.getString(home)));
    panel.add(eclipseHomeChooser);

    form.bind(home, eclipseHomeChooser.getTextField(),
        new ValidatorBuilder()
        .required()
        .isDirectory()
        .validator(new EclipseHomeValidator())
        .validator());

    String eclipseHomeDefault = getDefaultEclipseHome();
    eclipseHomeChooser.getTextField().setText(eclipseHomeDefault);

    return panel;
  }

  @Override
  public void displayed()
  {
    eclipseHomeChooser.getTextField().requestFocus();
  }

  @Override
  public boolean proceed()
  {
    boolean proceed = super.proceed();
    String home = (String)Installer.getContext().getValue("eclipse.home");
    home = FilenameUtils.normalizeNoEndSeparator(home).replace('\\', '/');
    Installer.getContext().setValue("eclipse.home", home);
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

  private class EclipseHomeValidator
    implements Validator
  {
    public boolean isValid(Object value)
    {
      String folder = (String)value;
      if(folder != null && folder.trim().length() > 0){
        File plugins = new File(FilenameUtils.concat(folder, "plugins"));
        if (plugins.exists() && plugins.isDirectory()){
          return EclipseUtils.findEclipseLauncherJar(folder) != null;
        }
      }
      return false;
    }

    public String getErrorMessage()
    {
      return getName() + ".home.invalid";
    }
  }
}
