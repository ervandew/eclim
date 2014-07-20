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
import org.eclipse.jdt.debug.core.IJavaObject;
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
    if (vars != null) {
      for (IVariable var : vars) {
        result.add(var.getName() + " : " + var.getValue().getValueString());
        printVar(var);
      }
    }

    return result;
  }

  private void printVar(IVariable var) throws DebugException
  {
    IJavaVariable jvar = (IJavaVariable) var;
    IJavaValue jvalue = (IJavaValue) jvar.getValue();
    logger.info(jvar.getJavaType() + " " + jvar.getName() + " " +
        jvar.getValue().getValueString() + " " + jvalue.getClass().getName());
    if (jvalue instanceof IJavaArray) {
      logger.info("ijavaarr found");
      IJavaArray jarr = (IJavaArray) jvalue;
      if (jarr.getSize() > 0) {
        logger.info("array value " + jarr.getSize() + " " +
            jarr.getValue(0).getValueString());
      }
    } else if (jvalue instanceof IJavaObject) {
      logger.info("fond ijavaobject: " + ((IJavaObject) jvalue).toString());
    }
  }
}
