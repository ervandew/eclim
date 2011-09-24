/**
 * Copyright (C) 2005 - 2011  Eric Van Dewoestine
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
package org.eclim.plugin.cdt.command.hierarchy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.text.Collator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.cdt.command.search.SearchCommand;

import org.eclim.plugin.cdt.util.CUtils;

import org.eclim.util.StringUtils;

import org.eclim.util.file.Position;

import org.eclipse.cdt.core.CCorePlugin;

import org.eclipse.cdt.core.dom.ast.IBinding;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;

import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IIndexName;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IFunction;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;

import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;

import org.eclipse.cdt.internal.core.model.ASTCache;

import org.eclipse.cdt.internal.ui.callhierarchy.CallHierarchyUI;

import org.eclipse.cdt.internal.ui.editor.ASTProvider;
import org.eclipse.cdt.internal.ui.editor.WorkingCopyManager;

import org.eclipse.cdt.internal.ui.viewsupport.IndexUI;

import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;

import org.eclipse.ui.IEditorInput;

import org.eclipse.ui.part.FileEditorInput;

/**
 * Command to generate a call hierarchy for a method or function.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "c_callhierarchy",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "REQUIRED o offset ARG," +
    "REQUIRED l length ARG," +
    "REQUIRED e encoding ARG"
)
public class CallHierarchyCommand
  extends SearchCommand
{
  /**
   * {@inheritDoc}
   * @see org.eclim.command.Command#execute(CommandLine)
   */
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String projectName = commandLine.getValue(Options.PROJECT_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);
    int offset = getOffset(commandLine);
    int length = commandLine.getIntValue(Options.LENGTH_OPTION);

    ICProject cproject = CUtils.getCProject(projectName);

    CUIPlugin cuiPlugin = CUIPlugin.getDefault();
    ITranslationUnit src = CUtils.getTranslationUnit(cproject, file);
    src = src.getSharedWorkingCopy(null, cuiPlugin.getBufferFactory());

    IEditorInput input = new FileEditorInput((IFile)src.getResource());

    // hack... there has to be a better way
    WorkingCopyManager manager = (WorkingCopyManager)
      cuiPlugin.getWorkingCopyManager();
    manager.connect(input);
    manager.setWorkingCopy(input, (IWorkingCopy)src);

    HashMap<String,Object> result = new HashMap<String,Object>();
    try{
      src.open(null);

      // more hacks to got get around gui dependency
      ASTProvider provider = ASTProvider.getASTProvider();
      Field astCache = ASTProvider.class.getDeclaredField("fCache");
      astCache.setAccessible(true);
      ((ASTCache)astCache.get(provider)).setActiveElement(src);

      TextSelection selection = new TextSelection(offset, length);
      Method findDefinitions = CallHierarchyUI.class.getDeclaredMethod(
          "findDefinitions",
          ICProject.class, IEditorInput.class, ITextSelection.class);
      findDefinitions.setAccessible(true);
      ICElement[] elements = (ICElement[])findDefinitions.invoke(
          null, cproject, input, selection);

      if (elements != null && elements.length > 0) {
        ICElement callee = elements[0];
        Set<ICElement> seen = new HashSet<ICElement>();
        ICProject project = callee.getCProject();
        ICProject[] scope = getScope(SCOPE_PROJECT, project);
        IIndex index = CCorePlugin.getIndexManager().getIndex(
            scope, IIndexManager.ADD_DEPENDENCIES | IIndexManager.ADD_DEPENDENT);
        index.acquireReadLock();
        try{
          IIndexName name = IndexUI.elementToName(index, callee);
          result = formatElement(index, callee, name, seen);
        }finally{
          index.releaseReadLock();
        }
      }
    }finally{
      manager.removeWorkingCopy(input);
      manager.disconnect(input);
    }

    return result;
  }

  private ArrayList<HashMap<String,Object>> findCalledBy(
      IIndex index, ICElement callee, Set<ICElement> seen)
    throws Exception
  {
    ArrayList<HashMap<String,Object>> results =
      new ArrayList<HashMap<String,Object>>();
    ICProject project = callee.getCProject();
    IIndexBinding calleeBinding = IndexUI.elementToBinding(index, callee);
    if (calleeBinding != null) {
      results.addAll(findCalledBy(index, calleeBinding, true, project, seen));
      if (calleeBinding instanceof ICPPMethod) {
        IBinding[] overriddenBindings =
          ClassTypeHelper.findOverridden((ICPPMethod)calleeBinding);
        for (IBinding overriddenBinding : overriddenBindings) {
          results.addAll(findCalledBy(
              index, overriddenBinding, false, project, seen));
        }
      }
    }
    return results;
  }

  private ArrayList<HashMap<String,Object>> findCalledBy(
      IIndex index,
      IBinding callee,
      boolean includeOrdinaryCalls,
      ICProject project,
      Set<ICElement> seen)
    throws Exception
  {
    IIndexName[] names = index.findNames(
        callee, IIndex.FIND_REFERENCES | IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);

    ArrayList<Call> calls = new ArrayList<Call>(names.length);
    for (IIndexName name : names) {
      if (includeOrdinaryCalls || name.couldBePolymorphicMethodCall()) {
        IIndexName caller = name.getEnclosingDefinition();
        if (caller == null) {
          continue;
        }

        ICElement element = IndexUI.getCElementForName(project, index, caller);
        if (element == null) {
          continue;
        }
        calls.add(new Call(name, element));
      }
    }

    Collections.sort(calls);

    ArrayList<HashMap<String,Object>> results =
      new ArrayList<HashMap<String,Object>>();
    for (Call call : calls) {
      results.add(formatElement(index, call.element, call.name, seen));
    }
    return results;
  }

  private HashMap<String,Object> formatElement(
      IIndex index, ICElement element, IIndexName name, Set<ICElement> seen)
    throws Exception
  {
    HashMap<String,Object> result = new HashMap<String,Object>();

    String[] types = null;
    if (element instanceof IFunction){
      types = ((IFunction)element).getParameterTypes();
    }else if (element instanceof IFunctionDeclaration){
      types = ((IFunctionDeclaration)element).getParameterTypes();
    }
    String message = element.getElementName() +
      '(' + StringUtils.join(types, ", ") + ')';
    result.put("name", message);

    if (name != null){
      IResource resource = element.getResource();
      if (resource != null){
        String file = element.getResource()
          .getRawLocation().toOSString().replace('\\', '/');
        result.put("position",
            Position.fromOffset(file, null, name.getNodeOffset(), 0));
      }
    }

    if (!seen.contains(element)){
      seen.add(element);
      result.put("calledBy", findCalledBy(index, element, seen));
    }

    return result;
  }

  private static class Call
    implements Comparable<Call>
  {
    private static final Collator COLLATOR = Collator.getInstance();

    public IIndexName name;
    public ICElement element;
    public String location;

    public Call(IIndexName name, ICElement element){
      this.name = name;
      this.element = element;
      this.location =
        element.getResource().getRawLocation().toOSString() +
        element.getElementName();
    }

    /**
     * {@inheritDoc}
     * @see Comparable#compareTo(T)
     */
    public int compareTo(Call o)
    {
      int result = COLLATOR.compare(location, o.location);
      if (result == 0){
        return name.getNodeOffset() - o.name.getNodeOffset();
      }
      return result;
    }
  }
}
