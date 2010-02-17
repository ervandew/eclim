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
package org.eclim.plugin.jdt.command.src;

import java.io.IOException;
import java.io.OutputStream;

import java.util.ArrayList;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import org.apache.tools.ant.taskdefs.ExecuteStreamHandler;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.taskdefs.PumpStreamHandler;
import org.apache.tools.ant.taskdefs.Redirector;

import org.apache.tools.ant.types.Commandline.Argument;
import org.apache.tools.ant.types.Path;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.plugin.jdt.util.ClasspathUtils;
import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclim.util.StringUtils;

import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.IPath;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageDeclaration;

import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;

/**
 * Command to run the project's main class.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "java",
  options =
    "REQUIRED p project ARG," +
    "OPTIONAL c classname ARG," +
    "OPTIONAL a args ANY"
)
public class JavaCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   * @see org.eclim.command.Command#execute(CommandLine)
   */
  public String execute(CommandLine commandLine)
    throws Exception
  {
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    String mainClass = commandLine.getValue(Options.CLASSNAME_OPTION);
    IProject project = ProjectUtils.getProject(projectName);
    IJavaProject javaProject = JavaUtils.getJavaProject(project);

    Project antProject = new Project();
    BuildLogger buildLogger = new DefaultLogger();
    buildLogger.setMessageOutputLevel(Project.MSG_INFO);
    buildLogger.setOutputPrintStream(System.out);
    buildLogger.setErrorPrintStream(System.err);
    antProject.addBuildListener(buildLogger);
    antProject.setBasedir(ProjectUtils.getPath(project));
    antProject.setDefaultInputStream(System.in);

    if (mainClass == null){
      mainClass =
        ProjectUtils.getSetting(project, "org.eclim.java.run.mainclass");
    }

    if (mainClass == null ||
        mainClass.trim().equals(StringUtils.EMPTY) ||
        mainClass.trim().equals("none"))
    {
      // first try to locate a main method.
      mainClass = findMainClass(javaProject);
      if (mainClass == null){
        throw new RuntimeException(Services.getMessage(
              "setting.not.set", "org.eclim.java.run.mainclass"));
      }
    }

    if (mainClass.endsWith(".java") || mainClass.endsWith(".class")){
      mainClass = mainClass.substring(0, mainClass.lastIndexOf('.'));
    }

    Java java = new MyJava();
    java.setTaskName("java");
    java.setProject(antProject);
    java.setClassname(mainClass);
    java.setFork(true);

    // construct classpath
    Path classpath = new Path(antProject);
    String[] paths = ClasspathUtils.getClasspath(javaProject);
    for (String path : paths){
      Path.PathElement pe = classpath.createPathElement();
      pe.setPath(path);
    }

    java.setClasspath(classpath);

    // add any supplied command line args
    String[] args = commandLine.getValues(Options.ARGS_OPTION);
    if (args != null && args.length > 0){
      for(String arg : args){
        Argument a = java.createArg();
        a.setValue(arg);
      }
    }

    java.execute();

    return StringUtils.EMPTY;
  }

  private String findMainClass(IJavaProject javaProject)
    throws Exception
  {
    final String projectPath = ProjectUtils.getPath(javaProject.getProject());
    final ArrayList<IMethod> methods = new ArrayList<IMethod>();
    int context = IJavaSearchConstants.DECLARATIONS;
    int type = IJavaSearchConstants.METHOD;
    int matchType = SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE;
    IJavaSearchScope scope =
      SearchEngine.createJavaSearchScope(new IJavaElement[]{javaProject});
    SearchPattern pattern =
      SearchPattern.createPattern("main(String[])", type, context, matchType);
    SearchRequestor requestor = new SearchRequestor(){
      public void acceptSearchMatch(SearchMatch match){
        if(match.getAccuracy() != SearchMatch.A_ACCURATE){
          return;
        }

        IPath location = match.getResource().getRawLocation();
        if (location == null){
          return;
        }

        String path = location.toOSString().replace('\\', '/');
        if (!path.toLowerCase().startsWith(projectPath.toLowerCase())){
          return;
        }

        IJavaElement element = (IJavaElement)match.getElement();
        if (element.getElementType() != IJavaElement.METHOD){
          return;
        }

        IMethod method = (IMethod)element;
        String[] params = method.getParameterTypes();
        if (params.length != 1){
          return;
        }
        methods.add(method);
      }
    };

    if(pattern != null){
      SearchEngine engine = new SearchEngine();
      SearchParticipant[] participants = new SearchParticipant[]{
        SearchEngine.getDefaultSearchParticipant()};
      engine.search(pattern, participants, scope, requestor, null);

      // if we found only 1 result, we can use it.
      if (methods.size() == 1){
        IMethod method = methods.get(0);
        ICompilationUnit cu = method.getCompilationUnit();
        IPackageDeclaration[] packages = cu.getPackageDeclarations();
        if (packages != null && packages.length > 0){
          return packages[0].getElementName() + "." + cu.getElementName();
        }
        return cu.getElementName();
      }
    }
    return null;
  }

  /* All of this is to ensure that System.out calls by the running class are
   * flushed immediatly to the console for things like user input prompts. */

  private class MyJava
    extends Java
  {
    public MyJava()
    {
      super();
      this.redirector = new MyRedirector(this);
    }
  }

  private class MyRedirector
    extends Redirector
  {
    public MyRedirector(Task task)
    {
      super(task);
    }

    @Override
    public synchronized ExecuteStreamHandler createHandler()
      throws BuildException
    {
      return new MyPumpStreamHandler();
    }

    @Override
    public synchronized void complete() throws IOException {
      System.out.flush();
      System.err.flush();
    }
  }

  private class MyPumpStreamHandler
    extends PumpStreamHandler
  {
    public MyPumpStreamHandler()
    {
      super(new FlushingOutputStream(System.out), System.err, System.in);
    }
  }

  private class FlushingOutputStream
    extends OutputStream
  {
    private OutputStream out;

    public FlushingOutputStream(OutputStream out)
    {
      this.out = out;
    }

    @Override
    public void write(int b)
      throws IOException
    {
      out.write(b);
      out.flush();
    }

    @Override
    public void write(byte[] b)
      throws IOException
    {
      out.write(b);
      out.flush();
    }

    @Override
    public void write(byte[] b, int off, int len)
      throws IOException
    {
      out.write(b, off, len);
      out.flush();
    }
  }
}
