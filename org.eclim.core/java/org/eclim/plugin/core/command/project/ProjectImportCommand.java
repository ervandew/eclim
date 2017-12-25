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
package org.eclim.plugin.core.command.project;

import java.io.File;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.ArrayList;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;

import org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage;

/**
 * Command to import a project from a folder.
 *
 * @author Eric Van Dewoestine
 */
@Command(name = "project_import", options = "REQUIRED f folder ARG")
public class ProjectImportCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String folder = commandLine.getValue(Options.FOLDER_OPTION);
    if(folder.endsWith("/") || folder.endsWith("\\")){
      folder = folder.substring(0, folder.length() - 1);
    }

    if (!new File(folder).exists()){
      return Services.getMessage("project.directory.missing", folder);
    }

    File dotproject = new File(folder + "/.project");
    if (!dotproject.exists()){
      return Services.getMessage("project.dotproject.missing", folder);
    }

    // hacky, but I want to re-use the eclipse logic as much as possible.
    WizardProjectsImportPage page = new WizardProjectsImportPage();

    // construct a ProjectRecord
    Constructor<WizardProjectsImportPage.ProjectRecord> construct =
      WizardProjectsImportPage.ProjectRecord.class
      .getDeclaredConstructor(WizardProjectsImportPage.class, File.class);
    construct.setAccessible(true);
    WizardProjectsImportPage.ProjectRecord record =
      construct.newInstance(page, dotproject);

    String projectName = record.getProjectName();
    IProject project = ProjectUtils.getProject(projectName);
    if(project.exists()){
      return Services.getMessage("project.name.exists", projectName, folder);
    }

    // need to initialize the wizard page's 'createdProjects' list.
    Field field = page.getClass().getDeclaredField("createdProjects");
    field.setAccessible(true);
    field.set(page, new ArrayList<IProject>());

    // create the project from the ProjectRecord
    Method method = page.getClass().getDeclaredMethod(
        "createExistingProject",
        WizardProjectsImportPage.ProjectRecord.class,
        IProgressMonitor.class);
    method.setAccessible(true);
    Status result = (Status)method.invoke(page, record, new NullProgressMonitor());

    if (!result.isOK()){
      return Services.getMessage("project.import.failed", projectName);
    }

    return Services.getMessage("project.imported", projectName);
  }
}
