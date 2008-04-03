/**
 * Copyright (C) 2005 - 2008  Eric Van Dewoestine
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
    Requirement[] requirements = new Requirement[4];
    requirements[0] = new EclipseRequirement();
    requirements[1] = new VimRequirement();
    requirements[2] = new WhichRequirement("make");
    requirements[3] = new WhichRequirement("gcc");
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

  private class WhichRequirement
    extends ValidatingRequirement
  {
    private String program;

    public WhichRequirement (String program)
    {
      super(program);
      this.program = program;
    }

    public Status validate ()
    {
      try{
        int result = Runtime.getRuntime().exec(
            new String[] {"which", program}).waitFor();
        if (result != 0){
          return new Status(
              FAIL, Installer.getString(program + ".not.found"));
        }
      }catch(Exception e){
        logger.error("Error checking for '" + program + "'", e);
        return new Status(
            WARN, Installer.getString(program + ".validation.failed"));
      }
      return OK_STATUS;
    }
  }
}
