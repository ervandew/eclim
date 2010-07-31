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
package org.eclim.installer.step;

import java.io.File;

import org.apache.tools.ant.taskdefs.condition.Os;

import org.formic.Installer;

import org.formic.util.CommandExecutor;

public class EclipseUtils
{
  public static String findEclipse()
    throws Exception
  {
    String eclipseHome = Installer.getProject().getProperty("eclipse.home");
    if (eclipseHome == null){
      return null;
    }
    return findEclipse(eclipseHome);
  }

  public static String findEclipse(String eclipseHome)
    throws Exception
  {
    // Windows
    if (Os.isFamily(Os.FAMILY_WINDOWS)){
      String eclipse = eclipseHome + "/eclipse.exe";
      if (new File(eclipse).exists()){
        return eclipse;
      }
      return null;
    }

    // Everyone else
    String eclipse = eclipseHome + "/eclipse";
    if (new File(eclipse).exists()){
      return eclipse;
    }

    // OSX
    eclipse = eclipseHome + "/Eclipse.app/Contents/MacOS/eclipse";
    if (new File(eclipse).exists()){
      return eclipse;
    }

    CommandExecutor executor = CommandExecutor.execute(
      new String[]{"which", "eclipse"}, 1000);
    eclipse = executor.getResult();
    if (eclipse.trim().length() > 0){
      return eclipse;
    }

    executor = CommandExecutor.execute(
      new String[]{"which", "eclipse-3.6"}, 1000);
    eclipse = executor.getResult();
    if (eclipse.trim().length() > 0){
      return eclipse;
    }

    return null;
  }
}
