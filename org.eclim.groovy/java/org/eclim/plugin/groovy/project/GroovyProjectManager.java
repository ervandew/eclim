/**
 * Copyright (C) 2014 - 2017  Eric Van Dewoestine
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
package org.eclim.plugin.groovy.project;

import java.util.ArrayList;

import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;

import org.eclim.command.CommandLine;

import org.eclim.plugin.groovy.PluginResources;

import org.eclim.plugin.jdt.project.JavaProjectManager;

import org.eclim.util.CollectionUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Implementation of {@link org.eclim.plugin.core.project.ProjectManager} for
 * groovy projects.
 *
 * @author Eric Van Dewoestine
 */
public class GroovyProjectManager
  extends JavaProjectManager
{
  @Override
  public void create(IProject project, CommandLine commandLine)
  {
    super.create(project, commandLine);

    // hack to co-operate with the java project manager.
    // remove the groovy nature so that we can use the GroovyRuntime method call
    // below to add all the necessary classpath container entries that the java
    // project manager either removes or prevents the creation of.
    try{
      IProjectDescription desc = project.getDescription();
      String[] natureIds = desc.getNatureIds();
      ArrayList<String> modified = new ArrayList<String>();
      CollectionUtils.addAll(modified, natureIds);
      modified.remove(PluginResources.NATURE);
      desc.setNatureIds((String[])modified.toArray(new String[modified.size()]));
      project.setDescription(desc, new NullProgressMonitor());
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }

    GroovyRuntime.addGroovyRuntime(project);
  }
}
