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
package eclim.plugin.sdt

import eclim.plugin.sdt.project.ScalaProjectManager

import org.eclim.Services

import org.eclim.plugin.AbstractPluginResources

import org.eclim.plugin.core.project.ProjectManagement
import org.eclim.plugin.core.project.ProjectNatureFactory

import org.scalaide.core.SdtConstants

/**
 * Implementation of AbstractPluginResources.
 *
 * @author Eric Van Dewoestine
 */
class PluginResources
  extends AbstractPluginResources
{
  import PluginResources._

  override
  def initialize(name: String)
  {
    super.initialize(name)

    //System.setProperty(SdtConstants.HeadlessProperty) // private property
    System.setProperty("sdtcore.headless", "true")

    ProjectNatureFactory.addNature("scala", NATURE)
    ProjectManagement.addProjectManager(NATURE, new ScalaProjectManager)
  }

  override
  def getBundleBaseName() = "eclim/plugin/sdt/messages"
}

object PluginResources {
  /**
   * Name that can be used to lookup this PluginResources from
   * {@link Services#getPluginResources(String)}.
   */
  final val NAME = "org.eclim.sdt"

  final val NATURE = SdtConstants.NatureId
}
