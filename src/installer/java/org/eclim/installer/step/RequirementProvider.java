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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
  private static final Map REQUIREMENTS = new HashMap();
  static {
    REQUIREMENTS.put("ant", new String[]{"org.eclipse.ant.ui"});
    REQUIREMENTS.put("jdt", new String[]{"org.eclipse.jdt.ui"});
    REQUIREMENTS.put("wst", new String[]{
      "org.eclipse.wst.css.ui",
      "org.eclipse.wst.html.ui",
      "org.eclipse.wst.sse.ui",
      "org.eclipse.wst.xml.ui",
    });
    REQUIREMENTS.put("pydev", new String[]{"org.python.pydev"});
  }

  private GuiForm guiForm;
  private ConsoleForm consoleForm;

  /**
   * {@inheritDoc}
   * @see RequirementProvider#getRequirements()
   */
  public Requirement[] getRequirements ()
  {
    ArrayList requirements = new ArrayList();
    String[] features = Installer.getContext().getKeysByPrefix("featureList");
    for (int ii = 0; ii < features.length; ii++){
      Boolean value = (Boolean)Installer.getContext().getValue(features[ii]);
      String name = features[ii].substring(features[ii].indexOf('.') + 1);
      if(value.booleanValue() && REQUIREMENTS.containsKey(name)){
        requirements.add(new Requirement(name));
      }
    }

    return (Requirement[])
      requirements.toArray(new Requirement[requirements.size()]);
  }

  /**
   * {@inheritDoc}
   * @see RequirementProvider#validate(Requirement)
   */
  public Status validate (Requirement requirement)
  {
    String eclipseHome = (String)
      Installer.getContext().getValue("eclipse.home");
    String plugins = FilenameUtils.concat(eclipseHome, "plugins");

    String[] list = (String[])REQUIREMENTS.get(requirement.getKey());
    for(int ii = 0; ii < list.length; ii++){
      File[] results =
        new File(plugins).listFiles(new PluginFileFilter(list[ii]));
      if(results.length == 0){
        return new Status(
            FAIL, Installer.getString("plugin.not.found", list[ii]));
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
