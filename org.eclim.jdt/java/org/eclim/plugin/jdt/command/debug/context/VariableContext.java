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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclim.plugin.jdt.command.debug.ui.VariableView;

import org.eclipse.debug.core.DebugException;

import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;

import org.eclipse.jdt.debug.core.IJavaThread;

/**
 * Maintains the variables belonging to a debug session.
 */
public class VariableContext
{
  private ThreadContext threadCtx;

  private VariableView varView;

  private Map<Long, IVariable[]> varsMap =
    new HashMap<Long, IVariable[]>();

  public VariableContext(ThreadContext threadCtx)
  {
    this.threadCtx = threadCtx;
    this.varView = new VariableView();
  }

  public synchronized List<String> get()
    throws DebugException
  {
    IThread thread = threadCtx.getSteppingThread();
    return varView.get(thread);
  }

  public synchronized void update(IThread thread, IVariable[] vars)
    throws DebugException
  {
    long threadId = ((IJavaThread) thread).getThreadObject().getUniqueId();
    varsMap.put(threadId, vars);

    varView.update(threadId, vars);
  }

  public synchronized void removeVariables()
    throws DebugException
  {
    for (long threadId : varsMap.keySet()) {
      varsMap.put(threadId, null);
      varView.update(threadId, null);
    }
  }
}
