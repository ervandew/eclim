/**
 * Copyright (C) 2005 - 2010  Eric Van Dewoestine
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
package org.eclim.installer.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;

import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import org.eclim.installer.step.command.Command;
import org.eclim.installer.step.command.UninstallCommand;

import org.formic.Installer;

/**
 * Ant task to remove installer resources.
 *
 * @author Eric Van Dewoestine
 */
public class CleanupTask
  extends Task
{
  /**
   * Executes this task.
   */
  public void execute()
    throws BuildException
  {
    Command command = null;
    try{
      Project project = Installer.getProject();

      String url = project.getProperty("eclim.feature.location");
      command = new UninstallCommand(
          null, url, "org.eclim.installer", "org.eclipse.equinox.p2.director");
      command.start();
      command.join();
      if(command.getReturnCode() != 0){
        Installer.getProject().log(
            "error: " + command.getErrorMessage() +
            " out: " + command.getOutput(), Project.MSG_WARN);
      }

      // remove installer plugin jar + artifacts.xml entry which the p2
      // director does not.
      new File(project.replaceProperties(
          "${eclipse.local}/plugins/org.eclim.installer_${eclim.version}.jar"))
        .delete();

      FileInputStream fin = new FileInputStream(project.replaceProperties(
          "${eclipse.local}/artifacts.xml"));
      FileWriter fout = null;
      try{
        String artifacts = IOUtils.toString(fin);
        fin.close();
        Pattern pattern = Pattern.compile(
          "\n\\s*<artifact classifier='osgi.bundle' id='org\\.eclim\\.installer.*?</artifact>",
          Pattern.DOTALL);
        artifacts = pattern.matcher(artifacts).replaceFirst("");
        fout = new FileWriter(project.replaceProperties(
              "${eclipse.local}/artifacts.xml"));
        fout.write(artifacts);
      }finally{
        IOUtils.closeQuietly(fout);
        IOUtils.closeQuietly(fin);
      }
    }catch(Exception e){
      throw new BuildException(e);
    }finally{
      if (command != null){
        command.destroy();
      }
    }
  }
}
