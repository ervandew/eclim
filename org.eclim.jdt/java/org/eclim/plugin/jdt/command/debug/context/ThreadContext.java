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
import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;

import org.eclim.plugin.jdt.command.debug.ui.ThreadView;

import org.eclipse.debug.core.DebugException;

import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;

import org.eclipse.jdt.debug.core.IJavaThread;

/**
 * Maintains the state of all threads belonging to a debug session.
 */
public class ThreadContext
{
  private ThreadView threadView;

  /**
   * Thread that is being currently stepped through. There can be more than one
   * thread suspended in some breakpoint ready to be stepped through. But only
   * one of them can be stepped through at a time. This field holds that
   * thread.
   */
  private IThread steppingThread = null;

  private Map<Long, IThread> threadMap = new ConcurrentHashMap<Long, IThread>();

  private Map<Long, IStackFrame[]> stackFrameMap =
    new ConcurrentHashMap<Long, IStackFrame[]>();

  public ThreadContext()
  {
    this.threadView = new ThreadView();
  }

  public synchronized IThread getSteppingThread()
  {
    if (steppingThread != null) {
      // Check if it is still valid thread and in suspended state
      if (steppingThread.isSuspended()) {
        return steppingThread;
      }
    }

    // Find the first suspended thread and set it as stepping thread
    for (Map.Entry<Long, IThread> entry : threadMap.entrySet()) {
      if (entry.getValue().isSuspended()) {
        steppingThread = entry.getValue();
      }
    }

    return steppingThread;
  }

  /**
   * Explicitly sets the stepping thread.
   */
  public synchronized void setSteppingThread(IThread thread)
  {
    this.steppingThread = thread;
  }

  public IThread getThread(long threadId)
  {
    return threadMap.get(threadId);
  }

  public synchronized List<String> get()
  {
    return threadView.get();
  }

  public synchronized void update(IThread thread, IStackFrame[] stackFrames)
    throws DebugException
  {

    long threadId = ((IJavaThread) thread).getThreadObject().getUniqueId();
    threadMap.put(threadId, thread);
    if (stackFrames != null) {
      stackFrameMap.put(threadId, stackFrames);
    }

    threadView.update(threadMap, stackFrameMap);
  }

  public synchronized void remove(IThread thread)
    throws DebugException
  {
    long threadId = ((IJavaThread) thread).getThreadObject().getUniqueId();
    threadMap.remove(threadId);
    stackFrameMap.remove(threadId);
  }

  public synchronized void removeStackFrames()
  {
    stackFrameMap.clear();
  }

  /**
   * Suspends the given thread.
   */
  public void suspend(long threadId)
    throws DebugException
  {
    IThread thread = threadMap.get(threadId);
    if (thread != null) {
      thread.suspend();
    }
  }

  /**
   * Resumes execution of the given thread.
   */
  public void resume(long threadId)
    throws DebugException
  {
    IThread thread = threadMap.get(threadId);
    if (thread != null) {
      thread.resume();
    }
  }
}
