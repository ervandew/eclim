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
package org.eclim.plugin.pdt.project;

import org.eclim.plugin.dltk.project.DltkProjectManager;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.dltk.core.ISourceModule;

import org.eclipse.php.internal.core.project.PHPNature;

import org.eclipse.php.internal.ui.PHPUiPlugin;

import org.eclipse.ui.IEditorInput;

import org.eclipse.ui.part.FileEditorInput;

/**
 * Implementation of {@link org.eclim.plugin.core.project.ProjectManager} for
 * php projects.
 *
 * @author Eric Van Dewoestine
 */
public class PhpProjectManager
  extends DltkProjectManager
{
  @Override
  public String getNatureId()
  {
    return PHPNature.ID;
  }

  @Override
  public void refresh(IProject project, IFile file)
  {
    IEditorInput input = new FileEditorInput(file);
    ISourceModule module = PHPUiPlugin.getEditorInputTypeRoot(input);
    if (module != null){
      try{
        module.makeConsistent(new NullProgressMonitor());
      }catch(CoreException ce){
        throw new RuntimeException(ce);
      }
      // alternate to makeConsistent where moduleDecl can be obtained using
      // PHPSourceParserFactory.parse, like in SrcUpdateCommmand.
      //ISourceModuleInfoCache sourceModuleInfoCache = ModelManager
      //    .getModelManager().getSourceModuleInfoCache();
      //ISourceModuleInfo mifo = sourceModuleInfoCache.get(module);
      //SourceParserUtil.putModuleToCache(
      //    mifo, moduleDecl, ISourceParserConstants.DEFAULT, null);
    }
  }
}
