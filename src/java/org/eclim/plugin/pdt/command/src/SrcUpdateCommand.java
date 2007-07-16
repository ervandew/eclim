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
import java.util.Iterator;

import org.apache.commons.io.FilenameUtils;

import org.apache.commons.lang.StringUtils;

import org.apache.log4j.Logger;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.eclipse.ui.internal.EclimWorkbenchPage;

import org.eclim.plugin.pdt.internal.ui.editor.EclimPHPStructuredEditor;

import org.eclim.plugin.wst.command.validate.Reporter;

import org.eclim.util.ProjectUtils;

import org.eclim.util.file.FileOffsets;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.eclipse.jface.text.IDocument;

import org.eclipse.jface.text.reconciler.DirtyRegion;

import org.eclipse.php.internal.core.documentModel.validate.PHPProblemsValidator;

import org.eclipse.php.internal.core.phpModel.parser.PHPWorkspaceModelManager;
import org.eclipse.php.internal.core.phpModel.phpElementData.IPHPMarker;
import org.eclipse.php.internal.core.phpModel.phpElementData.PHPFileData;

import org.eclipse.php.internal.ui.editor.PHPStructuredEditor;

import org.eclipse.php.internal.ui.editor.validation.PHPHTMLValidator;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import org.eclipse.wst.sse.core.StructuredModelManager;

import org.eclipse.wst.sse.core.internal.provisional.IModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;

import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;

import org.eclipse.wst.validation.internal.provisional.core.IMessage;
import org.eclipse.wst.validation.internal.provisional.core.IValidationContext;

/**
 * Command to update and optionally validate a php src file.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class SrcUpdateCommand
  extends AbstractCommand
{
  private static final Logger logger = Logger.getLogger(SrcUpdateCommand.class);

  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
  {
    try{
      final String file = _commandLine.getValue(Options.FILE_OPTION);
      String projectName = _commandLine.getValue(Options.PROJECT_OPTION);

      IProject project = ProjectUtils.getProject(projectName, true);
      IFile ifile = ProjectUtils.getFile(project, file);

      // validate the src file.
      if(_commandLine.hasOption(Options.VALIDATE_OPTION)){
        final String projectPath = FilenameUtils.getFullPath(
            ProjectUtils.getPath(project));

        Reporter reporter = new Reporter();
        /*PHPHTMLValidator validator = new PHPHTMLValidator();
        IValidationContext context = new IValidationContext(){
          public String[] getURIs(){
            return new String[]{file.substring(projectPath.length())};
          }
          public Object loadModel (String name){
            return null;
          }
          public Object loadModel (String name, Object[] params){
            return null;
          }
        };*/

        IModelManager manager = StructuredModelManager.getModelManager();
        IStructuredModel model = manager.getModelForRead(ifile);
        /*IDocument document = model.getStructuredDocument();
        DirtyRegion region = new DirtyRegion(
            0, document.getLength(), DirtyRegion.INSERT,
            "<?php\n  echo \"Hello World.\"\n  echo \"Hello World again.\"\n?>");

PHPStructuredEditor editor = new EclimPHPStructuredEditor(ifile, document);
IWorkbench workbench = PlatformUI.getWorkbench();
IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
((EclimWorkbenchPage)window.getActivePage()).setActiveEditor(editor);

        validator.connect(document);
        validator.validate(region, context, reporter);*/

PHPWorkspaceModelManager.getInstance().addFileToModel(ifile);

PHPProblemsValidator validator = PHPProblemsValidator.getInstance();
validator.validateFile(ifile);
validator.validateFileProblems(ifile, true);

PHPFileData fileData = PHPWorkspaceModelManager.getInstance().getModelForFile(ifile.getFullPath().toString(), true);
System.out.println("### fileData = " + fileData);
IPHPMarker[] markers = fileData.getMarkers();
System.out.println("### markers = " + markers);
System.out.println("### markers.length = " + markers.length);
for(int ii = 0; ii < markers.length; ii++){
  System.out.println("###     " + markers[ii].getDescription());
}

        FileOffsets offsets = FileOffsets.compile(
            ProjectUtils.getFilePath(project, file));

        ArrayList errors = new ArrayList();
        for (Iterator ii = reporter.getMessages().iterator(); ii.hasNext();){
          IMessage message = (IMessage)ii.next();
          int[] lineColumn = offsets.offsetToLineColumn(message.getOffset());
          errors.add(new Error(
              message.getText(),
              file,
              lineColumn[0],
              lineColumn[1],
              false
          ));
        }
        return super.filter(_commandLine,
            (Error[])errors.toArray(new Error[errors.size()]));
      }
    }catch(Exception e){
      return e;
    }
    return StringUtils.EMPTY;
  }
}
