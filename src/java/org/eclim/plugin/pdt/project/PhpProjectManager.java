/**
 * Copyright (C) 2005 - 2008  Eric Van Dewoestine
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

import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclim.Services;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;

import org.eclim.plugin.pdt.PluginResources;

import org.eclim.project.ProjectManager;

import org.eclim.util.ProjectUtils;
import org.eclim.util.XmlUtils;

import org.eclim.util.file.FileUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.eclipse.php.internal.core.phpModel.parser.PHPIncludePathModelManager;
import org.eclipse.php.internal.core.phpModel.parser.PHPProjectModel;
import org.eclipse.php.internal.core.phpModel.parser.PHPWorkspaceModelManager;

import org.eclipse.php.internal.core.project.IIncludePathEntry;
import org.eclipse.php.internal.core.project.PHPNature;

import org.eclipse.php.internal.core.project.options.PHPProjectOptions;

/**
 * Implementation of {@link ProjectManager} for php projects.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class PhpProjectManager
  implements ProjectManager
{
  private static final String PROJECT_OPTIONS_XSD =
    "/resources/schema/eclipse/projectOptions.xsd";

  /**
   * {@inheritDoc}
   */
  public void create (IProject _project, CommandLine _commandLine)
    throws Exception
  {
    refresh(_project, _commandLine);
  }

  /**
   * {@inheritDoc}
   */
  public List<Error> update (IProject _project, CommandLine _commandLine)
    throws Exception
  {
    // validate that .classpath xml is well formed and valid.
    PluginResources resources = (PluginResources)
      Services.getPluginResources(PluginResources.NAME);
    List<Error> errors = XmlUtils.validateXml(
        _project.getName(),
        ".projectOptions",
        resources.getResource(PROJECT_OPTIONS_XSD).toString());
    if(errors.size() > 0){
      return errors;
    }

    // force the .projectOptions file to be refreshed.
    _project.refreshLocal(IResource.DEPTH_ONE, null);

    PHPNature nature = (PHPNature)_project.getNature(PHPNature.ID);
    // not ideal, but does the job.
    nature.setProject(_project);

    List<String> names =
      Arrays.asList(PHPProjectOptions.getIncludePathVariableNames());

    PHPProjectOptions options = PHPProjectOptions.forProject(_project);
    IIncludePathEntry[] entries = options.readRawIncludePath();
    ArrayList<Error> errs = new ArrayList<Error>();
    for(IIncludePathEntry entry : entries){
      String message = entry.validate();

      // perform validation that pdt does not.
      if (message == null){
        if (entry.getEntryKind() == IIncludePathEntry.IPE_VARIABLE){
          String name = entry.getPath().toString();
          if (!names.contains(name)){
            message = Services.getMessage("variable.not.found", name);
          }
        }else if (entry.getEntryKind() == IIncludePathEntry.IPE_PROJECT){
          String path = entry.getPath().toString();
          if(!path.equals("/" + entry.getResource().getName())){
            message = Services.getMessage("project.path.invalid", path);
          }
        }
      }

      if (message != null){
        // hacky... using filename arg as the entry path to caculate line/col on
        // the vim side.
        errs.add(new Error(message, entry.getPath().toString(), 1, 1, false));
      }
    }

    return errs;
  }

  /**
   * {@inheritDoc}
   */
  public void refresh (IProject _project, CommandLine _commandLine)
    throws Exception
  {
    _project.refreshLocal(IResource.DEPTH_INFINITE, null);

    // FIXME: The following clears the dataModel cache properly, but doesn't
    // reseed it.
    /*PHPWorkspaceModelManager manager = PHPWorkspaceModelManager.getInstance();
    manager.removeModel(_project);

    File cacheDir = new File(ProjectUtils.getPath(_project) + "/.cache");
    if(cacheDir.exists()){
      FileUtils.deleteDirectory(cacheDir);
    }

    PHPProjectModel model = manager.getModelForProject(_project, true);*/
  }

  /**
   * {@inheritDoc}
   */
  public void delete (IProject _project, CommandLine _commandLine)
    throws Exception
  {
  }
}
