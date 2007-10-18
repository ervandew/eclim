/**
 * Copyright (c) 2005 - 2006
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

import org.formic.Installer;

import org.formic.form.console.ConsoleForm;

import org.formic.form.gui.GuiForm;

import org.formic.wizard.step.RequirementsValidationStep.Requirement;

import org.formic.wizard.step.RequirementsValidationStep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides requirements to validate for RequirementsValidationStep.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class RequirementProvider
  implements RequirementsValidationStep.RequirementProvider
{
  private static final Logger logger =
    LoggerFactory.getLogger(RequirementProvider.class);

  private GuiForm guiForm;
  private ConsoleForm consoleForm;

  /**
   * {@inheritDoc}
   * @see RequirementProvider#getRequirements()
   */
  public Requirement[] getRequirements ()
  {
    Requirement[] requirements = new Requirement[1];
    requirements[0] = new Requirement("make");
    return requirements;
  }

  /**
   * {@inheritDoc}
   * @see RequirementProvider#validate(Requirement)
   */
  public Status validate (Requirement requirement)
  {
    String eclipseHome = (String)
      Installer.getContext().getValue("eclipse.home");

    if ("make".equals(requirement.getKey())){
      try{
        int result = Runtime.getRuntime().exec(
            new String[] {"which", "make"}).waitFor();
        if (result != 0){
          return new Status(
              FAIL, Installer.getString("make.not.found"));
        }
      }catch(Exception e){
        logger.error("Error checking for 'make'", e);
        return new Status(
            WARN, Installer.getString("make.validation.failed"));
      }
    }


    return OK_STATUS;
  }

  /**
   * {@inheritDoc}
   * @see RequirementProvider#setGuiForm(GuiForm)
   */
  public void setGuiForm (GuiForm form)
  {
    this.guiForm = form;
  }

  /**
   * {@inheritDoc}
   * @see RequirementProvider#setConsoleForm(ConsoleForm)
   */
  public void setConsoleForm (ConsoleForm form)
  {
    this.consoleForm = form;
  }
}
