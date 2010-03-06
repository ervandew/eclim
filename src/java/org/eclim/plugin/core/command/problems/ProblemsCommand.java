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
package org.eclim.plugin.core.command.problems;

import java.io.File;

import java.lang.reflect.Method;

import java.text.Collator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;

import java.util.regex.Pattern;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.plugin.core.CorePlugin;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.command.filter.ErrorFilter;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.util.CollectionUtils;
import org.eclim.util.StringUtils;

import org.eclim.util.file.FileUtils;

import org.eclipse.core.internal.resources.ResourceException;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.ui.internal.views.markers.MarkerContentGenerator;

import org.eclipse.ui.views.markers.internal.MarkerSupportRegistry;

/**
 * Command to retrieve a list of global eclipse problems.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "problems",
  options = "REQUIRED p project ARG"
)
public class ProblemsCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public String execute(CommandLine commandLine)
    throws Exception
  {
    waitOnBuild();

    String name = commandLine.getValue(Options.PROJECT_OPTION);
    IProject project = ProjectUtils.getProject(name);

    MarkerContentGenerator generator =
      MarkerSupportRegistry.getInstance()
        .getGenerator(MarkerSupportRegistry.PROBLEMS_GENERATOR);

    Method generateFilteredMarkers = MarkerContentGenerator.class
      .getDeclaredMethod(
          "generateFilteredMarkers",
          SubProgressMonitor.class,
          Boolean.TYPE,
          IResource[].class,
          Collection.class);
    generateFilteredMarkers.setAccessible(true);
    Object markers = generateFilteredMarkers.invoke(
        generator,
        new SubProgressMonitor(new NullProgressMonitor(), 10),
        false /* 'and' the filters? */,
        new IResource[0],
        new HashSet<Object>() /* filters, MarkerFieldFilterGroup */);

    Method getSize = markers.getClass().getDeclaredMethod("getSize");
    getSize.setAccessible(true);
    Method elementAt = markers.getClass()
      .getDeclaredMethod("elementAt", Integer.TYPE);
    elementAt.setAccessible(true);

    Method getMarker = null;
    int size = ((Integer)getSize.invoke(markers)).intValue();
    if (size == 0){
      return StringUtils.EMPTY;
    }

    ArrayList<IProject> projects = new ArrayList<IProject>();
    projects.add(project);
    CollectionUtils.addAll(projects, project.getReferencedProjects());
    CollectionUtils.addAll(projects, project.getReferencingProjects());

    ArrayList<Error> problems = new ArrayList<Error>();
    for (int ii = 0; ii < size; ii++){
      Object markerEntry = elementAt.invoke(markers, ii);
      if (getMarker == null){
        getMarker = markerEntry.getClass().getDeclaredMethod("getMarker");
        getMarker.setAccessible(true);
      }
      try{
        IMarker marker = (IMarker)getMarker.invoke(markerEntry);
        IResource resource = marker.getResource();
        if (resource == null || resource.getRawLocation() == null){
          continue;
        }

        if (!projects.contains(resource.getProject())){
          continue;
        }

        @SuppressWarnings("unchecked")
        Map<String,Object> attributes = marker.getAttributes();
        String message = (String)attributes.get("message");
        int severity = attributes.containsKey("severity") ?
          ((Integer)attributes.get("severity")).intValue() :
          IMarker.SEVERITY_WARNING;
        int offset = attributes.containsKey("charStart") ?
          ((Integer)attributes.get("charStart")).intValue() : 1;
        int line = attributes.containsKey("lineNumber") ?
          ((Integer)attributes.get("lineNumber")).intValue() : 1;
        int[] pos = {1, 1};

        String path = resource.getRawLocation().toOSString().replace('\\', '/');
        File file = new File(path);
        if (file.isFile() && file.exists() && offset > 0){
          pos = FileUtils.offsetToLineColumn(path, offset);
        }
        problems.add(new Error(
              message,
              path,
              Math.max(pos[0], line),
              pos[1],
              severity != IMarker.SEVERITY_ERROR));
      }catch(ResourceException ignore){
        // race condition, i think, where we are attempting to obtain a
        // marker that has been removed since obtaining our list.
      }
    }

    Collections.sort(problems, new ProblemComparator(project));

    return ErrorFilter.instance.filter(commandLine, problems);
  }

  private void waitOnBuild()
  {
    CorePlugin plugin = CorePlugin.getDefault();
    int tries = 0;
    while(tries < 30 && plugin.isBuildRunning()){
      try{
        Thread.sleep(100);
      }catch(Exception ignore){
      }
      tries++;
    }
  }

  private static class ProblemComparator
    implements Comparator<Error>
  {
    private Pattern projectPattern;
    private Collator collator = Collator.getInstance();

    /**
     * Constructs a new instance.
     *
     * @param project The project for this instance.
     */
    public ProblemComparator(IProject project)
      throws Exception
    {
      this.projectPattern =
        Pattern.compile("^\\Q" + ProjectUtils.getPath(project) + "\\E\\b.*");
    }

    /**
     * {@inheritDoc}
     * @see Comparator#compare(T,T)
     */
    public int compare(Error e1, Error e2)
    {
      String ef1 = e1.getFilename();
      boolean e1InProject = projectPattern.matcher(ef1).matches();

      String ef2 = e2.getFilename();
      boolean e2InProject = projectPattern.matcher(ef2).matches();

      if (e1InProject && !e2InProject){
        return -1;
      }

      if (e2InProject && !e1InProject){
        return 1;
      }

      int result = collator.compare(ef1, ef2);
      if (result == 0){
        result = e1.getLineNumber() - e2.getLineNumber();
      }
      if (result == 0){
        result = e1.getColumnNumber() - e2.getColumnNumber();
      }
      return result;
    }

    /**
     * {@inheritDoc}
     * @see Comparator#equals(Object)
     */
    public boolean equals(Object obj)
    {
      return super.equals(obj);
    }
  }
}
