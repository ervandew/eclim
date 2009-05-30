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
package org.eclim.command.project;

import java.io.File;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.util.ProjectUtils;

import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

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
  /**
   * {@inheritDoc}
   */
  public String execute(CommandLine commandLine)
    throws Exception
  {
    String folder = commandLine.getValue(Options.FOLDER_OPTION);
    if(folder.endsWith("/") || folder.endsWith("\\")){
      folder = folder.substring(0, folder.length() - 1);
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

    // create the project from the ProjectRecord
    Method method = page.getClass().getDeclaredMethod(
        "createExistingProject",
        WizardProjectsImportPage.ProjectRecord.class,
        IProgressMonitor.class);
    method.setAccessible(true);
    Boolean result = (Boolean)method.invoke(page, record, new NullProgressMonitor());

    if (!result.booleanValue()){
      return Services.getMessage("project.import.failed", projectName);
    }

    return Services.getMessage("project.imported", projectName);
  }
}
