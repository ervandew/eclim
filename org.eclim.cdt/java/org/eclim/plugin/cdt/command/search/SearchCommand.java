/**
 * Copyright (C) 2005 - 2017  Eric Van Dewoestine
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
package org.eclim.plugin.cdt.command.search;

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.taskdefs.condition.Os;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.cdt.PluginResources;

import org.eclim.plugin.cdt.util.CUtils;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.util.CollectionUtils;
import org.eclim.util.StringUtils;

import org.eclim.util.file.Position;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;

import org.eclipse.cdt.core.dom.IName;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;

import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IndexFilter;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;

import org.eclipse.cdt.internal.ui.search.CSearchElement;
import org.eclipse.cdt.internal.ui.search.CSearchMatch;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.search.CSearchPatternQuery;
import org.eclipse.cdt.internal.ui.search.CSearchQuery;
import org.eclipse.cdt.internal.ui.search.CSearchResult;

import org.eclipse.cdt.internal.ui.viewsupport.IndexUI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.search.ui.text.Match;

/**
 * Command to handle search requests.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "c_search",
  options =
    "OPTIONAL n project ARG," +
    "OPTIONAL f file ARG," +
    "OPTIONAL o offset ARG," +
    "OPTIONAL l length ARG," +
    "OPTIONAL e encoding ARG," +
    "OPTIONAL p pattern ARG," +
    "OPTIONAL t type ARG," +
    "OPTIONAL x context ARG," +
    "OPTIONAL s scope ARG," +
    "OPTIONAL i case_insensitive NOARG"
)
public class SearchCommand
  extends AbstractCommand
{
  public static final String CONTEXT_ALL = "all";
  public static final String CONTEXT_CONTEXT = "context";
  public static final String CONTEXT_DECLARATIONS = "declarations";
  public static final String CONTEXT_DEFINITIONS = "definitions";
  public static final String CONTEXT_REFERENCES = "references";

  private static final int FIND_CONTEXT = -1;

  public static final String SCOPE_ALL = "all";
  public static final String SCOPE_PROJECT = "project";

  public static final String TYPE_ALL = "all";
  public static final String TYPE_CLASS_STRUCT = "class_struct";
  public static final String TYPE_FUNCTION = "function";
  public static final String TYPE_VARIABLE = "variable";
  public static final String TYPE_UNION = "union";
  public static final String TYPE_METHOD = "method";
  public static final String TYPE_FIELD = "field";
  public static final String TYPE_ENUM = "enum";
  public static final String TYPE_ENUMERATOR = "enumerator";
  public static final String TYPE_NAMESPACE = "namespace";
  public static final String TYPE_TYPEDEF = "typedef";
  public static final String TYPE_MACRO = "macro";

  private static final Pattern PATTERN_MATCH = Pattern.compile("^\\d+:\\s*");

  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String projectName = commandLine.getValue(Options.NAME_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);
    String offset = commandLine.getValue(Options.OFFSET_OPTION);
    String length = commandLine.getValue(Options.LENGTH_OPTION);

    IProject project = projectName != null ?
      ProjectUtils.getProject(projectName) : null;
    ICProject cproject = null;
    if (project != null){
      cproject = CUtils.getCProject(project);
    }

    // element search
    if(file != null && offset != null && length != null){
      return executeElementSearch(commandLine, cproject);
    }

    // pattern search
    return executePatternSearch(commandLine, cproject);
  }

  private Object executeElementSearch(
      CommandLine commandLine, ICProject cproject)
    throws Exception
  {
    ArrayList<Position> results = new ArrayList<Position>();
    String file = commandLine.getValue(Options.FILE_OPTION);
    ITranslationUnit src = CUtils.getTranslationUnit(cproject, file);
    if(src != null){
      int context = getContext(
          commandLine.getValue(Options.CONTEXT_OPTION), FIND_CONTEXT);

      ICProject[] scope = new ICProject[]{cproject};
      if (SCOPE_ALL.equals(commandLine.getValue(Options.SCOPE_OPTION))){
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        ArrayList<ICProject> cprojects = new ArrayList<ICProject>();
        for (IProject project : projects){
          if (project.isOpen() && (
              project.hasNature(PluginResources.NATURE_C) ||
              project.hasNature(PluginResources.NATURE_CPP)))
          {
            cprojects.add(CUtils.getCProject(project));
          }
        }
        scope = cprojects.toArray(new ICProject[cprojects.size()]);
      }

      IIndex index = CCorePlugin.getIndexManager().getIndex(
          scope, IIndexManager.ADD_DEPENDENCIES | IIndexManager.ADD_DEPENDENT);
      index.acquireReadLock();
      try{
        int offset = getOffset(commandLine);
        int length = commandLine.getIntValue(Options.LENGTH_OPTION);
        IName[] names = findElement(src, scope, context, offset, length);
        for (IName iname : names){
          IASTFileLocation loc = iname.getFileLocation();
          String filename = loc.getFileName().replace('\\', '/');
          results.add(
              Position.fromOffset(filename, null, loc.getNodeOffset(), 0));
        }
      }finally{
        index.releaseReadLock();
      }
    }

    return results;
  }

  private Object executePatternSearch(
      CommandLine commandLine, ICProject cproject)
    throws Exception
  {
    String scopeName = commandLine.getValue(Options.SCOPE_OPTION);
    ICProject[] scope = getScope(scopeName, cproject);
    String scopeDesc = null;
    if (cproject == null || SCOPE_ALL.equals(scopeName)){
      scopeDesc = CSearchMessages.WorkspaceScope;
    }else if (SCOPE_PROJECT.equals(scopeName)){
      scopeDesc = CSearchMessages.ProjectScope;
    }

    int context = getContext(commandLine.getValue(Options.CONTEXT_OPTION));
    int type = getType(commandLine.getValue(Options.TYPE_OPTION));
    String pattern = commandLine.getValue(Options.PATTERN_OPTION);
    boolean caseSensitive =
      !commandLine.hasOption(Options.CASE_INSENSITIVE_OPTION);
    CSearchQuery query = new CSearchPatternQuery(
        scope, scopeDesc, pattern, caseSensitive, type | context);

    ArrayList<Position> results = new ArrayList<Position>();
    if (query != null){
      query.run(new NullProgressMonitor());
      CSearchResult result = (CSearchResult)query.getSearchResult();
      for (Object e : result.getElements()){
        Method method = CSearchElement.class.getDeclaredMethod("getLocation");
        method.setAccessible(true);
        IIndexFileLocation location = (IIndexFileLocation)method.invoke(e);
        String filename = location.getURI().getPath();
        if (Os.isFamily("windows") && filename.startsWith("/")){
          filename = filename.substring(1);
        }
        for (Match m : result.getMatches(e)){
          CSearchMatch match = (CSearchMatch)m;
          String message = match.getElement().toString();
          Matcher matcher = PATTERN_MATCH.matcher(message);
          message = matcher.replaceFirst(StringUtils.EMPTY);
          results.add(
              Position.fromOffset(filename, message, match.getOffset(), 0));
        }
      }
    }

    return results;
  }

  protected IName[] findElement(
      ITranslationUnit src,
      ICProject[] scope, int context,
      int offset, int length)
    throws Exception
  {
    LinkedHashSet<IName> names = new LinkedHashSet<IName>();
    IIndex index = CCorePlugin.getIndexManager().getIndex(
        scope,
        IIndexManager.ADD_DEPENDENCIES |
        IIndexManager.ADD_DEPENDENT |
        IIndexManager.ADD_EXTENSION_FRAGMENTS_NAVIGATION);
    index.acquireReadLock();
    try{
      IASTTranslationUnit ast = src.getAST(index,
          ITranslationUnit.AST_CONFIGURE_USING_SOURCE_CONTEXT |
          ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
      IASTNodeSelector selector = ast.getNodeSelector(null);
      IASTName name = selector.findEnclosingName(offset, length);
      if (name != null){
        IBinding binding = name.resolveBinding();

        int flags = IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES;
        if (context == FIND_CONTEXT){
          if (!name.isDeclaration() && !name.isDefinition()){
            flags |= IIndex.FIND_DEFINITIONS;
          } else {
            // if on the declaration, search for the definition and vice verca
            flags |= (name.isDefinition() ?
                IIndex.FIND_DECLARATIONS : IIndex.FIND_DEFINITIONS);
          }
        } else if (context == CSearchQuery.FIND_ALL_OCCURRENCES){
          flags |= IIndex.FIND_ALL_OCCURRENCES;
        } else if (context == CSearchQuery.FIND_REFERENCES){
          flags |= IIndex.FIND_REFERENCES;
        } else if (context == CSearchQuery.FIND_DECLARATIONS_DEFINITIONS) {
          flags |= IIndex.FIND_DECLARATIONS_DEFINITIONS;
        } else if (context == CSearchQuery.FIND_DECLARATIONS) {
          flags |= IIndex.FIND_DECLARATIONS;
        } else if (context == CSearchQuery.FIND_DEFINITIONS) {
          flags |= IIndex.FIND_DEFINITIONS;
        }

        CollectionUtils.addAll(names, index.findNames(binding, flags));

        // kind of hacky.  if we issued a context search and found no
        // definitions, we'll try a declarations search (useful for system
        // library references).
        if (names.size() == 0 &&
            context == FIND_CONTEXT &&
            (flags & IIndex.FIND_DEFINITIONS) != 0)
        {
          CollectionUtils.addAll(names, index.findNames(
                binding,
                IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES |
                IIndex.FIND_DECLARATIONS));
        }

        if (names.size() == 0){
          // alternate search that finds some things that index.findNames may
          // not.
          if ((flags & IIndex.FIND_DECLARATIONS) != 0){
            CollectionUtils.addAll(names, ast.getDeclarations(binding));
          }
          if ((flags & IIndex.FIND_DEFINITIONS) != 0){
            CollectionUtils.addAll(names, ast.getDefinitions(binding));
          }
          if ((flags & IIndex.FIND_REFERENCES) != 0){
            CollectionUtils.addAll(names, ast.getReferences(binding));
          }

          if ((flags & IIndex.FIND_DECLARATIONS) != 0 ||
              (flags & IIndex.FIND_DEFINITIONS) != 0)
          {
            // also try to find macros (now required for stdlib EXIT_SUCCESS)
            // gleaned from navigationFallBack in
            // org.eclipse.cdt.internal.ui.search.actions.OpenDeclarationJob
            IndexFilter filter = IndexFilter.getDeclaredBindingFilter(
                ast.getLinkage().getLinkageID(), false);
            IIndexMacro[] macros = index.findMacros(
                binding.getName().toCharArray(),
                filter,
                new NullProgressMonitor());
            for (IIndexMacro macro : macros) {
              ICElement element = IndexUI.getCElementForMacro(
                  src.getCProject(), index, macro);
              if (element != null) {
                ISourceReference ref = (ISourceReference)element;
                FileLocation location = new FileLocation(
                    ref.getTranslationUnit().getPath().toPortableString(),
                    ref.getSourceRange().getStartPos(),
                    ref.getSourceRange().getLength(),
                    ref.getSourceRange().getStartLine());
                names.add(new Name(location));
              }
            }
          }
        }
      }
    }finally{
      index.releaseReadLock();
    }

    return names.toArray(new IName[names.size()]);
  }

  /**
   * Gets the search scope to use.
   *
   * @param scope The string name of the scope.
   * @param project The current project.
   *
   * @return The ICProject array representing the scope.
   */
  protected ICProject[] getScope(String scope, ICProject project)
  {
    ArrayList<ICProject> elements = new ArrayList<ICProject>();
    try{
      if (project == null || SCOPE_ALL.equals(scope)){
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        for (IProject p : projects){
          if(p.isOpen() && (
                p.hasNature(CProjectNature.C_NATURE_ID) ||
                p.hasNature(CCProjectNature.CC_NATURE_ID)))
          {
            elements.add(CoreModel.getDefault().create(p));
          }
        }
        return elements.toArray(new ICProject[elements.size()]);
      }

      elements.add(project);
      IProject[] depends = project.getProject().getReferencedProjects();
      for (IProject p : depends){
        if(!p.isOpen()){
          p.open(null);
        }
        elements.add(CoreModel.getDefault().create(p));
      }
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }
    return elements.toArray(new ICProject[elements.size()]);
  }

  /**
   * Translates the string context to the int equivalent.
   *
   * @param context The String context.
   * @return The int context
   */
  protected int getContext(String context)
  {
    return getContext(context, CSearchQuery.FIND_DECLARATIONS_DEFINITIONS);
  }

  /**
   * Translates the string context to the int equivalent.
   *
   * @param context The String context.
   * @param dflt The default String context.
   * @return The int context
   */
  protected int getContext(String context, int dflt)
  {
    if (context == null){
      return dflt;
    }

    if(CONTEXT_ALL.equals(context)){
      return CSearchQuery.FIND_ALL_OCCURRENCES;
    }else if(CONTEXT_CONTEXT.equals(context)){
      return FIND_CONTEXT;
    }else if(CONTEXT_REFERENCES.equals(context)){
      return CSearchQuery.FIND_REFERENCES;
    }else if(CONTEXT_DECLARATIONS.equals(context)){
      return CSearchQuery.FIND_DECLARATIONS;
    }else if(CONTEXT_DEFINITIONS.equals(context)){
      return CSearchQuery.FIND_DEFINITIONS;
    }
    return CSearchQuery.FIND_DECLARATIONS_DEFINITIONS;
  }

  /**
   * Translates the string type to the int equivalent.
   *
   * @param type The String type.
   * @return The int type.
   */
  protected int getType(String type)
  {
    if(TYPE_CLASS_STRUCT.equals(type)){
      return CSearchPatternQuery.FIND_CLASS_STRUCT;
    }else if(TYPE_FUNCTION.equals(type)){
      return CSearchPatternQuery.FIND_FUNCTION;
    }else if(TYPE_VARIABLE.equals(type)){
      return CSearchPatternQuery.FIND_VARIABLE;
    }else if(TYPE_UNION.equals(type)){
      return CSearchPatternQuery.FIND_UNION;
    }else if(TYPE_METHOD.equals(type)){
      return CSearchPatternQuery.FIND_METHOD;
    }else if(TYPE_FIELD.equals(type)){
      return CSearchPatternQuery.FIND_FIELD;
    }else if(TYPE_ENUM.equals(type)){
      return CSearchPatternQuery.FIND_ENUM;
    }else if(TYPE_ENUMERATOR.equals(type)){
      return CSearchPatternQuery.FIND_ENUMERATOR;
    }else if(TYPE_NAMESPACE.equals(type)){
      return CSearchPatternQuery.FIND_NAMESPACE;
    }else if(TYPE_TYPEDEF.equals(type)){
      return CSearchPatternQuery.FIND_TYPEDEF;
    }else if(TYPE_MACRO.equals(type)){
      return CSearchPatternQuery.FIND_MACRO;
    }
    return CSearchPatternQuery.FIND_ALL_TYPES;
  }

  private class Name
    implements IName
  {
    private IASTFileLocation fileLocation;

    public Name(IASTFileLocation fileLocation)
    {
      this.fileLocation = fileLocation;
    }

    @Override
    public char[] getSimpleID()
    {
      return null;
    }

    @Override
    public char[] toCharArray()
    {
      return null;
    }

    @Override
    public boolean isDeclaration()
    {
      return false;
    }

    @Override
    public boolean isReference()
    {
      return false;
    }

    @Override
    public boolean isDefinition()
    {
      return false;
    }

    @Override
    public IASTFileLocation getFileLocation()
    {
      return fileLocation;
    }
  }

  private class FileLocation
    implements IASTFileLocation
  {
    private String fileName;
    private int offset;
    private int length;
    private int line;

    public FileLocation(String fileName, int offset, int length, int line)
    {
      this.fileName = fileName;
      this.offset = offset;
      this.length = length;
      this.line = line;
    }

    @Override
    public IASTFileLocation asFileLocation()
    {
      return this;
    }

    @Override
    public String getFileName()
    {
      return fileName;
    }

    @Override
    public int getNodeOffset()
    {
      return offset;
    }

    @Override
    public int getNodeLength()
    {
      return length;
    }

    @Override
    public int getStartingLineNumber()
    {
      return line;
    }

    @Override
    public int getEndingLineNumber()
    {
      return 0;
    }

    @Override
    public IASTPreprocessorIncludeStatement getContextInclusionStatement()
    {
      return null;
    }
  }
}
