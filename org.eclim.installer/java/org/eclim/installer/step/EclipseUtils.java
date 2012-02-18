/**
 * Copyright (C) 2005 - 2012  Eric Van Dewoestine
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

import java.io.File;
import java.io.FilenameFilter;

import org.apache.commons.io.FilenameUtils;

import org.formic.Installer;

public class EclipseUtils
{
  private static final String LAUNCHER_PREFIX = "org.eclipse.equinox.launcher_";

  public static String findEclipseLauncherJar()
  {
    String eclipseHome = Installer.getProject().getProperty("eclipse.home");
    if (eclipseHome == null){
      return null;
    }
    return findEclipseLauncherJar(eclipseHome);
  }

  public static String findEclipseLauncherJar(String eclipseHome)
  {
    // Everyone else
    String plugins = eclipseHome + "/plugins";
    String[] results = new File(plugins).list(new FilenameFilter(){
      public boolean accept(File dir, String name){
        return name.startsWith(LAUNCHER_PREFIX) && name.endsWith(".jar");
      }
    });

    if (results != null && results.length > 0){
      return FilenameUtils.concat(plugins, results[0]);
    }
    return null;
  }
}
