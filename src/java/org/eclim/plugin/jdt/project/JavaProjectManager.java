/**
 * Copyright (c) 2004 - 2006
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
package org.eclim.plugin.jdt.project;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.collections.CollectionUtils;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import org.apache.commons.lang.StringUtils;

import org.eclim.Services;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.plugin.jdt.JavaUtils;

import org.eclim.plugin.jdt.project.classpath.Dependency;
import org.eclim.plugin.jdt.project.classpath.Parser;

import org.eclim.preference.Preferences;

import org.eclim.project.ProjectManager;

import org.eclim.util.XmlUtils;

import org.eclim.util.file.FileUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;

import org.eclipse.jdt.internal.ui.wizards.ClassPathDetector;

import org.eclipse.jdt.launching.JavaRuntime;

import org.jaxen.XPath;

import org.jaxen.dom.DOMXPath;

import org.w3c.dom.Document;
import org.w3c.dom.Text;

/**
 * Implementation of {@link ProjectManager} for java projects.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class JavaProjectManager
  implements ProjectManager
{
  private static DocumentBuilderFactory factory;
  private static XPath xpath;

  private static final Pattern STATUS_PATTERN =
    Pattern.compile(".*[\\\\/](.*)'.*");

  private String libraryRootPreference;
  private Preferences preferences;

  /**
   * {@inheritDoc}
   */
  public Object create (String _name, String _folder, CommandLine _commandLine)
    throws Exception
  {
    String depends = _commandLine.getValue(Options.DEPENDS_OPTION);
    create(_name, _folder, depends);
    return Services.getMessage("project.created", _name);
  }

  /**
   * {@inheritDoc}
   */
  public Object update (String _name, CommandLine _commandLine)
    throws Exception
  {
    String buildfile = _commandLine.getValue(Options.BUILD_FILE_OPTION);
    String settings = _commandLine.getValue(Options.SETTINGS_OPTION);

    IJavaProject javaProject = JavaUtils.getJavaProject(_name);

    // project settings update
    if(settings != null){
      updateSettings(javaProject, settings);
      javaProject.makeConsistent(null);
    }else{
      // validate that .classpath xml is well formed and valid.
      String dotclasspath = javaProject.getProject().getFile(".classpath")
        .getRawLocation().toOSString();
      Error[] errors = XmlUtils.validateXml(dotclasspath,
          System.getProperty("eclim.home") +
          "/resources/schema/eclipse/classpath.xsd");
      if(errors.length > 0){
        return errors;
      }

      // ivy.xml, project.xml, etc updated.
      if(buildfile != null){
        String filename = FilenameUtils.getName(buildfile);
        Parser parser = (Parser)Services.getService(filename, Parser.class);
        IClasspathEntry[] entries = merge(javaProject, parser.parse(buildfile));
        errors = setClasspath(javaProject, entries, dotclasspath);

      // .classpath updated.
      }else{
        IClasspathEntry[] entries = javaProject.readRawClasspath();
        errors = setClasspath(javaProject, entries, dotclasspath);
      }

      if(errors.length > 0){
        return errors;
      }
    }
    return Services.getMessage("project.updated", _name);
  }

  /**
   * {@inheritDoc}
   */
  public Object delete (String _name, CommandLine _commandLine)
    throws Exception
  {
    return null;
  }

// Project creation methods

  /**
   * Creates a new project.
   *
   * @param _name The project name.
   * @param _folder The project root folder.
   * @param _depends Comma seperated project names this project depends on.
   */
  protected void create (String _name, String _folder, String _depends)
    throws Exception
  {
    deleteStaleProject(_name, _folder);
    IProject project = createProject(_name, _folder);

    project.open(null);

    IJavaProject javaProject = JavaCore.create(project);
    ClassPathDetector detector = new ClassPathDetector(project, null);
    IClasspathEntry[] detected = detector.getClasspath();
    IClasspathEntry[] depends =
      createOrUpdateDependencies(javaProject, _depends);
    IClasspathEntry[] container = new IClasspathEntry[]{
      JavaCore.newContainerEntry(new Path(JavaRuntime.JRE_CONTAINER))
    };

    IClasspathEntry[] classpath = merge(
        new IClasspathEntry[][]{
          javaProject.readRawClasspath(), detected, depends, container
        });

    javaProject.setRawClasspath(classpath, null);
    javaProject.makeConsistent(null);
    javaProject.save(null, false);
  }

  /**
   * Handle deleting the stale project if it exists.
   *
   * @param _name  The project name.
   * @param _folder The project folder.
   */
  protected void deleteStaleProject (String _name, String _folder)
    throws Exception
  {
    // check for same project location w/ diff project name, or a stale
    // .project file.
    File projectFile = new File(_folder + File.separator + ".project");
    if(projectFile.exists()){
      if(xpath == null){
        xpath = new DOMXPath("/projectDescription/name/text()");
        factory = DocumentBuilderFactory.newInstance();
      }
      Document document = factory.newDocumentBuilder().parse(projectFile);
      String projectName = "";
      Object result = xpath.selectSingleNode(document);
      if(result != null){
        projectName = ((Text)result).getData();
      }

      if(!projectName.equals(_name)){
        IProject project =
          ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        if(project.exists()){
          project.delete(false/*deleteContent*/, true/*force*/, null/*monitor*/);
        }else{
          projectFile.delete();
        }
      }
    }
  }

  /**
   * Handle creation of project if necessary.
   *
   * @param _name  The project name.
   * @param _folder The project folder.
   *
   * @return The created project.
   */
  protected IProject createProject (String _name, String _folder)
    throws CoreException
  {
    // create the project if it doesn't already exist.
    IProject project =
      ResourcesPlugin.getWorkspace().getRoot().getProject(_name);
    if(!project.exists()){
      IPath location = new Path(_folder);
      IProjectDescription description =
        ResourcesPlugin.getWorkspace().newProjectDescription(_name);
      // location must not overlap the workspace.
      description.setLocation(location);
      description.setNatureIds(new String[]{JavaCore.NATURE_ID});

      project.create(description, null/*monitor*/);
    }

    // check if the existing project is located elsewhere.
    File path = project.getLocation().toFile();
    if(!path.equals(new File(_folder))){
      throw new IllegalArgumentException(Services.getMessage(
          "project.name.exists",
          new Object[]{_name, path.toString()}));
    }

    return project;
  }

  /**
   * Creates or updates the projects dependecies other other projects.
   *
   * @param _project The project.
   * @param _depends The comma seperated list of project names.
   */
  protected IClasspathEntry[] createOrUpdateDependencies (
      IJavaProject _project, String _depends)
    throws CoreException
  {
    if(_depends != null){
      String[] dependPaths = StringUtils.split(_depends, ',');
      IClasspathEntry[] entries = new IClasspathEntry[dependPaths.length];
      for(int ii = 0; ii < dependPaths.length; ii++){
        IProject theProject =
          ResourcesPlugin.getWorkspace().getRoot().getProject(dependPaths[ii]);
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
   * @param _entries The array of classpath entry arrays to merge.
   *
   * @return The union of all entry arrays.
   */
  protected IClasspathEntry[] merge (IClasspathEntry[][] _entries)
  {
    Collection union = new ArrayList();
    if(_entries != null){
      for(int ii = 0; ii < _entries.length; ii++){
        if(_entries[ii] != null){
          union = CollectionUtils.union(union, Arrays.asList(_entries[ii]));
        }
      }
    }
    return (IClasspathEntry[])union.toArray(new IClasspathEntry[union.size()]);
  }

// Project update methods

  /**
   * Sets the classpath for the supplied project.
   *
   * @param _javaProject The project.
   * @param _entries The classpath entries.
   * @param _classpath The file path of the .classpath file.
   * @return Array of Error or null if no errors reported.
   */
  protected Error[] setClasspath (
      IJavaProject _javaProject, IClasspathEntry[] _entries, String _classpath)
    throws Exception
  {
    String classpath = IOUtils.toString(new FileInputStream(_classpath));
    List errors = new ArrayList();
    for(int ii = 0; ii < _entries.length; ii++){
      IJavaModelStatus status = JavaConventions.validateClasspathEntry(
          _javaProject, _entries[ii], true);
      if(!status.isOK()){
        errors.add(createErrorFromStatus(_classpath, classpath, status));
      }
    }

    IJavaModelStatus status = JavaConventions.validateClasspath(
        _javaProject, _entries, _javaProject.getOutputLocation());

    // always set the classpath anyways, so that the user can correct the file.
    //if(status.isOK() && errors.isEmpty()){
      _javaProject.setRawClasspath(_entries, null);
      _javaProject.makeConsistent(null);
    //}

    if(!status.isOK()){
      errors.add(createErrorFromStatus(_classpath, classpath, status));
    }
    return (Error[])errors.toArray(new Error[errors.size()]);
  }

  /**
   * Creates an Error from the supplied IJavaModelStatus.
   *
   * @param _filename The filename of the error.
   * @param _contents The contents of the file as a String.
   * @param _status The IJavaModelStatus.
   * @return The Error.
   */
  protected Error createErrorFromStatus (
      String _filename, String _contents, IJavaModelStatus _status)
    throws Exception
  {
    int line = 0;
    int col = 0;

    // get the pattern to search for from the status message.
    Matcher matcher = STATUS_PATTERN.matcher(_status.getMessage());
    String pattern = matcher.replaceFirst("$1");

    // find the pattern in the classpath file.
    matcher = Pattern.compile("\\Q" + pattern + "\\E").matcher(_contents);
    if(matcher.find()){
      int[] position = FileUtils.offsetToLineColumn(_filename, matcher.start());
      line = position[0];
      col = position[1];
    }

    return new Error(_status.getMessage(), _filename, line, col, false);
  }

  /**
   * Updates the projects settings.
   *
   * @param _project The project.
   * @param _settings The settings.
   */
  protected void updateSettings (IJavaProject _project, String _settings)
    throws Exception
  {
    String settings = _settings.replace('|', '\n');
    Properties properties = new Properties();
    properties.load(new ByteArrayInputStream(settings.getBytes()));

    boolean updateOptions = false;
    for(Iterator ii = properties.keySet().iterator(); ii.hasNext();){
      String name = (String)ii.next();
      String value = properties.getProperty(name);
      preferences.setOption(_project.getProject(), name, value);
    }
  }

  /**
   * Merges the supplied project's classpath with the specified dependencies.
   *
   * @param _project The project.
   * @param _dependencies The dependencies.
   * @return The classpath entries.
   */
  protected IClasspathEntry[] merge (
      IJavaProject _project, Dependency[] _dependencies)
    throws Exception
  {
    IWorkspaceRoot root = _project.getProject().getWorkspace().getRoot();
    String libraryDir = preferences.getPreference(
        _project.getProject(), libraryRootPreference);

    Collection results = new ArrayList();

    // load the results with all the non library entries.
    IClasspathEntry[] entries = _project.getRawClasspath();
    for(int ii = 0; ii < entries.length; ii++){
      if (entries[ii].getEntryKind() != IClasspathEntry.CPE_LIBRARY &&
          entries[ii].getEntryKind() != IClasspathEntry.CPE_VARIABLE)
      {
        results.add(entries[ii]);
      }
    }

    // merge the dependencies with the classpath entires.
    for(int ii = 0; ii < _dependencies.length; ii ++){
      IClasspathEntry match = null;
      for(int jj = 0; jj < entries.length; jj++){
        if (entries[jj].getEntryKind() == IClasspathEntry.CPE_LIBRARY ||
            entries[jj].getEntryKind() == IClasspathEntry.CPE_VARIABLE)
        {
          String path = entries[jj].getPath().toOSString();
          String pattern = _dependencies[ii].getName() +
            Dependency.VERSION_SEPARATOR;

          // exact match
          if(path.endsWith(_dependencies[ii].toString())){
            match = entries[jj];
            results.add(entries[jj]);
            break;

          // different version match
          }else if(path.indexOf(pattern) != -1){
            break;
          }
        }else if(entries[jj].getEntryKind() == IClasspathEntry.CPE_PROJECT){
          String path = entries[jj].getPath().toOSString();
          if(path.endsWith(_dependencies[ii].getName())){
            match = entries[jj];
            break;
          }
        }
      }

      if(match == null){
        boolean variable = startsWithVariable(libraryDir);
        results.add(createEntry(
              root, _project, _dependencies[ii], libraryDir, variable));
      }else{
        match = null;
      }
    }

    return (IClasspathEntry[])
      results.toArray(new IClasspathEntry[results.size()]);
  }

  /**
   * Creates the classpath entry.
   *
   * @param _root The workspace root.
   * @param _project The project to create the dependency in.
   * @param _dependency The dependency to create the entry for.
   * @param _libraryDir The root library dir to use.
   * @param _variable If the library path indicates a variable entry.
   * @return The classpath entry.
   */
  protected IClasspathEntry createEntry (
      IWorkspaceRoot _root,
      IJavaProject _project,
      Dependency _dependency,
      String _libraryDir,
      boolean _variable)
    throws Exception
  {
    String dependency = _dependency.toString();

    if(_variable){
      return JavaCore.newVariableEntry(
          new Path(_libraryDir).append(dependency), null, null, true);
    }

    IPath dir = new Path(_libraryDir);
    if(!dir.isAbsolute()){
      IPath path = new Path(_project.getElementName())
        .append(dir).append(dependency);
      IResource resource = _root.findMember(path);

      // if the file doesn't exist, create a temp one, grab the resource and
      // then delete the file (handles cases where the jar file is downloaded
      // from a repository).
      if(resource == null){
        IPath fullPath = _project.getResource().getRawLocation()
          .append(_libraryDir);
        File theDir = new File(fullPath.toOSString());
        if(!theDir.exists()){
          throw new IllegalArgumentException(
              Services.getMessage("dir.not.found", fullPath.toOSString()));
        }
        fullPath = fullPath.append(dependency);
        File file = new File(fullPath.toOSString());
        if(file.createNewFile()){
          _root.findMember(new Path(_project.getElementName()).append(dir))
            .refreshLocal(IResource.DEPTH_ONE, null);
          resource = _root.findMember(path);
          file.delete();
        }
      }
      if(resource == null){
        throw new IllegalArgumentException(Services.getMessage(
              "resource.unable.resolve", path.toOSString()));
      }
      path = resource.getFullPath();
      return JavaCore.newLibraryEntry(path, null, null, true);
    }

    IPath path = dir.append(dependency);
    return JavaCore.newLibraryEntry(path, null, null, true);
  }

  /**
   * Determines if the supplied path starts with a variable name.
   *
   * @param _path The path to test.
   * @return True if the path starts with a variable name, false otherwise.
   */
  protected boolean startsWithVariable (String _path)
  {
    String[] variables = JavaCore.getClasspathVariableNames();
    for(int ii = 0; ii < variables.length; ii++){
      if(_path.startsWith(variables[ii])){
        return true;
      }
    }
    return false;
  }

  /**
   * Set libraryRootPreference.
   * <p/>
   * Dependency injection.
   *
   * @param _libraryRootPreference the value to set.
   */
  public void setLibraryRootPreference (String _libraryRootPreference)
  {
    libraryRootPreference = _libraryRootPreference;
  }

// Project delete methods

  /**
   * Set preferences.
   * <p/>
   * Dependency injection.
   *
   * @param _preferences the value to set.
   */
  public void setPreferences (Preferences _preferences)
  {
    this.preferences = _preferences;
  }
}
