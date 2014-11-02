/**
 * Copyright (C) 2011 - 2014 Eric Van Dewoestine
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
package eclim.plugin.sdt.util

import org.eclim.plugin.jdt.util.JavaUtils

import org.scalaide.core.internal.jdt.model.ScalaSourceFile

/**
 * Utility methods for working with scala files / projects.
 *
 * @author Eric Van Dewoestine
 */
object ScalaUtils
{
  /**
   * Gets a scala source file from the specified project.
   *
   * @param project The name of the project to locate the file in.
   * @param file The file to find.
   * @return A ScalaSourceFile instance
   */
  def getSourceFile(project: String, file: String): ScalaSourceFile = {
    val cu = JavaUtils.getCompilationUnit(project, file)
    cu match {
      case src: ScalaSourceFile => refreshSourceFile(src)
    }
  }

  /**
   * Refresh the given source file.
   */
  def refreshSourceFile(src: ScalaSourceFile): ScalaSourceFile = {
    if (src != null){
      src.initialReconcile
    }
    src
  }
}
