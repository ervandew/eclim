/**
 * Copyright (C) 2005 - 2009  Eric Van Dewoestine
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

import org.apache.commons.io.IOUtils;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import org.apache.tools.ant.taskdefs.Concat;

import org.formic.Installer;

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
    Concat concat = new Concat();
    concat.setProject(getProject());
    concat.setDestfile(destfile);
    concat.setAppend(true);

    boolean run = false;
    String[] features = Installer.getContext().getKeysByPrefix("featureList");
    for (int ii = 0; ii < features.length; ii++){
      Boolean value = (Boolean)Installer.getContext().getValue(features[ii]);
      if(!value.booleanValue()){
        String feature = features[ii].substring(features[ii].indexOf('.') + 1);
        String path = getProject().replaceProperties(
            "${basedir}/resources/excludes/" + feature + ".excludes");
        File excludes = new File(path);
        if(excludes.exists()){
          try{
            String text = IOUtils.toString(new FileInputStream(excludes));
            text = getProject().replaceProperties(text);
            concat.addText(text);
            run = true;
          }catch(Exception e){
            throw new BuildException(e);
          }
        }
      }
    }

    if(run){
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
