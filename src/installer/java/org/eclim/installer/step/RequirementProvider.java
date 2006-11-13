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

import java.io.File;
import java.io.FileFilter;

import org.apache.commons.io.FilenameUtils;

import org.formic.Installer;

import org.formic.form.console.ConsoleForm;

import org.formic.form.gui.GuiForm;

import org.formic.wizard.step.RequirementsValidationStep.Requirement;

import org.formic.wizard.step.RequirementsValidationStep;

/**
 * Provides requirements to validate for RequirementsValidationStep.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class RequirementProvider
  implements RequirementsValidationStep.RequirementProvider
{
  private static final String WST = "wst";

  private GuiForm guiForm;
  private ConsoleForm consoleForm;

  /**
   * {@inheritDoc}
   * @see RequirementProvider#getRequirements()
   */
  public Requirement[] getRequirements ()
  {
    Requirement[] requirements = new Requirement[1];
    requirements[0] = new Requirement(WST);
    return requirements;
  }

  /**
   * {@inheritDoc}
   * @see RequirementProvider#validate(Requirement)
   */
  public int validate (Requirement requirement)
  {
    String eclipseHome = (String)
      Installer.getContext().getValue("eclipse.home");
    String plugins = FilenameUtils.concat(eclipseHome, "plugins");
    if(WST.equals(requirement.getKey())){
      File[] results =
        new File(plugins).listFiles(new PluginFileFilter("org.eclipse.wst."));
      return results != null && results.length > 0 ? OK : FAIL;
    }

    return OK;
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

  /**
   * FileFilter implementation for finding plugins by prefix.
   */
  private class PluginFileFilter
    implements FileFilter
  {
    private String prefix;

    /**
     * Constructs a new instance.
     *
     * @param prefix The prefix for this instance.
     */
    public PluginFileFilter (String prefix)
    {
      this.prefix = prefix;
    }

    /**
     * {@inheritDoc}
     * @see FileFilter#accept(File)
     */
    public boolean accept (File pathname)
    {
      return pathname.getName().startsWith(prefix);
    }
  }
}
