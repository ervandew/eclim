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
package org.eclim.plugin.jdt.command.hierarchy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.jdt.command.search.SearchCommand;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclim.util.file.Position;

import org.eclipse.core.resources.IResource;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;

import org.eclipse.jdt.core.search.SearchEngine;

import org.eclipse.jdt.internal.corext.callhierarchy.CallHierarchy;
import org.eclipse.jdt.internal.corext.callhierarchy.CallLocation;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;

import org.eclipse.jdt.internal.ui.viewsupport.AppearanceAwareLabelProvider;

import org.eclipse.jdt.ui.JavaElementLabels;

/**
 * Command to generate a call hierarchy for a method.
 *
 * @author Alexandre Fonseca
 */
@Command(
  name = "java_callhierarchy",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG," +
    "REQUIRED o offset ARG," +
    "REQUIRED l length ARG," +
    "REQUIRED e encoding ARG," +
    "OPTIONAL s scope ARG," +
    "OPTIONAL c callees NOARG"
)
public class CallHierarchyCommand
  extends SearchCommand
{
  private static final String CALLEES_OPTION = "c";
  private static final int MAX_CALL_DEPTH = 3;

  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    HashMap<String,Object> result  = new HashMap<String, Object>();

    String project = commandLine.getValue(Options.PROJECT_OPTION);
    String file = commandLine.getValue(Options.FILE_OPTION);
    boolean callees = commandLine.hasOption(CALLEES_OPTION);
    String scope = commandLine.getValue(Options.SCOPE_OPTION);
    int length = commandLine.getIntValue(Options.LENGTH_OPTION);
    int offset = getOffset(commandLine);

    ICompilationUnit src = JavaUtils.getCompilationUnit(project, file);
    IJavaElement[] elements = src.codeSelect(offset, length);
    if(elements == null || elements.length == 0){
      return result;
    }

    IJavaElement element = elements[0];
    if (element instanceof IMethod) {
      IMethod method = (IMethod) element;
      IMember[] members = new IMember[]{method};
      MethodWrapper[] roots;

      CallHierarchy callHierarchy = CallHierarchy.getDefault();
      callHierarchy.setSearchScope(getScope(scope, src.getJavaProject()));

      Comparator<MethodWrapper> comparator = null;

      if (callees) {
        roots = callHierarchy.getCalleeRoots(members);
      } else {
        roots = callHierarchy.getCallerRoots(members);
        // Following Eclipse's GUI, callers are ordered
        // alphabetically, callees by position in function.
        comparator = new Comparator<MethodWrapper>() {
          public int compare(MethodWrapper o1, MethodWrapper o2) {
            return o1.getName().compareToIgnoreCase(o2.getName());
          }
        };
      }

      if (roots.length > 0) {
        // Is it possible to have multiple roots? If so we'll need
        // to change this.
        result = formatRoot(roots[0], comparator, callees);

        IResource resource = method.getResource();
        ISourceRange sourceRange = method.getSourceRange();
        // The root element doesn't get his location like all the others
        // (this happens with the GUI too). So add it ourselves.
        result.put("position", Position.fromOffset(
              resource.getLocation().toOSString(), null, sourceRange.getOffset(),
              sourceRange.getLength()));
      }
    }

    return result;
  }

  private ArrayList<HashMap<String,Object>> formatRoots(
      MethodWrapper[] roots,
      Comparator<MethodWrapper> comparator,
      boolean callees)
    throws Exception
  {
    ArrayList<HashMap<String,Object>> results =
      new ArrayList<HashMap<String,Object>>();

    if (comparator != null) {
      Arrays.sort(roots, comparator);
    }

    for (MethodWrapper root : roots) {
      if (root.getLevel() > MAX_CALL_DEPTH || root.isRecursive()) {
        continue;
      }
      results.add(formatRoot(root, comparator, callees));
    }

    return results;
  }

  private HashMap<String, Object> formatRoot(
      MethodWrapper root, Comparator<MethodWrapper> comparator, boolean callees)
    throws Exception
  {
    IMember member = root.getMember();
    String memberName = JavaElementLabels.getTextLabel(member,
        AppearanceAwareLabelProvider.DEFAULT_TEXTFLAGS |
        JavaElementLabels.ALL_POST_QUALIFIED |
        JavaElementLabels.P_COMPRESSED);
    CallLocation location = root.getMethodCall().getFirstCallLocation();

    HashMap<String,Object> result = new HashMap<String, Object>();
    result.put("name", memberName);

    if (location != null) {
      // If caller, locationMember == member. If callee, locationMember
      // is the function where the callee is called.
      IMember locationMember = location.getMember();
      IResource resource = locationMember.getResource();

      if (resource != null) {
        String file = resource.getLocation().toOSString().replace('\\', '/');
        int offset = location.getStart();

        result.put("position", Position.fromOffset(
              file, null, offset, location.getEnd() - offset));
      }
    }

    result.put(callees ? "callees" : "callers", formatRoots(
        root.getCalls(new NullProgressMonitor()),
        comparator,
        callees));

    return result;
  }
}
