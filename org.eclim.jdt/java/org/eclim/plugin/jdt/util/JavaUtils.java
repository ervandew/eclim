/**
 * Copyright (C) 2005 - 2014  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;

import org.eclim.Services;

import org.eclim.plugin.core.preference.Preferences;

import org.eclim.plugin.core.project.ProjectNatureFactory;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.plugin.jdt.PluginResources;

import org.eclim.util.IOUtils;

import org.eclim.util.file.FileUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClassFile;
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

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Javadoc;

import org.eclipse.jdt.core.formatter.IndentManipulation;

import org.eclipse.jdt.internal.core.DocumentAdapter;

import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;

import org.eclipse.jdt.internal.corext.refactoring.structure.ASTNodeSearchUtil;

import org.eclipse.jdt.internal.formatter.DefaultCodeFormatter;

import org.eclipse.jdt.internal.ui.JavaPlugin;

import org.eclipse.jdt.internal.ui.text.correction.ContributedProcessorDescriptor;

import org.eclipse.jdt.ui.JavaUI;

import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import org.eclipse.text.edits.TextEdit;

/**
 * Utility methods for working with java files / projects.
 *
 * @author Eric Van Dewoestine
 */
public class JavaUtils
{
  /**
   * String version of java.lang package.
   */
  public static final String JAVA_LANG = "java.lang";

  private static final Pattern PACKAGE_LINE =
    Pattern.compile("^\\s*package\\s+(\\w+(\\.\\w+){0,})\\s*;\\s*$");

  private static final Pattern TRAILING_WHITESPACE =
    Pattern.compile("[ \t]+$", Pattern.MULTILINE);

  private static ContributedProcessorDescriptor[] correctionProcessors;
  private static ContributedProcessorDescriptor[] assistProcessors;

  /**
   * Gets a java project by name.
   *
   * @param project The name of the project.
   * @return The project.
   */
  public static IJavaProject getJavaProject(String project)
    throws Exception
  {
    return getJavaProject(ProjectUtils.getProject(project, true));
  }

  /**
   * Gets a java project from the supplied IProject.
   *
   * @param project The IProject.
   * @return The java project.
   */
  public static IJavaProject getJavaProject(IProject project)
    throws Exception
  {
    if(ProjectUtils.getPath(project) == null){
      throw new IllegalArgumentException(
          Services.getMessage("project.location.null", project.getName()));
    }

    if(!project.hasNature(PluginResources.NATURE)){
      throw new IllegalArgumentException(Services.getMessage(
            "project.missing.nature",
            project.getName(),
            ProjectNatureFactory.getAliasForNature(PluginResources.NATURE)));
    }

    IJavaProject javaProject = JavaCore.create(project);
    if(javaProject == null || !javaProject.exists()){
      throw new IllegalArgumentException(
          Services.getMessage("project.not.found", project));
    }

    return javaProject;
  }

  /**
   * Finds a compilation unit by looking in the java project of the supplied
   * name.
   *
   * @param project The name of the project to locate the file in.
   * @param file The file to find.
   * @return The compilation unit.
   *
   * @throws IllegalArgumentException If the file is not found.
   */
  public static ICompilationUnit getCompilationUnit(String project, String file)
    throws Exception
  {
    IJavaProject javaProject = getJavaProject(project);
    return getCompilationUnit(javaProject, file);
  }

  /**
   * Finds a compilation unit by looking in all the available java projects.
   *
   * @param file The absolute file path to find.
   * @return The compilation unit.
   *
   * @throws IllegalArgumentException If the file is not found.
   */
  public static ICompilationUnit getCompilationUnit(String file)
    throws Exception
  {
    IProject[] projects =
      ResourcesPlugin.getWorkspace().getRoot().getProjects();
    for(int ii = 0; ii < projects.length; ii++){
      IJavaProject javaProject = getJavaProject(projects[ii]);

      ICompilationUnit src = JavaCore.createCompilationUnitFrom(
          ProjectUtils.getFile(javaProject.getProject(), file));
      if(src != null && src.exists()){
        return src;
      }
    }
    throw new IllegalArgumentException(
        Services.getMessage("src.file.not.found", file, ".classpath"));
  }

  /**
   * Gets the compilation unit from the supplied project.
   *
   * @param project The project.
   * @param file The absolute path to the file.
   * @return The compilation unit or null if not found.
   *
   * @throws IllegalArgumentException If the file is not found.
   */
  public static ICompilationUnit getCompilationUnit(
      IJavaProject project, String file)
    throws Exception
  {
    ICompilationUnit src = JavaCore.createCompilationUnitFrom(
        ProjectUtils.getFile(project.getProject(), file));
    if(src == null || !src.exists()){
      throw new IllegalArgumentException(
          Services.getMessage("src.file.not.found", file, ".classpath"));
    }
    return src;
  }

  /**
   * Finds a compilation unit by looking in all the java project of the supplied
   * name.
   *
   * @param project The name of the project to locate the file in.
   * @param file The src dir relative file path to find.
   * @return The compilation unit or null if not found.
   */
  public static ICompilationUnit findCompilationUnit(
      String project, String file)
    throws Exception
  {
    IPath path = Path.fromOSString(file);

    IJavaProject javaProject = getJavaProject(project);
    javaProject.open(null);
    //javaProject.getResource().refreshLocal(IResource.DEPTH_INFINITE, null);

    ICompilationUnit src = (ICompilationUnit)javaProject.findElement(path);

    return src;
  }

  /**
   * Finds a compilation unit by looking in all the available java projects.
   *
   * @param file The src directory relative file to find.
   * @return The compilation unit or null if not found.
   */
  public static ICompilationUnit findCompilationUnit(String file)
    throws Exception
  {
    IPath path = Path.fromOSString(file);
    IProject[] projects =
      ResourcesPlugin.getWorkspace().getRoot().getProjects();
    for(IProject project : projects){
      if (project.hasNature(JavaCore.NATURE_ID)){
        IJavaProject javaProject = getJavaProject(project);
        javaProject.open(null);
        //javaProject.getResource().refreshLocal(IResource.DEPTH_INFINITE, null);

        ICompilationUnit src = (ICompilationUnit)javaProject.findElement(path);
        if(src != null){
          return src;
        }
      }
    }
    return null;
  }

  /**
   * Attempts to locate the IClassFile for the supplied file path from the
   * specified project's classpath.
   *
   * @param project The project to find the class file in.
   * @param path Absolute path or url (jar:, zip:) to a .java source file.
   * @return The IClassFile.
   */
  public static IClassFile findClassFile(IJavaProject project, String path)
    throws Exception
  {
    if(path.startsWith("/") ||
        path.toLowerCase().startsWith("jar:") ||
        path.toLowerCase().startsWith("zip:"))
    {
      FileSystemManager fsManager = VFS.getManager();
      FileObject file = fsManager.resolveFile(path.replace("%", "%25"));
      if(file.exists()){
        BufferedReader in = null;
        try{
          in = new BufferedReader(
              new InputStreamReader(file.getContent().getInputStream()));
          String pack = null;
          String line = null;
          while((line = in.readLine()) != null){
            Matcher matcher = PACKAGE_LINE.matcher(line);
            if (matcher.matches()){
              pack = matcher.group(1);
              break;
            }
          }
          if (pack != null){
            String name = pack + '.' +
              FileUtils.getFileName(file.getName().getPath());
            IType type = project.findType(name);
            if (type != null){
              return type.getClassFile();
            }
          }
        }finally{
          IOUtils.closeQuietly(in);
        }
      }
    }
    return null;
  }

  /**
   * Gets the primary element (compilation unit or class file) for the supplied
   * element.
   *
   * @param element The element.
   * @return The primary element.
   */
  public static IJavaElement getPrimaryElement(IJavaElement element)
  {
    IJavaElement parent = element;
    while(parent.getElementType() != IJavaElement.COMPILATION_UNIT &&
        parent.getElementType() != IJavaElement.CLASS_FILE)
    {
      parent = parent.getParent();
    }
    return parent;
  }

  /**
   * Get the offset of the supplied element within the source.
   *
   * @param element The element
   * @return The offset or -1 if it could not be determined.
   */
  public static int getElementOffset(IJavaElement element)
    throws Exception
  {
    IJavaElement parent = getPrimaryElement(element);
    CompilationUnit cu = null;
    switch(parent.getElementType()){
      case IJavaElement.COMPILATION_UNIT:
        cu = ASTUtils.getCompilationUnit((ICompilationUnit)parent);
        break;
      case IJavaElement.CLASS_FILE:
        try{
          cu = ASTUtils.getCompilationUnit((IClassFile)parent);
        }catch(IllegalStateException ise){
          // no source attachement
        }
        break;
    }

    if (cu != null) {
      ASTNode[] nodes = ASTNodeSearchUtil.getDeclarationNodes(element, cu);
      if (nodes != null && nodes.length > 0){
        int offset = nodes[0].getStartPosition();
        if (nodes[0] instanceof BodyDeclaration){
          Javadoc docs = ((BodyDeclaration)nodes[0]).getJavadoc();
          if (docs != null){
            offset += docs.getLength() + 1;
          }
        }
        return offset;
      }
    }
    return -1;
  }

  /**
   * Gets the IDocument for the supplied src file.
   * <p/>
   * Code borrowed from org.eclipse.jdt.internal.core.JavaModelOperation.
   *
   * @param src The src file.
   * @return The IDocument.
   */
  public static IDocument getDocument(ICompilationUnit src)
    throws Exception
  {
    IBuffer buffer = src.getBuffer();
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
   * @param element The IJavaElement.
   *
   * @return The fully qualified name.
   */
  public static String getFullyQualifiedName(IJavaElement element)
  {
    IJavaElement parent = element;
    while(parent.getElementType() != IJavaElement.COMPILATION_UNIT &&
        parent.getElementType() != IJavaElement.CLASS_FILE)
    {
      parent = parent.getParent();
    }

    StringBuffer elementName = new StringBuffer()
      .append(parent.getParent().getElementName())
      .append('.')
      .append(FileUtils.getFileName(parent.getElementName()));

    switch(element.getElementType()){
      case IJavaElement.FIELD:
        IField field = (IField)element;
        elementName.append('#').append(field.getElementName());
        break;
      case IJavaElement.METHOD:
        IMethod method = (IMethod)element;
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
   * @param src The compilation unit.
   * @param type The type.
   *
   * @return The relative type name.
   */
  public static String getCompilationUnitRelativeTypeName(
      ICompilationUnit src, IType type)
    throws Exception
  {
    String typeName = type.getFullyQualifiedName().replace('$', '.');
    if(JavaUtils.containsImport(src, type)){
      typeName = type.getElementName();

      int parentType = type.getParent().getElementType();
      if (parentType == IJavaElement.TYPE){
        typeName = type.getParent().getElementName() + '.' + typeName;
      }else if (parentType == IJavaElement.CLASS_FILE){
        String parentName = type.getParent().getElementName();
        int index = parentName.indexOf('$');
        if (index != -1){
          parentName = parentName.substring(0, index);
          typeName = parentName + '.' + typeName;
        }
      }
    }else{
      typeName = type.getFullyQualifiedName().replace('$', '.');
    }

    return typeName;
  }

  /**
   * Determines if the supplied src file contains an import for the
   * supplied type (including wildcard .* imports).
   *
   * @param src The compilation unit.
   * @param type The type.
   * @return true if the src file has a qualifying import.
   */
  public static boolean containsImport(ICompilationUnit src, String type)
    throws Exception
  {
    return containsImport(src, src.getType(type));
  }

  /**
   * Determines if the supplied src file contains an import for the
   * supplied type (including wildcard .* imports).
   *
   * @param src The compilation unit.
   * @param type The type.
   * @return true if the src file has a qualifying import.
   */
  public static boolean containsImport(ICompilationUnit src, IType type)
    throws Exception
  {
    String typePkg = type.getPackageFragment().getElementName();

    IPackageDeclaration[] packages = src.getPackageDeclarations();
    String pkg = packages.length > 0 ? packages[0].getElementName() : null;

    // classes in same package are auto imported.
    if ((pkg == null && typePkg == null) ||
        (pkg != null && pkg.equals(typePkg)))
    {
      return true;
    }

    // java.lang is auto imported.
    if(JAVA_LANG.equals(typePkg)){
      return true;
    }

    typePkg = typePkg + ".*";
    String typeName = type.getFullyQualifiedName().replace('$', '.');

    IImportDeclaration[] imports = src.getImports();
    for (int ii = 0; ii < imports.length; ii++){
      String name = imports[ii].getElementName();
      if(name.equals(typeName) || name.equals(typePkg)){
        return true;
      }
    }
    return false;
  }

  /**
   * Format a region in the supplied source file.
   *
   * @param src The ICompilationUnit.
   * @param kind The kind of code snippet to format.
   * @param offset The starting offset of the region to format.
   * @param length The length of the region to format.
   */
  public static void format(ICompilationUnit src, int kind, int offset, int length)
    throws Exception
  {
    IBuffer buffer = src.getBuffer();
    String contents = buffer.getContents();
    String delimiter = StubUtility.getLineDelimiterUsed(src);
    DefaultCodeFormatter formatter =
      new DefaultCodeFormatter(src.getJavaProject().getOptions(true));

    // when the eclipse indent settings differ from vim (tabs vs spaces) then
    // the inserted method's indent may be a bit off. this is a workaround to
    // force reformatting of the code from the start of the line to the start of
    // the next set of code following the new method. Doesn't quite fix indent
    // formatting of methods in nested classes.
    while (offset > 0 &&
        !IndentManipulation.isLineDelimiterChar(buffer.getChar(offset - 1)))
    {
      offset--;
      length++;
    }
    while ((offset + length) < contents.length() &&
        IndentManipulation.isLineDelimiterChar(buffer.getChar(offset + length)))
    {
      length++;
    }

    TextEdit edits = formatter.format(kind, contents, offset, length, 0, delimiter);
    if (edits != null) {
      int oldLength = contents.length();
      Document document = new Document(contents);
      edits.apply(document);

      String formatted = document.get();

      // jdt formatter can introduce trailing whitespace (javadoc comments), so
      // we'll remove all trailing whitespace from the formatted section (unless
      // the user has configured eclim not to do so).
      length += formatted.length() - oldLength;
      if (offset < (offset + length)){
        String stripTrailingWhitespace = Preferences.getInstance().getValue(
          src.getJavaProject().getProject(),
          "org.eclim.java.format.strip_trialing_whitespace");
        if ("true".equals(stripTrailingWhitespace)){
          String pre = formatted.substring(0, offset);
          StringBuffer section = new StringBuffer(
              formatted.substring(offset, offset + length));
          StringBuffer post = new StringBuffer(
              formatted.substring(offset + length));
          // account for section not ending at a line delimiter
          while (!section.toString().endsWith(delimiter) && post.length() > 0){
            section.append(post.charAt(0));
            post.deleteCharAt(0);
          }

          Matcher matcher = TRAILING_WHITESPACE.matcher(section);
          String stripped = matcher.replaceAll(StringUtils.EMPTY);

          src.getBuffer().setContents(pre + stripped + post);
        }else{
          src.getBuffer().setContents(formatted);
        }
      }else{
        src.getBuffer().setContents(formatted);
      }

      if (src.isWorkingCopy()) {
        src.commitWorkingCopy(true, null);
      }
      src.save(null, false);
    }
  }

  /**
   * Gets the java version for which all source is to be compatable with.
   *
   * @param project The java project.
   * @return The source compliance version.
   */
  public static String getCompilerSourceCompliance(IJavaProject project)
  {
    return (String)project.getOptions(true).get(JavaCore.COMPILER_SOURCE);
  }

  /**
   * Sets the java version for which all source is to be compatable with.
   *
   * @param version The java version.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static void setCompilerSourceCompliance(String version)
    throws Exception
  {
    Map<String, String> options = JavaCore.getOptions();
    options.put(JavaCore.COMPILER_SOURCE, version);
    options.put(JavaCore.COMPILER_COMPLIANCE, version);
    options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, version);

    JavaCore.setOptions((Hashtable)options);
  }

  /**
   * Sets the java version for which all source is to be compatable with.
   *
   * @param project The java project.
   * @param version The java version.
   */
  @SuppressWarnings("unchecked")
  public static void setCompilerSourceCompliance(
      IJavaProject project, String version)
    throws Exception
  {
    // using project.setOption(String,String) doesn't save the setting.
    Map<String, String> options = project.getOptions(false);
    options.put(JavaCore.COMPILER_SOURCE, version);
    options.put(JavaCore.COMPILER_COMPLIANCE, version);
    options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, version);

    project.setOptions(options);
  }

  /**
   * Enables a visibility check for code assist.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static void enableVisibilityCheck()
  {
    Map<String, String> options = JavaCore.getOptions();
    options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);

    JavaCore.setOptions((Hashtable)options);
  }

  /**
   * Loads the supplied map to be used in a template with the available
   * preferences.
   *
   * @param project The current project.
   * @param preferences The eclim preferences.
   * @param values The values to populate.
   */
  public static void loadPreferencesForTemplate(
      IProject project, Preferences preferences, Map<String, Object> values)
    throws Exception
  {
    Map<String, String> options = preferences.getValues(project);
    for(String key : options.keySet()){
      String value = options.get(key);
      values.put(key.replace('.', '_'), value);
    }
  }

  /**
   * Gets the problems for a given src file.
   *
   * @param src The src file.
   * @return The problems.
   */
  public static IProblem[] getProblems(ICompilationUnit src)
    throws Exception
  {
    return getProblems(src, null);
  }

  /**
   * Gets the problems for a given src file.
   *
   * @param src The src file.
   * @param ids Array of problem ids to accept.
   * @return The problems.
   */
  public static IProblem[] getProblems(ICompilationUnit src, int[] ids)
    throws Exception
  {
    ICompilationUnit workingCopy = src.getWorkingCopy(null);

    ProblemRequestor requestor = new ProblemRequestor(ids);
    try{
      workingCopy.discardWorkingCopy();
      workingCopy.becomeWorkingCopy(requestor, null);
    }finally{
      workingCopy.discardWorkingCopy();
    }
    List<IProblem> problems = requestor.getProblems();
    return (IProblem[])problems.toArray(new IProblem[problems.size()]);
  }

  /**
   * Gets array of IQuickFixProcessor(s).
   * <p/>
   * Based on
   * org.eclipse.jdt.internal.ui.text.correction.JavaCorrectionProcessor#getCorrectionProcessors()
   *
   * @param src The src file to get processors for.
   * @return quick fix processors.
   */
  public static IQuickFixProcessor[] getQuickFixProcessors(ICompilationUnit src)
    throws Exception
  {
    if (correctionProcessors == null) {
      correctionProcessors = getProcessorDescriptors("quickFixProcessors", true);
    }
    IQuickFixProcessor[] processors =
      new IQuickFixProcessor[correctionProcessors.length];
    for(int ii = 0; ii < correctionProcessors.length; ii++){
      processors[ii] = (IQuickFixProcessor)
        correctionProcessors[ii].getProcessor(src, IQuickFixProcessor.class);
    }
    return processors;
  }

  /**
   * Gets array of IQuickAssistProcessor(s).
   * <p/>
   * Based on
   * org.eclipse.jdt.internal.ui.text.correction.JavaCorrectionProcessor#getAssistProcessors()
   *
   * @param src The src file to get processors for.
   * @return quick assist processors.
   */
  public static IQuickAssistProcessor[] getQuickAssistProcessors(
      ICompilationUnit src)
    throws Exception
  {
    if (assistProcessors == null) {
      assistProcessors = getProcessorDescriptors("quickAssistProcessors", false);
    }
    IQuickAssistProcessor[] processors =
      new IQuickAssistProcessor[assistProcessors.length];
    for(int ii = 0; ii < assistProcessors.length; ii++){
      processors[ii] = (IQuickAssistProcessor)
        assistProcessors[ii].getProcessor(src, IQuickAssistProcessor.class);
    }
    return processors;
  }

  private static ContributedProcessorDescriptor[] getProcessorDescriptors(
      String id, boolean testMarkerTypes)
    throws Exception
  {
    IConfigurationElement[] elements = Platform.getExtensionRegistry()
      .getConfigurationElementsFor(JavaUI.ID_PLUGIN, id);
    ArrayList<ContributedProcessorDescriptor> res =
      new ArrayList<ContributedProcessorDescriptor>(elements.length);

    for(int ii = 0; ii < elements.length; ii++){
      ContributedProcessorDescriptor desc =
        new ContributedProcessorDescriptor(elements[ii], testMarkerTypes);
      IStatus status = desc.checkSyntax();
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
    private ArrayList<IProblem> problems = new ArrayList<IProblem>();
    private int[] ids;

    /**
     * Constructs a new instance.
     *
     * @param ids Array of problem ids to accept.
     */
    public ProblemRequestor(int[] ids)
    {
      this.ids = ids;
    }

    /**
     * Gets a list of problems recorded.
     *
     * @return The list of problems.
     */
    public List<IProblem> getProblems()
    {
      return problems;
    }

    /**
     * {@inheritDoc}
     */
    public void acceptProblem(IProblem problem)
    {
      if(ids != null){
        for (int ii = 0; ii < ids.length; ii++){
          if(problem.getID() == ids[ii]){
            problems.add(problem);
            break;
          }
        }
      }else{
        problems.add(problem);
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
