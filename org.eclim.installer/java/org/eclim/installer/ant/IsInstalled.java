/**
 * Copyright (C) 2012  Eric Van Dewoestine
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
import java.io.FilenameFilter;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectComponent;

import org.apache.tools.ant.taskdefs.condition.Condition;

import org.eclim.installer.step.EclipseInfo;

import org.formic.Installer;

/**
 * Condition to test if a given eclipse feature is installed.
 *
 * @author Eric Van Dewoestine
 */
public class IsInstalled
  extends ProjectComponent
  implements Condition
{
  private String feature;

  public boolean eval()
    throws BuildException
  {
      if (feature == null) {
        throw new BuildException("Attribute 'feature' must be set.");
      }
      EclipseInfo info = (EclipseInfo)
        Installer.getContext().getValue("eclipse.info");
      if (info != null && info.hasFeature(feature)){
        return true;
      }

      Project project = Installer.getProject();
      String[] names = new File(project.replaceProperties("${eclipse.local}/features"))
        .list(new FilenameFilter(){
          public boolean accept(File dir, String name){
            return name.startsWith(feature + "_");
          }
        });
      return names != null && names.length > 0;
  }

  public void setFeature(String feature)
  {
    this.feature = feature;
  }
}
