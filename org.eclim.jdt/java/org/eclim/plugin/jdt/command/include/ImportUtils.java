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
package org.eclim.plugin.jdt.command.include;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.eclim.plugin.core.preference.Preferences;

import org.eclipse.core.resources.IProject;

import org.eclipse.jdt.core.dom.ImportDeclaration;

import com.google.gson.Gson;

/**
 * Utilities for working with java imports.
 *
 * @author Eric Van Dewoestine
 */
public class ImportUtils
{
  public static boolean importsInSameGroup(
      int separationLevel, ImportDeclaration i1, ImportDeclaration i2)
  {
    // -1 = separate based on full package
    //  0 = never separate
    //  n = separate on comparing of n segments of the package.

    if (separationLevel == 0){
      return true;
    }

    List<String> pn1 = packageName(i1);
    List<String> pn2 = packageName(i2);

    for (int i = 0; (i < separationLevel || separationLevel == -1); i++){
      int level = i + 1;
      if (pn1.size() < level){
        return pn2.size() < level;
      }
      if (pn2.size() < level){
        return pn1.size() < level;
      }

      if (!pn1.get(i).equals(pn2.get(i))){
        return false;
      }
    }

    return true;
  }

  public static List<String> packageName(ImportDeclaration imprt)
  {
    String name = imprt.getName().getFullyQualifiedName();
    List<String> pack = new ArrayList<String>();
    for (String part : StringUtils.split(name, '.')){
      if (Character.isUpperCase(part.charAt(0))){
        break;
      }
      pack.add(part);
    }
    return pack;
  }

  public static boolean isImportExcluded(IProject project, String name)
    throws Exception
  {
    String setting = Preferences.getInstance().getValue(
        project, "org.eclim.java.import.exclude");
    if (setting != null && !setting.trim().equals(StringUtils.EMPTY)){
      String[] patterns = (String[])new Gson().fromJson(setting, String[].class);
      for (String pattern : patterns){
        if (name.matches(pattern)){
          return true;
        }
      }
    }
    return false;
  }
}
