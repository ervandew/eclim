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
package org.eclim.plugin.jdt.command.debug.context;

import java.util.List;

import org.eclim.plugin.jdt.command.debug.ui.VariableView;

import org.eclipse.debug.core.DebugException;

import org.eclipse.debug.core.model.IThread;

/**
 * Maintains the variables belonging to a debug session.
 */
public class VariableContext
{
  private ThreadContext threadCtx;

  private VariableView varView;

  public VariableContext(ThreadContext threadCtx)
  {
    this.threadCtx = threadCtx;
    this.varView = new VariableView();
  }

  public List<String> get()
  {
    IThread thread = threadCtx.getSteppingThread();
    return varView.get(thread);
  }

  public List<String> expandValue(long valueId)
    throws DebugException
  {
    return varView.expandValue(valueId);
  }

  public void removeVariables()
  {
    varView.removeVariables();
  }
}
