/**
 * Copyright (c) 2005 - 2006
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclim.plugin.pdt.command.src;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.util.ProjectUtils;

import org.eclim.util.file.FileOffsets;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import org.eclipse.php.internal.core.documentModel.validate.PHPProblemsValidator;

import org.eclipse.php.internal.core.phpModel.parser.PHPWorkspaceModelManager;
import org.eclipse.php.internal.core.phpModel.parser.PhpParserSchedulerTask;

import org.eclipse.php.internal.core.phpModel.phpElementData.IPHPMarker;
import org.eclipse.php.internal.core.phpModel.phpElementData.PHPFileData;

/**
 * Command to update and optionally validate a php src file.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class SrcUpdateCommand
  extends AbstractCommand
{
  private static PhpParserSchedulerTask scheduler =
    PhpParserSchedulerTask.getInstance();

  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
  {
    try{
      String file = _commandLine.getValue(Options.FILE_OPTION);
      String projectName = _commandLine.getValue(Options.PROJECT_OPTION);

      IProject project = ProjectUtils.getProject(projectName, true);
      IFile ifile = ProjectUtils.getFile(project, file);

      // validate the src file.
      if(_commandLine.hasOption(Options.VALIDATE_OPTION)){
        // ensure model is refreshed with latest version of the file.
        PHPWorkspaceModelManager.getInstance().addFileToModel(ifile);
        long timeout = 5000;
        long now = System.currentTimeMillis();
        while (!scheduler.isDone(ifile.getFullPath().toString()) &&
            (System.currentTimeMillis() - now) < timeout)
        {
          Thread.sleep(100);
        }

        PHPProblemsValidator validator = PHPProblemsValidator.getInstance();
        validator.validateFileProblems(ifile, true);

        PHPFileData fileData = PHPWorkspaceModelManager.getInstance()
          .getModelForFile(ifile.getFullPath().toString(), true);
        IPHPMarker[] markers = fileData.getMarkers();

        if(markers.length > 0){
          String filepath = ProjectUtils.getFilePath(project, file);
          FileOffsets offsets = FileOffsets.compile(filepath);
          ArrayList errors = new ArrayList();
          for(int ii = 0; ii < markers.length; ii++){
            IPHPMarker marker = markers[ii];
            int[] lineColumn = offsets.offsetToLineColumn(
                marker.getUserData().getStartPosition());
            errors.add(new Error(
                marker.getDescription(),
                filepath,
                lineColumn[0],
                lineColumn[1],
                !IPHPMarker.ERROR.equals(marker.getType())
            ));
          }
          return super.filter(_commandLine,
              (Error[])errors.toArray(new Error[errors.size()]));
        }
      }
    }catch(Exception e){
      return e;
    }
    return StringUtils.EMPTY;
  }
}
