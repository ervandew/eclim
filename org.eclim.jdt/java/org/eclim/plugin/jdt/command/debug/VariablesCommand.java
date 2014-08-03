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
package org.eclim.plugin.jdt.command.debug;

import java.util.ArrayList;
import java.util.List;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;

import org.eclim.logging.Logger;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclipse.debug.core.DebugException;

import org.eclipse.debug.core.model.IVariable;

import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaClassObject;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

/**
 * Command to get variables visible in the current stack frame.
 */
@Command(
  name = "java_debug_vars",
  options = ""
)
public class VariablesCommand extends AbstractCommand
{
  private static final Logger logger = Logger.getLogger(VariablesCommand.class);

  @Override
  public Object execute(CommandLine commandLine) throws Exception
  {
    if (logger.isInfoEnabled()) {
      logger.info("Command: " + commandLine);
    }

    List<String> result = new ArrayList<String>();

    IVariable[] vars = DebuggerContext.getInstance().getVariables();
    processVars(vars, result, 0);

    return result;
  }

  private void processVars(IVariable[] vars, List<String> result, int level)
      throws DebugException
  {
    if (vars == null) {
      return;
    }

    // Indent nested variables
    String prefix = getIndentation(level);

    // Defensive code to protect from too many nesting
    if (level >= 4) {
      result.add(prefix + " STOP STOP STOP");
      logger.info("Too much nesting");
      return;
    }

    for (IVariable var : vars) {
      IJavaVariable jvar = (IJavaVariable) var;
      if (jvar.isSynthetic()) {
        continue;
      }

      result.add(prefix + " " + var.getName() + " : " + var.getValue().getValueString());
      /*result.add(prefix + " " + var.getName() + " : " + var.getValue().getValueString() + " --> " + ((IJavaVariable) var).isSynthetic()
          + " --> " + var.getValue().getClass().getName() + " " + jvar.getJavaType().getName() + " " + jvar.getJavaType().getSignature());
      logger.info(prefix + " " + var.getName() + " : " + var.getValue().getValueString());
      */
      IJavaValue value = (IJavaValue) var.getValue();

      if (!processNestedVar(value)) {
        continue;
      }
      if (value instanceof IJavaObject) {
        if (value.hasVariables()) {
          processVars(value.getVariables(), result, level + 1);
        }
      }
    }
  }

  private boolean processNestedVar(IJavaValue value) throws DebugException {
    boolean nesting = true;

    if ((value instanceof IJavaArray) ||
        (value instanceof IJavaClassObject))
    {

      nesting = false;
    } else {
      IJavaType type = value.getJavaType();
      String typeName = type.getName();

      // TODO Instead of listing what to ignore, find them out by looking at
      // the source locator for class names that are not present.
      if (typeName.equals("java.util.List") ||
          typeName.equals("java.util.Map") ||
          typeName.equals("java.lang.String"))
      {
        nesting = false;
      }
    }

    return nesting;
  }

  private String getIndentation(int level) {
    if (level == 0) {
      return "";
    }

    StringBuilder sb = new StringBuilder();

    sb.append("  ");
    for (int i = 0; i < level; i++) {
      sb.append("-");
    }

    return sb.toString();
  }
}
