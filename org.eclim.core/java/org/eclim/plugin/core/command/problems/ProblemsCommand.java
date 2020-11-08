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
package org.eclim.plugin.core.command.problems;

import java.io.File;

import java.lang.reflect.Field;
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

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.util.CollectionUtils;

import org.eclim.util.file.FileUtils;

import org.eclipse.core.internal.resources.ResourceException;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.ui.IPageLayout;

import org.eclipse.ui.internal.views.markers.CachedMarkerBuilder;
import org.eclipse.ui.internal.views.markers.ExtendedMarkersView;
import org.eclipse.ui.internal.views.markers.MarkerContentGenerator;

import org.eclipse.ui.views.markers.internal.ContentGeneratorDescriptor;
import org.eclipse.ui.views.markers.internal.MarkerSupportRegistry;

/**
 * Command to retrieve a list of global eclipse problems.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "problems",
  options =
    "REQUIRED p project ARG," +
    "OPTIONAL e errors NOARG"
)
public class ProblemsCommand
  extends AbstractCommand
{
  @Override
  @SuppressWarnings("rawtypes")
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    waitOnBuild();

    String name = commandLine.getValue(Options.PROJECT_OPTION);
    boolean errorsOnly = commandLine.hasOption(Options.ERRORS_OPTION);
    IProject project = ProjectUtils.getProject(name);

    ContentGeneratorDescriptor descriptor =
      MarkerSupportRegistry.getInstance().getDefaultContentGenDescriptor();

    ExtendedMarkersView view = new ExtendedMarkersView(descriptor.getId());
    String viewId = IPageLayout.ID_PROBLEM_VIEW;
    MarkerContentGenerator generator = new MarkerContentGenerator(
        descriptor, new CachedMarkerBuilder(view), viewId);

    // hack to disable loading the gui configured filters
    Field enabledFilters = MarkerContentGenerator.class
      .getDeclaredField("enabledFilters");
    enabledFilters.setAccessible(true);
    enabledFilters.set(generator, new HashSet());

    Method gatherMarkers = MarkerContentGenerator.class
      .getDeclaredMethod("gatherMarkers",
          String[].class, Boolean.TYPE, Collection.class, IProgressMonitor.class);
    gatherMarkers.setAccessible(true);

    ArrayList markers = new ArrayList();
    gatherMarkers.invoke(generator,
        generator.getTypes(), true, markers, new NullProgressMonitor());

    ArrayList<Error> problems = new ArrayList<Error>();
    if (markers.size() == 0){
      return problems;
    }

    ArrayList<IProject> projects = new ArrayList<IProject>();
    projects.add(project);
    CollectionUtils.addAll(projects, project.getReferencedProjects());
    CollectionUtils.addAll(projects, project.getReferencingProjects());

    Method getMarker = null;
    for (Object markerEntry : markers){
      if (getMarker == null){
        getMarker = markerEntry.getClass().getDeclaredMethod("getMarker");
        getMarker.setAccessible(true);
      }
      try{
        IMarker marker = (IMarker)getMarker.invoke(markerEntry);
        Map<String, Object> attributes = marker.getAttributes();
        int severity = attributes.containsKey("severity") ?
          ((Integer)attributes.get("severity")).intValue() :
          IMarker.SEVERITY_WARNING;

        // would be more correct to use eclipse marker filter groups, but
        // setting those may be more trouble than they're worth. look into them
        // though if this doesn't prove to be fast enough.
        if(errorsOnly && severity != IMarker.SEVERITY_ERROR){
          continue;
        }

        IResource resource = marker.getResource();
        if (resource == null || resource.getRawLocation() == null){
          continue;
        }

        if (!projects.contains(resource.getProject())){
          continue;
        }

        int offset = attributes.containsKey("charStart") ?
          ((Integer)attributes.get("charStart")).intValue() : 1;
        int line = attributes.containsKey("lineNumber") ?
          ((Integer)attributes.get("lineNumber")).intValue() : 1;
        int[] pos = {1, 1};

        String message = (String)attributes.get("message");
        String path = resource.getLocation().toOSString().replace('\\', '/');
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

    return problems;
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

    @Override
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
        result = e1.getLine() - e2.getLine();
      }
      if (result == 0){
        result = e1.getColumn() - e2.getColumn();
      }
      return result;
    }

    @Override
    public boolean equals(Object obj)
    {
      return super.equals(obj);
    }
  }
}
