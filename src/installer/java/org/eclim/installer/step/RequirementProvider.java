/**
 * Copyright (c) 2005 - 2008
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import org.apache.commons.lang.StringUtils;

import org.apache.tools.ant.taskdefs.condition.Os;

import org.formic.Installer;

import org.formic.util.CommandExecutor;

import org.formic.wizard.step.gui.RequirementsValidationStep;
import org.formic.wizard.step.gui.RequirementsValidationStep.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides requirements to validate for RequirementsValidationStep.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class RequirementProvider
  implements RequirementsValidationStep.RequirementProvider
{
  private static final Logger logger =
    LoggerFactory.getLogger(RequirementProvider.class);

  /**
   * {@inheritDoc}
   * @see RequirementProvider#getRequirements()
   */
  public Requirement[] getRequirements ()
  {
    if(Os.isFamily("windows")){
      Requirement[] requirements = new Requirement[1];
      requirements[0] = new EclipseRequirement();
      return requirements;
    }
    Requirement[] requirements = new Requirement[3];
    requirements[0] = new EclipseRequirement();
    requirements[1] = new VimRequirement();
    requirements[2] = new MakeRequirement();
    return requirements;
  }

  /**
   * {@inheritDoc}
   * @see RequirementProvider#validate(Requirement)
   */
  public Status validate (Requirement requirement)
  {
    ValidatingRequirement req = (ValidatingRequirement)requirement;
    return req.validate();
  }

  private class EclipseRequirement
    extends ValidatingRequirement
  {
    private final Pattern VERSION =
      Pattern.compile(".*Release\\s+(([0-9]+\\.)+[0-9]).*");

    public EclipseRequirement ()
    {
      super("eclipse");
    }

    public Status validate ()
    {
      String eclipseHome = (String)
        Installer.getContext().getValue("eclipse.home");
      eclipseHome = eclipseHome.replace('\\', '/');
      // probably a better way
      File file = new File(eclipseHome + "/readme/readme_eclipse.html");
      if (!file.exists()){
        logger.error(
            "Error checking eclipse version. File does not exist: ", file);
        return new Status(
            WARN, Installer.getString("eclipse.validation.failed"));
      }
      BufferedReader reader = null;
      try{
        reader = new BufferedReader(new FileReader(file));
        String line = null;
        for(int ii = 0; ii < 30; ii++){
          line = reader.readLine();
          Matcher matcher = VERSION.matcher(line);
          if (matcher.matches()){
            String version = matcher.group(1);
            String[] parts = StringUtils.split(version, '.');
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;

            if(major < 3 || minor < 3 || patch < 0){
              return new Status(FAIL,
                  Installer.getString("eclipse.version.invalid", version, "3.3.x"));
            }
            break;
          }
        }
      }catch(Exception e){
        logger.error("Error checking eclipse version.", e);
        return new Status(
            WARN, Installer.getString("eclipse.validation.failed"));
      }finally{
        IOUtils.closeQuietly(reader);
      }
      return OK_STATUS;
    }
  }

  private class VimRequirement
    extends ValidatingRequirement
  {
    private final Pattern VERSION =
      Pattern.compile(".*VIM - Vi IMproved (([0-9]+\\.)+[0-9]).*");

    public VimRequirement ()
    {
      super("vim");
    }

    public Status validate ()
    {
      try{
        CommandExecutor command =
          CommandExecutor.execute(new String[]{"vim", "--version"}, 2000);
        if (command.getReturnCode() != 0){
          logger.error(
              "Error checking vim version: {}", command.getErrorMessage());
          return new Status(
              WARN, Installer.getString("vim.validation.failed"));
        }
        String result = command.getResult();
        Matcher matcher = VERSION.matcher(result);
        if(!matcher.find()){
          logger.error("Error finding vim version in output: {}", result);
          return new Status(
              WARN, Installer.getString("vim.validation.failed"));
        }
        String version = matcher.group(1);
        String[] parts = StringUtils.split(version, '.');
        int major = Integer.parseInt(parts[0]);
        int minor = Integer.parseInt(parts[1]);
        int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;

        if(major < 7 || minor < 0 || patch < 0){
          return new Status(FAIL,
              Installer.getString("vim.version.invalid", version, "7.0.x"));
        }
      }catch(Exception e){
        logger.error("Error checking vim version.", e);
        return new Status(WARN, Installer.getString("vim.validation.failed"));
      }
      return OK_STATUS;
    }
  }

  private class MakeRequirement
    extends ValidatingRequirement
  {
    public MakeRequirement ()
    {
      super("make");
    }

    public Status validate ()
    {
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
      return OK_STATUS;
    }
  }
}
