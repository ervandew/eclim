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
package eclim.plugin.sdt.project

import eclim.plugin.sdt.PluginResources

import eclim.plugin.sdt.util.ScalaUtils

import org.eclim.command.CommandLine

import org.eclim.plugin.core.project.ProjectManager

import org.eclim.plugin.jdt.project.JavaProjectManager

import org.eclipse.core.resources.IProject

import org.eclipse.core.runtime.NullProgressMonitor

import org.eclipse.jdt.core.JavaCore

import org.scalaide.core.IScalaPlugin

import org.scalaide.core.internal.jdt.model.ScalaSourceFile

import org.scalaide.core.internal.project.Nature

/**
 * {@link ProjectManager} for scala projects.
 *
 * @author Eric Van Dewoestine
 */
class ScalaProjectManager
  extends JavaProjectManager
{
  override
  protected def create(project: IProject, depends: String) {
    super.create(project, depends)

    val desc = project.getDescription
    val natures = desc.getNatureIds
    val newNatures = new Array[String](natures.length + 1)
    System.arraycopy(natures, 0, newNatures, 0, natures.length)
    newNatures(natures.length) = PluginResources.NATURE
    desc.setNatureIds(newNatures)

    project.setDescription(desc, new NullProgressMonitor)

    val nature = new Nature
    nature.setProject(project)
    nature.configure
  }

  override
  def refresh(project: IProject, commandLine: CommandLine) {
    super.refresh(project, commandLine)

    // is there an easier way to force re-parsing of all files?
    val sproject = IScalaPlugin().getScalaProject(project)
    for (src <- sproject.allSourceFiles){
      val cu = JavaCore.createCompilationUnitFrom(src)
      if (cu.isInstanceOf[ScalaSourceFile]){
        ScalaUtils.refreshSourceFile(cu.asInstanceOf[ScalaSourceFile])
      }
    }
  }
}
