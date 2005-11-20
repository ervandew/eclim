/**
 * Copyright (c) 2004 - 2005
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
package org.eclim.command.project;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import org.eclim.Services;

import org.eclim.client.Options;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Error;

import org.eclim.command.java.JavaUtils;

import org.eclim.command.project.classpath.Parser;
import org.eclim.command.project.classpath.Dependency;

import org.eclim.util.XmlUtils;

import org.eclim.util.file.FileUtils;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaConventions;

/**
 * Command to update a project.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ProjectUpdateCommand
  extends AbstractCommand
{
  private static final Log log = LogFactory.getLog(ProjectUpdateCommand.class);

  private static final Pattern STATUS_PATTERN = Pattern.compile(".*/(.*)'.*");

  private String libraryRootPreference;

  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
    throws IOException
  {
    try{
      String projectName = _commandLine.getValue(Options.NAME_OPTION);
      String buildfile = _commandLine.getValue(Options.BUILD_FILE_OPTION);
      String settings = _commandLine.getValue(Options.SETTINGS_OPTION);

      IJavaProject javaProject = JavaUtils.getJavaProject(projectName);

      // project settings update
      if(settings != null){
        updateSettings(javaProject, settings);
        javaProject.makeConsistent(null);
      }else{
        // validate that .classpath xml is well formed and valid.
        String dotclasspath = javaProject.getProject().getFile(".classpath")
          .getRawLocation().toOSString();
        Error[] errors = XmlUtils.validateXml(dotclasspath,
            System.getProperty("eclim.home") + "/schema/eclipse/classpath.xsd");
        if(errors.length > 0){
          return filter(_commandLine, errors);
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
          return filter(_commandLine, errors);
        }
      }

      return Services.getMessage("project.updated", projectName);
    }catch(Throwable t){
      return t;
    }
  }

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

    if(status.isOK() && errors.isEmpty()){
      _javaProject.setRawClasspath(_entries, null);
      _javaProject.makeConsistent(null);
    }

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
    matcher = Pattern.compile(pattern).matcher(_contents);
    if(matcher.find()){
      int[] position = FileUtils.offsetToLineColumn(_filename, matcher.start());
      line = position[0];
      col = position[1];
    }

    return new Error(_status.getMessage(), _filename, line, col);
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
      getEclimPreferences().setOption(_project.getProject(), name, value);
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
    String libraryDir = getEclimPreferences().getPreference(
        _project.getProject(), libraryRootPreference, null);

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
    this.libraryRootPreference = _libraryRootPreference;
  }
}
