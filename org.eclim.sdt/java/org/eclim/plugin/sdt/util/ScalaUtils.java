/**
 * Copyright (C) 2011  Eric Van Dewoestine
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
package org.eclim.plugin.sdt.util;

import org.apache.commons.lang.StringUtils;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclipse.jdt.core.BufferChangedEvent;

import scala.tools.eclipse.javaelements.ScalaSourceFile;

/**
 * Utility methods for working with scala files / projects.
 *
 * @author Eric Van Dewoestine
 */
public class ScalaUtils
{
  /**
   * Gets a scala source file from the specified project.
   *
   * @param project The name of the project to locate the file in.
   * @param file The file to find.
   * @return A ScalaSourceFile instance
   */
  public static ScalaSourceFile getSourceFile(String project, String file)
    throws Exception
  {
    ScalaSourceFile src = (ScalaSourceFile)
      JavaUtils.getCompilationUnit(project, file);
    refreshSourceFile(src);
    return src;
  }

  public static void refreshSourceFile(ScalaSourceFile src)
    throws Exception
  {
    if (src == null){
      return;
    }

    // without writing scala code, this is the easiest way to force the source
    // file to be run through the scala compiler.
    // TODO: implement this in scala to permit a more direct parsing of the file.
    src.bufferChanged(new BufferChangedEvent(
          src.getBuffer(), 1, 0, StringUtils.EMPTY));
  }
}
