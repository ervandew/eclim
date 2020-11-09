/**
 * Copyright (C) 2005 - 2020  Eric Van Dewoestine
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
package org.eclim.plugin.core.command.refactoring;

import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.project.ProjectManagement;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.ltk.core.refactoring.TextFileChange;

/**
 * Abstract super command for refactoring commands.
 *
 * @author Eric Van Dewoestine
 */
public abstract class AbstractRefactorCommand
  extends AbstractCommand
{
  private static final String PREVIEW_OPTION = "v";
  private static final String DIFF_OPTION = "d";

  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    // refresh the projects that may be affected to ensure eclipse is aware of
    // the current state of every file that could potentially be changed.
    if (!commandLine.hasOption(PREVIEW_OPTION)){
      String projectName = commandLine.getValue(Options.PROJECT_OPTION);
      IProject project = ProjectUtils.getProject(projectName, true);
      ProjectManagement.refresh(project, commandLine);
      IProject[] references = project.getReferencingProjects();
      for (IProject p : references){
        ProjectManagement.refresh(p, commandLine);
      }
    }

    try{
      NullProgressMonitor monitor = new NullProgressMonitor();
      Refactor refactor = createRefactoring(commandLine);
      Refactoring refactoring = refactor.refactoring;

      RefactoringStatus status = refactoring.checkAllConditions(
          SubMonitor.convert(monitor, 4));
      int stopSeverity = RefactoringCore.getConditionCheckingFailedSeverity();
      if (status.getSeverity() >= stopSeverity) {
        throw new RefactorException(status);
      }

      Change change = refactoring.createChange(SubMonitor.convert(monitor, 2));
      change.initializeValidationData(SubMonitor.convert(monitor, 1));

      // preview
      if (commandLine.hasOption(PREVIEW_OPTION)){
        // preview a specific file
        if (commandLine.hasOption(DIFF_OPTION)){
          return previewChange(change, commandLine.getValue("d"));
        }

        HashMap<String,Object> preview = new HashMap<String,Object>();
        String previewOpt = "-" + PREVIEW_OPTION;
        String[] args = commandLine.getArgs();
        StringBuffer apply = new StringBuffer();
        for (String arg : args){
          if (arg.equals(previewOpt)){
            continue;
          }
          if (apply.length() > 0){
            apply.append(' ');
          }
          if (arg.startsWith("-")){
            apply.append(arg);
          }else{
            apply.append('"').append(arg).append('"');
          }
        }
        // the command to apply the change minus the editor + pretty options.
        preview.put("apply", apply.toString()
            .replaceFirst("-" + Options.EDITOR_OPTION + "\\s\"\\w+\" ", "")
            .replaceFirst("-" + Options.PRETTY_OPTION + ' ', ""));
        preview.put("changes", previewChanges(change));
        return preview;
      }

      IWorkspace workspace = ResourcesPlugin.getWorkspace();
      ResourceChangeListener rcl = new ResourceChangeListener();
      workspace.addResourceChangeListener(rcl);
      try{
        PerformChangeOperation changeOperation = new PerformChangeOperation(change);
        // passing in refactor.name to the change op doesn't seem to do the
        // trick, so lets force our name on the change since the undo manager
        // will use the change's name if label is null (which it shouldn't be,
        // but is, hence this hack).
        if (change instanceof CompositeChange){
          try{
            Field fName = CompositeChange.class.getDeclaredField("fName");
            fName.setAccessible(true);
            fName.set(change, refactor.name);
          }catch(NoSuchFieldException nsfe){
            // change doesn't have the expected fName field.
          }
        }

        changeOperation.setUndoManager(
            RefactoringCore.getUndoManager(), change.getName());

        changeOperation.run(SubMonitor.convert(monitor, 4));
        return rcl.getChangedFiles();
      }finally{
        workspace.removeResourceChangeListener(rcl);
      }
    }catch(RefactorException re){
      HashMap<String,List<String>> result = new HashMap<String,List<String>>();
      List<String> errors = new ArrayList<String>();

      if (re.getMessage() != null){
        errors.add(re.getMessage());
      }

      RefactoringStatus status = re.getStatus();
      if (status != null){
        for (RefactoringStatusEntry entry : status.getEntries()){
          String message = entry.getMessage();
          if (!errors.contains(message) &&
              !message.startsWith("Found potential matches"))
          {
            errors.add(message);
          }
        }
      }

      result.put("errors", errors);
      return result;
    }
  }

  /**
   * Method to be ovrriden by subclasses to get the Change representing the
   * refactoring.
   *
   * @param commandLine The original command line.
   * @return The refactoring change.
   */
  public abstract Refactor createRefactoring(CommandLine commandLine);

  private ArrayList<HashMap<String,String>> previewChanges(Change change)
  {
    ArrayList<HashMap<String,String>> results =
      new ArrayList<HashMap<String,String>>();

    if (change instanceof CompositeChange){
      for (Change c : ((CompositeChange)change).getChildren()){
        results.addAll(previewChanges(c));
      }
    }else{
      HashMap<String,String> result = new HashMap<String,String>();
      if (change instanceof TextFileChange){
        TextFileChange text = (TextFileChange)change;
        result.put("type", "diff");
        result.put("file",
            text.getFile().getLocation().toOSString().replace('\\', '/'));
      }else{
        result.put("type", "other");
        result.put("message", change.toString());
      }
      results.add(result);
    }
    return results;
  }

  private String previewChange(Change change, String file)
  {
    if (change instanceof CompositeChange){
      for (Change c : ((CompositeChange)change).getChildren()){
        String preview = previewChange(c, file);
        if(preview != null){
          return preview;
        }
      }
    }else{
      if (change instanceof TextFileChange){
        TextFileChange text = (TextFileChange)change;
        String path = text.getFile().getLocation()
          .toOSString().replace('\\', '/');
        if (path.equals(file)){
          try{
            return text.getPreviewContent(new NullProgressMonitor());
          }catch(CoreException ce){
            throw new RuntimeException(ce);
          }
        }
      }
    }
    return null;
  }
}
