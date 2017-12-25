/**
 * Copyright (C) 2005 - 2017  Eric Van Dewoestine
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

//import java.lang.reflect.Field;

//import java.util.List;

import org.eclim.command.CommandLine;
//import org.eclim.command.Error;

import org.eclim.plugin.dltk.project.DltkProjectManager;

import org.eclim.plugin.dltk.util.DltkUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.dltk.core.DLTKCore;
//import org.eclipse.dltk.core.IBuildpathEntry;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.ISourceModule;

//import org.eclipse.dltk.launching.IInterpreterInstall;
//import org.eclipse.dltk.launching.ScriptRuntime;

import org.eclipse.dltk.ruby.core.RubyNature;

//import org.eclipse.dltk.ruby.internal.launching.RubyGenericInstall;

/**
 * Implementation of {@link org.eclim.plugin.core.project.ProjectManager} for
 * ruby projects.
 *
 * @author Eric Van Dewoestine
 */
public class RubyProjectManager
  extends DltkProjectManager
{
  @Override
  public void create(IProject project, CommandLine commandLine)
  {
    super.create(project, commandLine);

    // for some reason, when creating a dltk ruby project, the dltk script
    // builder is not added to the .project, so here we force it to be added.
    try{
      IProjectNature nature = project.getNature(RubyNature.NATURE_ID);
      nature.configure();

      IScriptProject scriptProject = DLTKCore.create(project);
      scriptProject.makeConsistent(null);
      scriptProject.save(null, false);
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }
  }

  // that last block doesn't seem to flush the previous interpreter's
  // completions... must be some other way to do that.
  /*@Override
  public List<Error> update(IProject project, CommandLine commandLine)
    throws Exception
  {
    IScriptProject scriptProject = DLTKCore.create(project);
    IBuildpathEntry[] buildpath = scriptProject.getRawBuildpath();
    IBuildpathEntry container = null;
    for (IBuildpathEntry entry : buildpath){
      if (entry.getEntryKind() == IBuildpathEntry.BPE_CONTAINER){
        container = entry;
        break;
      }
    }

    List<Error> errors = super.update(project, commandLine);
    if (errors != null){
      return errors;
    }

    buildpath = scriptProject.getRawBuildpath();
    IBuildpathEntry newContainer = null;
    for (IBuildpathEntry entry : buildpath){
      if (entry.getEntryKind() == IBuildpathEntry.BPE_CONTAINER){
        newContainer = entry;
        break;
      }
    }

    if (container != null && !container.equals(newContainer)){
      IInterpreterInstall install =
        ScriptRuntime.getInterpreterInstall(scriptProject);
      if (install != null){
        if (install instanceof RubyGenericInstall){
          RubyGenericInstall ruby = (RubyGenericInstall)install;
          Field helperField = ruby.getClass().getDeclaredField("helper");
          helperField.setAccessible(true);
          RubyGenericInstall.BuiltinsHelper helper =
            (RubyGenericInstall.BuiltinsHelper)helperField.get(ruby);
          Field sourcesField = helper.getClass().getDeclaredField("sources");
          sourcesField.setAccessible(true);
          sourcesField.set(helper, null);
        }
      }
    }

    return null;
  }*/

  @Override
  public void refresh(IProject project, IFile file)
  {
    try{
      ISourceModule module = DltkUtils.getSourceModule(file);
      if (module != null){
        try{
          module.makeConsistent(new NullProgressMonitor());
        }catch(CoreException ce){
          throw new RuntimeException(ce);
        }
      }
    }catch(IllegalArgumentException iae){
      // ignore
    }
  }

  @Override
  public String getNatureId()
  {
    return RubyNature.NATURE_ID;
  }
}
