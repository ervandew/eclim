/**
 * Copyright (C) 2005 - 2010  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.refactoring;

import java.io.StringWriter;
import java.io.Writer;

import java.util.ArrayList;
import java.util.List;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.project.ProjectManagement;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.util.StringUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
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

  protected ThreadLocal<ResourceChangeListener> listener =
    new ThreadLocal<ResourceChangeListener>();

  /**
   * {@inheritDoc}
   * @see org.eclim.command.Command#execute(CommandLine)
   */
  public String execute(CommandLine commandLine)
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
      Refactoring refactoring = createRefactoring(commandLine);

      RefactoringStatus status = refactoring.checkAllConditions(
          new SubProgressMonitor(monitor, 4));
      int stopSeverity = RefactoringCore.getConditionCheckingFailedSeverity();
      if (status.getSeverity() >= stopSeverity) {
        return status.getEntryWithHighestSeverity().getMessage();
      }

      Change change = refactoring.createChange(new SubProgressMonitor(monitor, 2));
      change.initializeValidationData(new SubProgressMonitor(monitor, 1));

      // preview
      if (commandLine.hasOption(PREVIEW_OPTION)){
        // preview a specific file
        if (commandLine.hasOption(DIFF_OPTION)){
          return previewChange(change, commandLine.getValue("d"));
        }

        StringWriter writer = new StringWriter();
        writer.write(StringUtils.join(commandLine.getArgs(), ','));
        writer.write('\n');
        previewChanges(change, writer);
        return writer.toString();
      }

      IWorkspace workspace = ResourcesPlugin.getWorkspace();
      ResourceChangeListener rcl = new ResourceChangeListener();
      listener.set(rcl);
      workspace.addResourceChangeListener(rcl);
      try{
        PerformChangeOperation changeOperation = new PerformChangeOperation(change);
        changeOperation.setUndoManager(
            RefactoringCore.getUndoManager(), refactoring.getName());

        changeOperation.run(new SubProgressMonitor(monitor, 4));
        return getChangedFilesOutput();
      }finally{
        workspace.removeResourceChangeListener(rcl);
      }
    }catch(RefactorException re){
      return re.getMessage();
    }
  }

  /**
   * Method to be ovrriden by subclasses to get the Change representing the
   * refactoring.
   *
   * @param commandLine The original command line.
   * @return The refactoring change.
   */
  public abstract Refactoring createRefactoring(CommandLine commandLine)
    throws Exception;

  /**
   * Builds an output string containing the list of changed files for
   * consumption by the client.
   *
   * The format is as follows:
   * <pre>
   * files:
   * /the/first/file/file1.txt
   * /the/second/file/file2.txt
   * </pre>
   *
   * @return The changed files output string.
   */
  public String getChangedFilesOutput()
    throws Exception
  {
    StringBuffer changed = new StringBuffer();
    for (IResourceDelta delta : listener.get().getResourceDeltas()){
      int flags = delta.getFlags();
      // the moved_from entry should handle this
      if ((flags & IResourceDelta.MOVED_TO) != 0){
        continue;
      }

      if (changed.length() > 0){
        changed.append('\n');
      }

      IResource resource = delta.getResource();
      if ((flags & IResourceDelta.MOVED_FROM) != 0){
        String path = ProjectUtils.getFilePath(
            resource.getProject(), delta.getMovedFromPath().toOSString());
        changed.append(path).append(" -> ");
      }

      changed.append(resource.getRawLocation().toOSString());
    }
    return "files:\n" + changed.toString();
  }

  private void previewChanges(Change change, Writer writer)
    throws Exception
  {
    if (change instanceof CompositeChange){
      for (Change c : ((CompositeChange)change).getChildren()){
        previewChanges(c, writer);
      }
    }else{
      if (change instanceof TextFileChange){
        TextFileChange text = (TextFileChange)change;
        writer.write("diff: ");
        writer.write(text.getFile().getRawLocation().toOSString().replace('\\', '/'));
      }else{
        writer.write("other: ");
        writer.write(change.toString());
      }
      writer.write('\n');
    }
  }

  private String previewChange(Change change, String file)
    throws Exception
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
        if (text.getFile().getRawLocation().toOSString().replace('\\', '/').equals(file)){
          return text.getPreviewContent(new NullProgressMonitor());
        }
      }
    }
    return null;
  }

  /**
   * Resource change listener use to collect a list of relevant resource deltas.
   */
  protected class ResourceChangeListener
    implements IResourceChangeListener, IResourceDeltaVisitor
  {
    private List<IResourceDelta> deltas = new ArrayList<IResourceDelta>();

    /**
     * {@inheritDoc}
     * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
     */
    public void resourceChanged(IResourceChangeEvent event)
    {
      try{
        event.getDelta().accept(this);
      }catch(CoreException ce){
        throw new RuntimeException(ce);
      }
    }

    /**
     * {@inheritDoc}
     * @see IResourceDeltaVisitor#visit(IResourceDelta)
     */
    public boolean visit(IResourceDelta delta)
      throws CoreException
    {
      IResource resource = delta.getResource();
      if (delta.getKind() != IResourceDelta.NO_CHANGE && (
            resource.getType() == IResource.FILE ||
            resource.getType() == IResource.FOLDER))
      {
        deltas.add(delta);
      }
      return true;
    }

    /**
     * Gets a list of relevant leaf node resource deltas.
     *
     * @return list of IResourceDelta.
     */
    public List<IResourceDelta> getResourceDeltas()
    {
      return deltas;
    }
  }
}
