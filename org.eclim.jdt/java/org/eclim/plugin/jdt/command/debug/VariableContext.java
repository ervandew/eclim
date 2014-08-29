/**
 * Copyright (C) 2014  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.debug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclim.logging.Logger;

import org.eclipse.debug.core.DebugException;

import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;

import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaClassObject;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;

import org.eclipse.jdt.internal.debug.core.model.JDIType;
import org.eclipse.jdt.internal.debug.core.model.JDIValue;
import org.eclipse.jdt.internal.debug.core.model.JDIVariable;

/**
 * Maintains the variables belonging to a debug session.
 */
public class VariableContext
{
  private static final Logger logger = Logger.getLogger(VariableContext.class);

  private Map<Long, IVariable[]> varsMap =
    new HashMap<Long, IVariable[]>();

  private Map<Long, List<String>> resultsMap =
    new HashMap<Long, List<String>>();

  public synchronized void clear()
  {
    varsMap.clear();
    resultsMap.clear();
  }

  public synchronized List<String> get()
  {
    // TODO Take thread id as input and return corresponding variables
    // Use IJavaThread.getThreadObject.getUniqueId
    for (Map.Entry<Long, List<String>> entry : resultsMap.entrySet()) {
      return entry.getValue();
    }

    return Collections.emptyList();
  }

  public synchronized void update(IThread thread, IVariable[] vars)
    throws DebugException
  {
    long threadId = ((IJavaThread) thread).getThreadObject().getUniqueId();
    varsMap.put(threadId, vars);

    List<String> results = resultsMap.get(threadId);
    if (results == null) {
      results = new ArrayList<String>();
      resultsMap.put(threadId, results);
    }

    results.clear();
    process(varsMap.get(threadId), results, 0);
  }

  public synchronized void removeVariables() {
    for (long threadId : varsMap.keySet()) {
      varsMap.put(threadId, null);
    }
  }

  private void process(IVariable[] vars, List<String> results, int level)
    throws DebugException
  {
    if (vars == null) {
      return;
    }

    // Indent nested variables
    String prefix = getIndentation(level);

    // Defensive code to protect from too many nesting
    if (level >= 4) {
      results.add(prefix + " Nesting terminated");
      return;
    }

    for (IVariable var : vars) {
      JDIVariable jvar = (JDIVariable) var;
      if (jvar.isSynthetic()) {
        continue;
      }

      JDIValue value = (JDIValue) var.getValue();
      if (ignoreVar(jvar)) {
        continue;
      }

      // TODO Create an object and send it over to VIM.
      // Do text formatting in VIM.
      results.add(prefix + " " + var.getName() + " : " +
          var.getValue().getValueString());

      if (value == null || !includeNestedVar(value)) {
        continue;
      }
      if (value instanceof IJavaObject) {
        if (value.hasVariables()) {
          process(value.getVariables(), results, level + 1);
        }
      }
    }
  }

  /**
   * Igmores final primitive variables.
   */
  private boolean ignoreVar(JDIVariable var)
    throws DebugException
  {
    if (var.isFinal()) {
      JDIValue value = (JDIValue) var.getValue();
      JDIType type = (JDIType) value.getJavaType();
      // TODO Add tools.jar to access com.sun classes
      // Use underlying type to ignore final var that are primitive only
      return true;
    }

    return false;
  }

  /**
   * Determines whether nested variables should be returned as part of result
   * set. Most times, we don't want to return fields that are part of Class
   * that are not part of the source code. For e.g., java.lang.String contains
   * fields that the application debugger doesn't care about. We only need to
   * return the value of the string in this case.
   *
   */
  private boolean includeNestedVar(JDIValue value)
    throws DebugException
  {
    boolean nesting = true;

    if ((value instanceof IJavaArray) ||
        (value instanceof IJavaClassObject))
    {

      nesting = false;
    } else {
      JDIType type = (JDIType) value.getJavaType();
      // TODO Add tools.jar to access com.sun classes
      //type.getUnderlyingType();

      if (type == null) {
        nesting = false;
      } else {
        String typeName = type.getName();
        logger.debug("found --" + typeName + "--");

        // TODO Instead of listing what to ignore, find them out by looking at
        // the source locator for class names that are not present.
        if (typeName.equals("java.util.List") ||
            typeName.equals("java.util.Map") ||
            typeName.equals("java.lang.String"))
        {
          logger.debug("Skipping: " + typeName);
          nesting = false;
        }
      }
    }

    return nesting;
  }

  /**
   * Returns the prefix string to use to simulate indentation.
   */
  private String getIndentation(int level)
  {
    if (level == 0) {
      return "";
    }

    StringBuilder sb = new StringBuilder();

    // Add a base indent since the fold level 0 has it
    sb.append("  ");
    for (int i = 0; i < level; i++) {
      sb.append(" ");
    }

    return sb.toString();
  }
}
