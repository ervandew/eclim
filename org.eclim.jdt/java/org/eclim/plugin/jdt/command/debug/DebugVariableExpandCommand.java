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

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.jdt.command.debug.context.DebuggerContext;
import org.eclim.plugin.jdt.command.debug.context.DebuggerContextManager;

/**
 * Command to expand the given variable value and return the child variable
 * values.
 */
@Command(
  name = "java_debug_variable_expand",
  options =
    "REQUIRED v value_id ARG,"
)
public class DebugVariableExpandCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    DebuggerContext ctx = DebuggerContextManager.getDefault();
    if (ctx == null) {
      return null;
    }

    String valueId = commandLine.getValue(Options.VARIABLE_VALUE_ID_OPTION);
    if (valueId == null) {
      return null;
    }

    return ctx.getVariableContext().expandValue(Long.parseLong(valueId));
  }
}
