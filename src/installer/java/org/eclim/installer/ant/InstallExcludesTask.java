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
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
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
