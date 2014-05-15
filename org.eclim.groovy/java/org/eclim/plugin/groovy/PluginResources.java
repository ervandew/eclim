/**
 * Copyright (C) 2014
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
package org.eclim.plugin.groovy;

import org.codehaus.jdt.groovy.model.GroovyNature;

import org.eclim.plugin.AbstractPluginResources;
import org.eclim.plugin.core.project.ProjectNatureFactory;

import org.eclipse.jdt.core.JavaCore;

/**
 * Implementation of AbstractPluginResources.
 */
public final class PluginResources
  extends AbstractPluginResources
{
  public static final String GROOVY_BUNDLE_BASENAME =
    "org/eclim/plugin/groovy/messages";

  @Override
  public void initialize(String name)
  {
    super.initialize(name);
    ProjectNatureFactory.addNature("groovy", GroovyNature.GROOVY_NATURE);
    ProjectNatureFactory.addNature("java", JavaCore.NATURE_ID);
  }

  @Override
  protected String getBundleBaseName()
  {
    return GROOVY_BUNDLE_BASENAME;
  }
}
