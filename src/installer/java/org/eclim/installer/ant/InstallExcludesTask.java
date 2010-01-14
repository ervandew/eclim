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
import java.io.FileFilter;
import java.io.FileInputStream;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import org.apache.tools.ant.taskdefs.Concat;

import org.formic.Installer;
import org.formic.InstallContext;

/**
 * Task for creating an excludes file based on the features the user chose not
 * to install.
 *
 * @author Eric Van Dewoestine
 */
public class InstallExcludesTask
  extends Task
{
  private File destfile;

  /**
   * Executes this task.
   */
  public void execute ()
    throws BuildException
  {
    Project project = getProject();
    Installer.setProject(project);  // only needed for unattented install
    InstallContext context = Installer.getContext();

    final Concat concat = new Concat();
    concat.setProject(project);
    concat.setDestfile(destfile);
    concat.setAppend(true);

    final ArrayList includes = new ArrayList();
    String[] keys = context.getKeysByPrefix("featureList");
    for (int ii = 0; ii < keys.length; ii++){
      Boolean value = Boolean.valueOf(context.getValue(keys[ii]).toString());
      if(value.booleanValue()){
        includes.add(keys[ii].substring(keys[ii].indexOf('.') + 1));
      }
    }
    log("including features: " + java.util.Arrays.toString(includes.toArray()));

    String excludesPath = project.replaceProperties("${basedir}/resources/excludes/");
    File[] excludes = new File(excludesPath).listFiles(new FileFilter(){
      public boolean accept(File file){
        if (!file.getName().endsWith(".excludes")){
          return false;
        }

        String feature = file.getName().substring(0, file.getName().indexOf('.'));
        if(!includes.contains(feature)){
          try{
            String text = IOUtils.toString(new FileInputStream(file));
            text = getProject().replaceProperties(text);
            concat.addText(text);
          }catch(Exception e){
            throw new BuildException(e);
          }
          return true;
        }
        return false;
      }
    });

    if(excludes.length > 0){
      concat.execute();
    }
  }

  /**
   * Sets the destination file for the excludes list.
   *
   * @param destfile The destination file.
   */
  public void setDestfile (File destfile)
  {
    this.destfile = destfile;
  }
}
