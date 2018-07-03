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
package org.eclim.plugin.jdt.command.src;

import java.io.File;
import java.io.FileInputStream;

import java.net.URL;
import java.net.URLClassLoader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.logging.Logger;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.preference.Preferences;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclim.util.IOUtils;
import org.eclim.util.StringUtils;

import org.eclim.util.file.FileUtils;

import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.IPath;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

/**
 * Command which invokes checkstyle on the specified file.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "java_checkstyle",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG"
)
public class CheckstyleCommand
  extends AbstractCommand
{
  private static final Logger logger =
    Logger.getLogger(CheckstyleCommand.class);

  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String name = commandLine.getValue(Options.PROJECT_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);

    IProject project = ProjectUtils.getProject(name, true);
    Preferences prefs = getPreferences();

    String configFile =
      prefs.getValue(project, "org.eclim.java.checkstyle.config");
    String propsFile =
      prefs.getValue(project, "org.eclim.java.checkstyle.properties");

    Properties properties = System.getProperties();
    if (propsFile != null && !propsFile.equals(StringUtils.EMPTY)){
      FileInputStream fis = null;
      try{
        File pfile = new File(propsFile);
        if(!pfile.exists()){
          pfile = new File(ProjectUtils.getFilePath(project, propsFile));
          if(!pfile.exists()){
            throw new RuntimeException(
                Services.getMessage("file.not.found", configFile));
          }
        }
        fis = new FileInputStream(pfile);
        properties = new Properties();
        properties.load(fis);
      }finally{
        IOUtils.closeQuietly(fis);
      }
    }

    if (!new File(configFile).exists()){
      String projectConfigFile = ProjectUtils.getFilePath(project, configFile);
      if (!new File(projectConfigFile).exists()){
        throw new RuntimeException(
            Services.getMessage("file.not.found", configFile));
      }
      configFile = projectConfigFile;
    }

    Configuration config = ConfigurationLoader.loadConfiguration(
        configFile, new PropertiesExpander(properties));
    ClassLoader moduleClassLoader = new ProjectClassLoader(
        JavaUtils.getJavaProject(project),
        Checker.class.getClassLoader());
    Checker checker = new Checker();
    CheckstyleListener listener = new CheckstyleListener();

    try{
      List<File> files = new ArrayList<File>();
      files.add(new File(ProjectUtils.getFilePath(project, file)));

      checker.setModuleClassLoader(moduleClassLoader);
      checker.configure(config);
      checker.addListener(listener);
      checker.process(files);
    }finally{
      checker.destroy();
    }

    return listener.getErrors();
  }

  private static class CheckstyleListener
    implements AuditListener
  {
    private static final String CHECKSTYLE_PREFIX = "[checkstyle] ";

    private ArrayList<Error> errors;

    public CheckstyleListener ()
    {
      errors = new ArrayList<Error>();
    }

    @Override
    public void auditStarted(AuditEvent event)
    {
      // ignore
    }

    @Override
    public void auditFinished(AuditEvent event)
    {
      // ignore
    }

    @Override
    public void fileStarted(AuditEvent event)
    {
      // ignore
    }

    @Override
    public void fileFinished(AuditEvent event)
    {
      // ignore
    }

    @Override
    public void addError(AuditEvent event)
    {
      errors.add(
          new Error(
            CHECKSTYLE_PREFIX + event.getMessage(),
            event.getFileName().replace('\\', '/'),
            event.getLine(),
            event.getColumn(),
            event.getSeverityLevel() != SeverityLevel.ERROR));

    }

    @Override
    public void addException(AuditEvent event, Throwable throwable)
    {
      throw new RuntimeException(throwable);
    }

    /**
     * Gets the errors for this instance.
     *
     * @return The errors.
     */
    public ArrayList<Error> getErrors()
    {
      return this.errors;
    }
  }

  private static class ProjectClassLoader
    extends URLClassLoader
  {
    public ProjectClassLoader (IJavaProject project)
      throws Exception
    {
      super(ProjectClassLoader.classpath(project));
    }

    public ProjectClassLoader (IJavaProject project, ClassLoader parent)
      throws Exception
    {
      super(ProjectClassLoader.classpath(project), parent);
    }

    private static URL[] classpath(IJavaProject project)
      throws Exception
    {
      Set<IJavaProject> visited = new HashSet<IJavaProject>();
      List<URL> urls = new ArrayList<URL>();
      collect(project, urls, visited, true);
      //logger.info(StringUtils.join(urls, "\n"));
      return urls.toArray(new URL[urls.size()]);
    }

    private static void collect(
        IJavaProject javaProject,
        List<URL> urls,
        Set<IJavaProject> visited,
        boolean isFirstProject)
      throws Exception
    {
      if(visited.contains(javaProject)){
        return;
      }
      visited.add(javaProject);

      try{
        IPath out = javaProject.getOutputLocation();
        String path = ProjectUtils.getFilePath(
            javaProject.getProject(), out.toOSString());
        path = FileUtils.addTrailingSlash(path.replace('\\', '/'));
        urls.add(new URL("file://" + path));
      }catch(JavaModelException ignore){
        // ignore... just signals that no output dir was configured.
      }

      IProject project = javaProject.getProject();
      String name = project.getName();

      IClasspathEntry[] entries = null;
      try {
        entries = javaProject.getResolvedClasspath(true);
      }catch(JavaModelException jme){
        // this may or may not be a problem.
        logger.warn(
            "Unable to retreive resolved classpath for project: " + name, jme);
        return;
      }

      for(IClasspathEntry entry : entries) {
        switch (entry.getEntryKind()) {
          case IClasspathEntry.CPE_LIBRARY :
          case IClasspathEntry.CPE_CONTAINER :
          case IClasspathEntry.CPE_VARIABLE :
            String path = entry.getPath().toOSString();
            if(path.startsWith("/" + name + "/")){
              path = ProjectUtils.getFilePath(project, path);
            }
            urls.add(new URL("file://" + path));
            break;
          case IClasspathEntry.CPE_PROJECT :
            if (isFirstProject || entry.isExported()){
              collect(getJavaProject(entry), urls, visited, false);
            }
            break;
        }
      }
    }

    private static IJavaProject getJavaProject(IClasspathEntry entry)
      throws Exception
    {
      IProject project = ProjectUtils.getProject(entry.getPath().segment(0));
      if (project != null){
        return JavaUtils.getJavaProject(project);
      }
      return null;
    }
  }
}
