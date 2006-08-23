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
package org.eclim.plugin.jdt.util;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import org.apache.log4j.Logger;

import org.eclim.Services;

import org.eclim.preference.Preferences;

import org.eclim.util.ProjectUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.Signature;

import org.eclipse.jdt.core.compiler.IProblem;

import org.eclipse.jdt.internal.core.DocumentAdapter;

import org.eclipse.jdt.internal.ui.JavaPlugin;

import org.eclipse.jdt.internal.ui.text.correction.ContributedProcessorDescriptor;

import org.eclipse.jdt.ui.JavaUI;

import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;

import org.eclipse.jface.text.IDocument;

/**
 * Utility methods for working with java files / projects.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class JavaUtils
{
  private static final Logger logger = Logger.getLogger(JavaUtils.class);

  /**
   * Java 1.1 compliance.
   */
  public static final String JAVA_1_1 = JavaCore.VERSION_1_1;

  /**
   * Java 1.2 compliance.
   */
  public static final String JAVA_1_2 = JavaCore.VERSION_1_2;

  /**
   * Java 1.3 compliance.
   */
  public static final String JAVA_1_3 = JavaCore.VERSION_1_3;

  /**
   * Java 1.4 compliance.
   */
  public static final String JAVA_1_4 = JavaCore.VERSION_1_4;

  /**
   * Java 1.5 compliance.
   */
  public static final String JAVA_1_5 = JavaCore.VERSION_1_5;

  /**
   * String version of java.lang package.
   */
  public static final String JAVA_LANG = "java.lang";

  private static ContributedProcessorDescriptor[] correctionProcessors;
  private static ContributedProcessorDescriptor[] assistProcessors;

  /**
   * Gets a java project by name.
   *
   * @param _project The name of the project.
   * @return The project.
   */
  public static IJavaProject getJavaProject (String _project)
    throws Exception
  {
    IProject project = ProjectUtils.getProject(_project, true);
    return getJavaProject(project);
  }

  /**
   * Gets a java project from the supplied IProject.
   *
   * @param _project The IProject.
   * @return The java project.
   */
  public static IJavaProject getJavaProject (IProject _project)
    throws Exception
  {
    if(ProjectUtils.getPath(_project) == null){
      throw new IllegalArgumentException(
          Services.getMessage("project.location.null", _project.getName()));
    }

    IJavaProject javaProject = JavaCore.create(_project);
    if(javaProject == null || !javaProject.exists()){
      throw new IllegalArgumentException(
          Services.getMessage("project.not.found", _project));
    }

    return javaProject;
  }

  /**
   * Finds a compilation unit by looking in all the java project of the supplied
   * name.
   *
   * @param _project The name of the project to locate the file in.
   * @param _file The file to find.
   * @return The compilation unit.
   */
  public static ICompilationUnit getCompilationUnit (String _project, String _file)
    throws Exception
  {
    IPath path = Path.fromOSString(_file);

    IJavaProject javaProject = getJavaProject(_project);
    ICompilationUnit src = getCompilationUnit(javaProject, _file);
    if(src == null){
      throw new IllegalArgumentException(
          Services.getMessage("src.file.not.found", _file));
    }
    return src;
  }

  /**
   * Finds a compilation unit by looking in all the available java projects.
   *
   * @param _file The absolute file path to find.
   * @return The compilation unit.
   *
   * @throws IllegalArgumentException If the file is not found.
   */
  public static ICompilationUnit getCompilationUnit (String _file)
    throws Exception
  {
    IProject[] projects =
      ResourcesPlugin.getWorkspace().getRoot().getProjects();
    for(int ii = 0; ii < projects.length; ii++){
      IJavaProject javaProject = getJavaProject(projects[ii]);

      ICompilationUnit src = getCompilationUnit(javaProject, _file);
      if(src != null){
        return src;
      }
    }
    throw new IllegalArgumentException(
        Services.getMessage("src.file.not.found", _file));
  }

  /**
   * Gets the compilation unit from the supplied project.
   *
   * @param _project The project.
   * @param _file The absolute path to the file.
   * @return The compilation unit or null if not found.
   */
  private static ICompilationUnit getCompilationUnit (
      IJavaProject _project, String _file)
    throws Exception
  {
    _project.open(null);
    _project.getResource().refreshLocal(IResource.DEPTH_INFINITE, null);

    // normalize the paths
    _file = _file.replace('\\', '/');

    String projectPath = ProjectUtils.getPath(_project.getProject());
    projectPath = projectPath.replace('\\', '/');

    // search all src classpath entries.
    IClasspathEntry[] entries = _project.getRawClasspath();
    for(int ii = 0; ii < entries.length; ii++){
      if(entries[ii].getEntryKind() == IClasspathEntry.CPE_SOURCE){
        String entryPath = entries[ii].getPath().toOSString().replace('\\', '/');
        // entry path consists of /project name/path.. strip off project name
        // portion.
        int index = entryPath.indexOf('/', 1);
        if(index != -1){
          entryPath = entryPath.substring(index);
        }else{
          // occurs when src path == "" in .classpath
          entryPath = "";
        }

        String path = projectPath + entryPath;
        if(_file.startsWith(path)){
          String file = _file.substring(path.length() + 1);
          return (ICompilationUnit)_project.findElement(Path.fromOSString(file));
        }
      }
    }

    return null;
  }

  /**
   * Finds a compilation unit by looking in all the java project of the supplied
   * name.
   *
   * @param _project The name of the project to locate the file in.
   * @param _file The src dir relative file path to find.
   * @return The compilation unit or null if not found.
   */
  public static ICompilationUnit findCompilationUnit (
      String _project, String _file)
    throws Exception
  {
    IPath path = Path.fromOSString(_file);

    IJavaProject javaProject = getJavaProject(_project);
    javaProject.open(null);
    javaProject.getResource().refreshLocal(IResource.DEPTH_INFINITE, null);

    ICompilationUnit src = (ICompilationUnit)javaProject.findElement(path);

    return src;
  }

  /**
   * Finds a compilation unit by looking in all the available java projects.
   *
   * @param _file The src directory relative file to find.
   * @return The compilation unit or null if not found.
   */
  public static ICompilationUnit findCompilationUnit (String _file)
    throws Exception
  {
    IPath path = Path.fromOSString(_file);
    IProject[] projects =
      ResourcesPlugin.getWorkspace().getRoot().getProjects();
    for(int ii = 0; ii < projects.length; ii++){
      IJavaProject javaProject = getJavaProject(projects[ii]);
      javaProject.open(null);
      javaProject.getResource().refreshLocal(IResource.DEPTH_INFINITE, null);

      ICompilationUnit src = (ICompilationUnit)javaProject.findElement(path);
      if(src != null){
        return src;
      }
    }
    return null;
  }

  /**
   * Gets the primary element (compilation unit or class file) for the supplied
   * element.
   *
   * @param _element The element.
   * @return The primary element.
   */
  public static IJavaElement getPrimaryElement (IJavaElement _element)
  {
    IJavaElement parent = _element;
    while(parent.getElementType() != IJavaElement.COMPILATION_UNIT &&
        parent.getElementType() != IJavaElement.CLASS_FILE){
      parent = parent.getParent();
    }
    return parent;
  }

  /**
   * Gets the IDocument for the supplied src file.
   * <p/>
   * Code borrowed from org.eclipse.jdt.internal.core.JavaModelOperation.
   *
   * @param _src The src file.
   * @return The IDocument.
   */
  public static IDocument getDocument (ICompilationUnit _src)
    throws Exception
  {
    IBuffer buffer = _src.getBuffer();
    if(buffer instanceof IDocument){
      return (IDocument)buffer;
    }
    return new DocumentAdapter(buffer);
  }

  /**
   * Gets the fully qualified name of the supplied java element.
   * <p/>
   * NOTE: For easy of determining fields and method segments, they are appended
   * with a javadoc style '#' instead of the normal '.'.
   *
   * @param _element The IJavaElement.
   *
   * @return The fully qualified name.
   */
  public static String getFullyQualifiedName (IJavaElement _element)
  {
    IJavaElement parent = _element;
    while(parent.getElementType() != IJavaElement.COMPILATION_UNIT &&
        parent.getElementType() != IJavaElement.CLASS_FILE){
      parent = parent.getParent();
    }

    StringBuffer elementName = new StringBuffer()
      .append(parent.getParent().getElementName())
      .append('.')
      .append(FilenameUtils.getBaseName(parent.getElementName()));

    switch(_element.getElementType()){
      case IJavaElement.FIELD:
        IField field = (IField)_element;
        elementName.append('#').append(field.getElementName());
        break;
      case IJavaElement.METHOD:
        IMethod method = (IMethod)_element;
        elementName.append('#')
          .append(method.getElementName())
          .append('(');
        String[] parameters = method.getParameterTypes();
        for(int ii = 0; ii < parameters.length; ii++){
          if(ii != 0){
            elementName.append(", ");
          }
          elementName.append(
              Signature.toString(parameters[ii]).replace('/', '.'));
        }
        elementName.append(')');
        break;
    }

    return elementName.toString();
  }

  /**
   * Constructs a compilation unit relative name for the supplied type.
   * <p/>
   * If the type is imported, in java.lang, or in the same package as the source
   * file, then the type name returned is unqualified, otherwise the name
   * returned is the fully qualified type name.
   *
   * @param _src The compilation unit.
   * @param _type The type.
   *
   * @return The relative type name.
   */
  public static String getCompilationUnitRelativeTypeName (
      ICompilationUnit _src, IType _type)
    throws Exception
  {
    String typeName = _type.getFullyQualifiedName().replace('$', '.');
    if(JavaUtils.containsImport(_src, _type)){
      typeName = _type.getElementName();

      int parentType = _type.getParent().getElementType();
      if (parentType == IJavaElement.TYPE){
        typeName = _type.getParent().getElementName() + '.' + typeName;
      }else if (parentType == IJavaElement.CLASS_FILE){
        String parentName = _type.getParent().getElementName();
        int index = parentName.indexOf('$');
        if (index != -1){
          parentName = parentName.substring(0, index);
          typeName = parentName + '.' + typeName;
        }
      }
    }else{
      typeName = _type.getFullyQualifiedName().replace('$', '.');
    }

    return typeName;
  }

  /**
   * Determines if the supplied src file contains an import that for the
   * supplied type (including wildcard .* imports).
   *
   * @param _src The compilation unit.
   * @param _type The type.
   * @return true if the src file has a qualifying import.
   */
  public static boolean containsImport (ICompilationUnit _src, String _type)
    throws Exception
  {
    return containsImport(_src, _src.getType(_type));
  }

  /**
   * Determines if the supplied src file contains an import that for the
   * supplied type (including wildcard .* imports).
   *
   * @param _src The compilation unit.
   * @param _type The type.
   * @return true if the src file has a qualifying import.
   */
  public static boolean containsImport (ICompilationUnit _src, IType _type)
    throws Exception
  {
    String typePkg = _type.getPackageFragment().getElementName();

    IPackageDeclaration[] packages = _src.getPackageDeclarations();
    String pkg = packages.length > 0 ? packages[0].getElementName() : null;

    // classes in same package are auto imported.
    if( (pkg == null && typePkg == null) ||
        (pkg != null && pkg.equals(typePkg)))
    {
      return true;
    }

    // java.lang is auto imported.
    if(JAVA_LANG.equals(typePkg)){
      return true;
    }

    typePkg = typePkg + ".*";
    String typeName = _type.getFullyQualifiedName().replace('$', '.');

    IImportDeclaration[] imports = _src.getImports();
    for (int ii = 0; ii < imports.length; ii++){
      String name = imports[ii].getElementName();
      if(name.equals(typeName) || name.equals(typePkg)){
        return true;
      }
    }
    return false;
  }

  /**
   * Gets the java version for which all source is to be compatable with.
   *
   * @param _project The java project.
   * @return The source compliance version.
   */
  public static String getCompilerSourceCompliance (IJavaProject _project)
  {
    return (String)_project.getOptions(true).get(JavaCore.COMPILER_SOURCE);
  }

  /**
   * Sets the java version for which all source is to be compatable with.
   *
   * @param _version The java version.
   */
  public static void setCompilerSourceCompliance (String _version)
    throws Exception
  {
    Map options = JavaCore.getOptions();
    options.put(JavaCore.COMPILER_SOURCE, _version);
    options.put(JavaCore.COMPILER_COMPLIANCE, _version);
    options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, _version);

    JavaCore.setOptions((Hashtable)options);
  }

  /**
   * Sets the java version for which all source is to be compatable with.
   *
   * @param _project The java project.
   * @param _version The java version.
   */
  public static void setCompilerSourceCompliance (
      IJavaProject _project, String _version)
    throws Exception
  {
    // using _project.setOption(String,String) doesn't save the setting.
    Map options = _project.getOptions(false);
    options.put(JavaCore.COMPILER_SOURCE, _version);
    options.put(JavaCore.COMPILER_COMPLIANCE, _version);
    options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, _version);

    _project.setOptions(options);
  }

  /**
   * Loads the supplied map to be used in a template with the available
   * preferences.
   *
   * @param _project The current project.
   * @param _preferences The eclim preferences.
   * @param _values The values to populate.
   */
  public static void loadPreferencesForTemplate (
      IProject _project, Preferences _preferences, Map _values)
    throws Exception
  {
    Map options = _preferences.getOptionsAsMap(_project);
    for(Iterator ii = options.keySet().iterator(); ii.hasNext();){
      String key = (String)ii.next();
      String value = (String)options.get(key);
      _values.put(key.replace('.', '_'), value);
    }
  }

  /**
   * Gets the problems for a given src file.
   *
   * @param _src The src file.
   * @return The problems.
   */
  public static IProblem[] getProblems (ICompilationUnit _src)
    throws Exception
  {
    return getProblems(_src, null);
  }

  /**
   * Gets the problems for a given src file.
   *
   * @param _src The src file.
   * @param _ids Array of problem ids to accept.
   * @return The problems.
   */
  public static IProblem[] getProblems (ICompilationUnit _src, int[] _ids)
    throws Exception
  {
    ProblemRequestor requestor = new ProblemRequestor(_ids);
    try{
      _src.becomeWorkingCopy(requestor, null);
    }finally{
      _src.discardWorkingCopy();
    }
    List problems = requestor.getProblems();
    return (IProblem[])problems.toArray(new IProblem[problems.size()]);
  }

  /**
   * Gets array of IQuickFixProcessor(s).
   * <p/>
   * Based on
   * org.eclipse.jdt.internal.ui.text.correction.JavaCorrectionProcessor#getCorrectionProcessors()
   *
   * @param _src The src file to get processors for.
   * @return quick fix processors.
   */
  public static IQuickFixProcessor[] getQuickFixProcessors (ICompilationUnit _src)
    throws Exception
  {
    if (correctionProcessors == null) {
      correctionProcessors = getProcessorDescriptors("quickFixProcessors", true);
    }
    IQuickFixProcessor[] processors =
      new IQuickFixProcessor[correctionProcessors.length];
    for(int ii = 0; ii < correctionProcessors.length; ii++){
      processors[ii] = (IQuickFixProcessor)
        correctionProcessors[ii].getProcessor(_src);
    }
    return processors;
  }

  /**
   * Gets array of IQuickAssistProcessor(s).
   * <p/>
   * Based on
   * org.eclipse.jdt.internal.ui.text.correction.JavaCorrectionProcessor#getAssistProcessors()
   *
   * @param _src The src file to get processors for.
   * @return quick assist processors.
   */
  public static IQuickAssistProcessor[] getQuickAssistProcessors (ICompilationUnit _src)
    throws Exception
  {
    if (assistProcessors == null) {
      assistProcessors = getProcessorDescriptors("quickAssistProcessors", false);
    }
    IQuickAssistProcessor[] processors =
      new IQuickAssistProcessor[assistProcessors.length];
    for(int ii = 0; ii < assistProcessors.length; ii++){
      processors[ii] = (IQuickAssistProcessor)
        assistProcessors[ii].getProcessor(_src);
    }
    return processors;
  }

  private static ContributedProcessorDescriptor[] getProcessorDescriptors (
      String _id, boolean _testMarkerTypes)
    throws Exception
  {
    IConfigurationElement[] elements = Platform.getExtensionRegistry()
      .getConfigurationElementsFor(JavaUI.ID_PLUGIN, _id);
    ArrayList res= new ArrayList(elements.length);

    for(int ii = 0; ii < elements.length; ii++){
      ContributedProcessorDescriptor desc =
        new ContributedProcessorDescriptor(elements[ii], _testMarkerTypes);
      IStatus status= desc.checkSyntax();
      if(status.isOK()){
        res.add(desc);
      }else{
        JavaPlugin.log(status);
      }
    }
    return (ContributedProcessorDescriptor[])
      res.toArray(new ContributedProcessorDescriptor[res.size()]);
  }

  /**
   * Gathers problems as a src file is processed.
   */
  public static class ProblemRequestor
    implements org.eclipse.jdt.core.IProblemRequestor
  {
    private List problems = new ArrayList();
    private int[] ids;

    /**
     * Constructs a new instance.
     *
     * @param _ids Array of problem ids to accept.
     */
    public ProblemRequestor (int[] _ids)
    {
      ids = _ids;
    }

    /**
     * Gets a list of problems recorded.
     *
     * @return The list of problems.
     */
    public List getProblems ()
    {
      return problems;
    }

    /**
     * {@inheritDoc}
     */
    public void acceptProblem (IProblem _problem)
    {
      if(ids != null){
        for (int ii = 0; ii < ids.length; ii++){
          if(_problem.getID() == ids[ii]){
            problems.add(_problem);
            break;
          }
        }
      }else{
        problems.add(_problem);
      }
    }

    /**
     * {@inheritDoc}
     */
    public void beginReporting(){}

    /**
     * {@inheritDoc}
     */
    public void endReporting(){}

    /**
     * {@inheritDoc}
     */
    public boolean isActive()
    {
      return true;
    }
  }
}
