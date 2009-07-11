/**
 * Copyright (C) 2005 - 2009  Eric Van Dewoestine
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
package org.eclim.plugin.dltkruby.project;

import org.eclim.command.CommandLine;

import org.eclim.plugin.dltk.project.DltkProjectManager;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;

import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.DLTKLanguageManager;
import org.eclipse.dltk.core.IDLTKLanguageToolkit;
import org.eclipse.dltk.core.IScriptProject;

import org.eclipse.dltk.ruby.core.RubyNature;

/**
 * Implementation of {@link org.eclim.plugin.core.project.ProjectManager} for
 * php projects.
 *
 * @author Eric Van Dewoestine
 */
public class RubyProjectManager
  extends DltkProjectManager
{
  /**
   * {@inheritDoc}
   * @see org.eclim.plugin.core.project.ProjectManager#create(IProject,CommandLine)
   */
  public void create(IProject project, CommandLine commandLine)
    throws Exception
  {
    super.create(project, commandLine);

    // for some reason, when creating a dltk ruby project, the dltk script
    // builder is not added to the .project, so here we force it to be added.
    IProjectNature nature = project.getNature(RubyNature.NATURE_ID);
    nature.configure();

    IScriptProject scriptProject = DLTKCore.create(project);
    scriptProject.makeConsistent(null);
    scriptProject.save(null, false);
  }

  /**
   * {@inheritDoc}
   * @see DltkProjectManager#getLanguageToolkit()
   */
  @Override
  public IDLTKLanguageToolkit getLanguageToolkit()
  {
    return DLTKLanguageManager.getLanguageToolkit(RubyNature.NATURE_ID);
  }
}
