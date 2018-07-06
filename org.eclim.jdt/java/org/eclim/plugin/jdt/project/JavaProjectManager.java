/**
 * Copyright (C) 2005 - 2018  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import org.eclim.Services;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.plugin.core.project.ProjectManager;

import org.eclim.plugin.core.util.ProjectUtils;
import org.eclim.plugin.core.util.XmlUtils;

import org.eclim.plugin.jdt.PluginResources;

import org.eclim.plugin.jdt.project.classpath.Dependency;
import org.eclim.plugin.jdt.project.classpath.IvyParser;
import org.eclim.plugin.jdt.project.classpath.MvnParser;
import org.eclim.plugin.jdt.project.classpath.Parser;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclim.util.IOUtils;

import org.eclim.util.file.FileOffsets;
import org.eclim.util.file.FileUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;

import org.eclipse.jdt.internal.core.JavaProject;

import org.eclipse.jdt.internal.ui.wizards.ClassPathDetector;

import org.eclipse.jdt.internal.ui.wizards.buildpaths.BuildPathsBlock;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.CPListElement;

import org.eclipse.jdt.ui.PreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Implementation of {@link ProjectManager} for java projects.
 *
 * @author Eric Van Dewoestine
 */
public class JavaProjectManager
  implements ProjectManager
{
  private static final String PRESERVE = "eclim.preserve";

  private static final String CLASSPATH = ".classpath";
  private static final String CLASSPATH_XSD =
    "/resources/schema/eclipse/classpath.xsd";

  private static final HashMap<String, Parser> PARSERS =
    new HashMap<String, Parser>();
  static{
    PARSERS.put("ivy.xml", new IvyParser());
    PARSERS.put("pom.xml", new MvnParser());
  }

  @Override
  public void create(IProject project, CommandLine commandLine)
  {
    String depends = commandLine.getValue(Options.DEPENDS_OPTION);
    create(project, depends);
  }

  @Override
  public List<Error> update(IProject project, CommandLine commandLine)
  {
    String buildfile = commandLine.getValue(Options.BUILD_FILE_OPTION);

    IJavaProject javaProject = JavaUtils.getJavaProject(project);
    try{
      javaProject.getResource().refreshLocal(IResource.DEPTH_INFINITE, null);
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }

    // validate that .classpath xml is well formed and valid.
    PluginResources resources = (PluginResources)
      Services.getPluginResources(PluginResources.NAME);
    List<Error> errors = XmlUtils.validateXml(
        javaProject.getProject().getName(),
        CLASSPATH,
        resources.getResource(CLASSPATH_XSD).toString());
    if(errors.size() > 0){
      return errors;
    }

    String dotclasspath = javaProject.getProject().getFile(CLASSPATH)
      .getRawLocation().toOSString();

    // ivy.xml, pom.xml, etc updated.
    if(buildfile != null){
      try{
        IClasspathEntry[] entries = mergeWithBuildfile(javaProject, buildfile);
        errors = setClasspath(javaProject, entries, dotclasspath);
      }catch(IllegalStateException ise){
        errors.add(new Error(ise.getMessage(), buildfile, 1, 1, false));
      }

    // .classpath updated.
    }else{
      // if an exception occurs reading the classpath then eclipse will return a
      // default classpath which we would otherwise then write back into the
      // .classpath file. This hack prevents that and will return a relevent
      // error message as a validation error.
      try{
        ((JavaProject)javaProject).readFileEntriesWithException(null);
      } catch(Exception e) {
        errors.add(new Error(e.getMessage(), dotclasspath, 1, 1, false));
        return errors;
      }

      IClasspathEntry[] entries = javaProject.readRawClasspath();
      errors = setClasspath(javaProject, entries, dotclasspath);
    }

    if(errors.size() > 0){
      return errors;
    }
    return null;
  }

  @Override
  public void refresh(IProject project, CommandLine commandLine)
  {
  }

  @Override
  public void refresh(IProject project, IFile file)
  {
  }

  @Override
  public void delete(IProject project, CommandLine commandLine)
  {
  }

// Project creation methods

  /**
   * Creates a new project.
   *
   * @param project The project.
   * @param depends Comma separated project names this project depends on.
   */
  protected void create(IProject project, String depends)
  {
    try{
      // with scala-ide installed, apparently this needs to be explicitly done
      IProjectDescription desc = project.getDescription();
      if(!desc.hasNature(PluginResources.NATURE)){
        String[] natures = desc.getNatureIds();
        String[] newNatures = new String[natures.length + 1];
        System.arraycopy(natures, 0, newNatures, 0, natures.length);
        newNatures[natures.length] = PluginResources.NATURE;
        desc.setNatureIds(newNatures);
        project.setDescription(desc, new NullProgressMonitor());
      }

      IJavaProject javaProject = JavaCore.create(project);
      ((JavaProject)javaProject).configure();

      if (!project.getFile(CLASSPATH).exists()) {
        ArrayList<IClasspathEntry> classpath = new ArrayList<IClasspathEntry>();
        boolean source = false;
        boolean container = false;

        ClassPathDetector detector = new ClassPathDetector(project, null);
        for (IClasspathEntry entry : detector.getClasspath()){
          if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE){
            source = true;
          } else if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER){
            container = true;
          }
          classpath.add(entry);
        }

        // default source folder
        if (!source){
          IResource src;
          IPreferenceStore store = PreferenceConstants.getPreferenceStore();
          String name = store.getString(PreferenceConstants.SRCBIN_SRCNAME);
          boolean srcBinFolders = store.getBoolean(
              PreferenceConstants.SRCBIN_FOLDERS_IN_NEWPROJ);
          if (srcBinFolders && name.length() > 0) {
            src = javaProject.getProject().getFolder(name);
          } else {
            src = javaProject.getProject();
          }

          classpath.add(
              new CPListElement(
                javaProject, IClasspathEntry.CPE_SOURCE, src.getFullPath(), src)
              .getClasspathEntry());

          File srcPath = new File(
              ProjectUtils.getFilePath(project, src.getFullPath().toString()));
          if (!srcPath.exists()){
            srcPath.mkdirs();
          }
        }

        // default containers
        if (!container){
          for (IClasspathEntry entry : PreferenceConstants.getDefaultJRELibrary()){
            classpath.add(entry);
          }
        }

        // dependencies on other projects
        IClasspathEntry[] entries =
          createOrUpdateDependencies(javaProject, depends);
        for (IClasspathEntry entry : entries){
          classpath.add(entry);
        }

        javaProject.setRawClasspath(
            classpath.toArray(new IClasspathEntry[classpath.size()]), null);

        // output location
        IPath output = detector.getOutputLocation();
        if (output == null){
          output = BuildPathsBlock.getDefaultOutputLocation(javaProject);
        }
        javaProject.setOutputLocation(output, null);
      }
      javaProject.makeConsistent(null);
      javaProject.save(null, false);
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }
  }

  /**
   * Creates or updates the projects dependencies on other projects.
   *
   * @param project The project.
   * @param depends The comma seperated list of project names.
   * @return Array of IClasspathEntry.
   */
  protected IClasspathEntry[] createOrUpdateDependencies(
      IJavaProject project, String depends)
  {
    if(depends != null){
      String[] dependPaths = StringUtils.split(depends, ',');
      IClasspathEntry[] entries = new IClasspathEntry[dependPaths.length];
      for(int ii = 0; ii < dependPaths.length; ii++){
        IProject theProject = ProjectUtils.getProject(dependPaths[ii]);
        if(!theProject.exists()){
          throw new IllegalArgumentException(Services.getMessage(
              "project.depends.not.found", dependPaths[ii]));
        }
        IJavaProject otherProject = JavaCore.create(theProject);
        entries[ii] = JavaCore.newProjectEntry(otherProject.getPath(), true);
      }
      return entries;
    }
    return new IClasspathEntry[0];
  }

  /**
   * Merges the supplied classpath entries into one.
   *
   * @param entries The array of classpath entry arrays to merge.
   *
   * @return The union of all entry arrays.
   */
  protected IClasspathEntry[] merge(IClasspathEntry[][] entries)
  {
    ArrayList<IClasspathEntry> union = new ArrayList<IClasspathEntry>();
    if(entries != null){
      for(IClasspathEntry[] values : entries){
        if(values != null){
          for(IClasspathEntry entry : values){
            if(!union.contains(entry)){
             union.add(entry);
            }
          }
        }
      }
    }
    return (IClasspathEntry[])union.toArray(new IClasspathEntry[union.size()]);
  }

// Project update methods

  /**
   * Sets the classpath for the supplied project.
   *
   * @param javaProject The project.
   * @param entries The classpath entries.
   * @param classpath The file path of the .classpath file.
   * @return Array of Error or null if no errors reported.
   */
  protected List<Error> setClasspath(
      IJavaProject javaProject, IClasspathEntry[] entries, String classpath)
  {
    ArrayList<Error> errors = new ArrayList<Error>();
    try{
      FileOffsets offsets = FileOffsets.compile(classpath);
      String classpathValue = IOUtils.toString(new FileInputStream(classpath));
      for(IClasspathEntry entry : entries){
        IJavaModelStatus status = JavaConventions
          .validateClasspathEntry(javaProject, entry, true);
        if(!status.isOK()){
          errors.add(createErrorForEntry(
                javaProject, entry, status, offsets, classpath, classpathValue));
        }
      }

      IJavaModelStatus status = JavaConventions.validateClasspath(
          javaProject, entries, javaProject.getOutputLocation());

      // always set the classpathValue anyways, so that the user can correct the
      // file.
      //if(status.isOK() && errors.isEmpty()){
        javaProject.setRawClasspath(entries, null);
        javaProject.makeConsistent(null);
      //}

      if(!status.isOK()){
        errors.add(new Error(status.getMessage(), classpath, 1, 1, false));
      }
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }catch(IOException ioe){
      throw new RuntimeException(ioe);
    }
    return errors;
  }

  /**
   * Creates an Error from the supplied IJavaModelStatus.
   *
   * @param project The java project.
   * @param entry The classpath entry.
   * @param status The IJavaModelStatus.
   * @param offsets File offsets for the classpath file.
   * @param filename The filename of the error.
   * @param contents The contents of the file as a String.
   * @return The Error.
   */
  protected Error createErrorForEntry(
      IJavaProject project,
      IClasspathEntry entry,
      IJavaModelStatus status,
      FileOffsets offsets,
      String filename,
      String contents)
  {
    int line = 0;
    int col = 0;

    String path = entry.getPath().toOSString();
    path = path.replaceFirst("^/" + project.getProject().getName() + "/", "");
    Matcher matcher =
      Pattern.compile("path\\s*=(['\"])\\s*\\Q" + path + "\\E\\s*\\1")
        .matcher(contents);
    if(matcher.find()){
      int[] position = offsets.offsetToLineColumn(matcher.start());
      line = position[0];
      col = position[1];
    }

    return new Error(status.getMessage(), filename, line, col, false);
  }

  /**
   * Merge the supplied project's classpath with entries found in the specified
   * build file.
   *
   * @param project The project.
   * @param buildfile The path to the build file (pom.xml, ivy.xml, etc)
   * @return The classpath entries.
   */
  protected IClasspathEntry[] mergeWithBuildfile(
      IJavaProject project, String buildfile)
  {
    String filename = FileUtils.getBaseName(buildfile);
    Parser parser = PARSERS.get(filename);
    String var = parser.getClasspathVar();
    Dependency[] dependencies = parser.parse(buildfile);

    IWorkspaceRoot root = project.getProject().getWorkspace().getRoot();
    ArrayList<IClasspathEntry> results = new ArrayList<IClasspathEntry>();

    // load the results with all the non library entries.
    IClasspathEntry[] entries = null;
    try{
      entries = project.getRawClasspath();
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }
    for(IClasspathEntry entry : entries){
      if (entry.getEntryKind() != IClasspathEntry.CPE_LIBRARY &&
          entry.getEntryKind() != IClasspathEntry.CPE_VARIABLE)
      {
        results.add(entry);
      }else{
        IPath path = entry.getPath();
        String prefix = path != null ? path.segment(0) : null;
        if ((!var.equals(prefix)) || preserve(entry)){
          results.add(entry);
        }
      }
    }

    // merge the dependencies with the classpath entires.
    for(int ii = 0; ii < dependencies.length; ii++){
      IClasspathEntry match = null;
      for(int jj = 0; jj < entries.length; jj++){
        if (entries[jj].getEntryKind() == IClasspathEntry.CPE_LIBRARY ||
            entries[jj].getEntryKind() == IClasspathEntry.CPE_VARIABLE)
        {
          String path = entries[jj].getPath().toOSString();
          String pattern = dependencies[ii].getName() +
            Dependency.VERSION_SEPARATOR;

          // exact match
          if(path.endsWith(dependencies[ii].toString())){
            match = entries[jj];
            results.add(entries[jj]);
            break;

          // different version match
          }else if(path.indexOf(pattern) != -1){
            break;
          }
        }else if(entries[jj].getEntryKind() == IClasspathEntry.CPE_PROJECT){
          String path = entries[jj].getPath().toOSString();
          if(path.endsWith(dependencies[ii].getName())){
            match = entries[jj];
            break;
          }
        }
      }

      if(match == null){
        IClasspathEntry entry = createEntry(root, project, dependencies[ii]);
        results.add(entry);
      }else{
        match = null;
      }
    }

    return (IClasspathEntry[])
      results.toArray(new IClasspathEntry[results.size()]);
  }

  /**
   * Determines if the supplied entry contains attribute indicating that it
   * should not be removed.
   *
   * @param entry The IClasspathEntry
   * @return true to preserve the entry, false otherwise.
   */
  protected boolean preserve(IClasspathEntry entry)
  {
    IClasspathAttribute[] attributes = entry.getExtraAttributes();
    for(int ii = 0; ii < attributes.length; ii++){
      String name = attributes[ii].getName();
      if(PRESERVE.equals(name)){
        return Boolean.parseBoolean(attributes[ii].getValue());
      }
    }
    return false;
  }

  /**
   * Creates the classpath entry.
   *
   * @param root The workspace root.
   * @param project The project to create the dependency in.
   * @param dependency The dependency to create the entry for.
   * @return The classpath entry.
   */
  protected IClasspathEntry createEntry(
      IWorkspaceRoot root, IJavaProject project, Dependency dependency)
  {
    if(dependency.isVariable()){
      return JavaCore.newVariableEntry(dependency.getPath(), null, null, true);
    }

    return JavaCore.newLibraryEntry(dependency.getPath(), null, null, true);
  }

  /**
   * Determines if the supplied path starts with a variable name.
   *
   * @param path The path to test.
   * @return True if the path starts with a variable name, false otherwise.
   */
  protected boolean startsWithVariable(String path)
  {
    String[] variables = JavaCore.getClasspathVariableNames();
    for(int ii = 0; ii < variables.length; ii++){
      if(path.startsWith(variables[ii])){
        return true;
      }
    }
    return false;
  }
}
