/**
 * Copyright (c) 2005 - 2007
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclim.installer.step;

import java.io.File;

import java.util.Properties;

import org.apache.tools.ant.taskdefs.condition.Os;

import org.formic.form.Validator;

import org.formic.form.gui.GuiForm;

import org.formic.form.validator.AggregateValidator;

import org.formic.util.CommandExecutor;

import org.formic.wizard.step.FileChooserStep;

/**
 * Step for choosing the location of the python interpreter.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class PythonInterpreterStep
  extends FileChooserStep
{
  private static final String[] WINDOWS_INTERPRETERS = {
    "C:/Program Files/Python25/python.exe",
    "C:/Program Files/Python24/python.exe"
  };

  private static final String[] UNIX_INTERPRETERS = {"python"};

  private static final String ICON = "/resources/images/python.png";

  /**
   * Constructs the step.
   */
  public PythonInterpreterStep (String name)
  {
    super(name);
  }

  /**
   * {@inheritDoc}
   * @see org.formic.wizard.WizardStep#initProperties(Properties)
   */
  public void initProperties (Properties properties)
  {
    properties.put(PROPERTY, "interpreter");
    properties.put("selectionMode", "files");
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

    String interpreter = null;
    if(Os.isFamily("windows")){
      for (int ii = 0; ii < WINDOWS_INTERPRETERS.length; ii++){
        if(new File(WINDOWS_INTERPRETERS[ii]).exists()){
          interpreter = WINDOWS_INTERPRETERS[ii];
          break;
        }
      }
    }else{
      for (int ii = 0; ii < UNIX_INTERPRETERS.length; ii++){
        String path = which(UNIX_INTERPRETERS[ii]);
        if(path != null){
          interpreter = path;
          break;
        }
      }
    }

    if(interpreter != null) {
      getGuiFileChooser().getTextField().setText(interpreter);
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
    validator.addValidator(new PythonValidator());

    return validator;
  }

  /**
   * Performs a 'which' on the supplied command and returns the resulting path
   * or null if not found on the path.
   *
   * @param cmd The command.
   * @return The path or null if not found.
   */
  private String which (String cmd)
  {
    try{
      CommandExecutor executor = CommandExecutor.execute(
          new String[]{"which", cmd}, 2000);
      if(executor.getReturnCode() == 0){
        return executor.getResult().trim();
      }
      executor.destroy();
    }catch(Exception e){
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Validates python interpreter.
   */
  private class PythonValidator
    implements Validator
  {
    /**
     * {@inheritDoc}
     * @see Validator#isValid(Object)
     */
    public boolean isValid (Object value)
    {
      File file = new File((String)value);
      return file.exists() && file.isFile();
    }

    /**
     * {@inheritDoc}
     * @see Validator#getErrorMessage()
     */
    public String getErrorMessage ()
    {
      return getName() + ".interpreter.invalid";
    }
  }
}
