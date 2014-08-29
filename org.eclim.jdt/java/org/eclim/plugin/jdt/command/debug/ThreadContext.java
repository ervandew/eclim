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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.debug.core.DebugException;

import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;

import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;

/**
 * Maintains the state of all threads belonging to a debug session.
 */
public class ThreadContext
{
  // Thread status
  private static final String RUNNING = "Running";
  private static final String SUSPENDED = "Suspended";

  private Map<Long, IThread> threadMap = new HashMap<Long, IThread>();

  private Map<Long, IStackFrame[]> stackFrameMap =
    new HashMap<Long, IStackFrame[]>();

  private List<String> results = new ArrayList<String>();

  public synchronized void clear()
  {
    stackFrameMap.clear();
    results.clear();
  }

  public List<String> get()
  {
    return results;
  }

  public synchronized void update(IThread thread, IStackFrame[] stackFrames)
    throws DebugException
  {

    long threadId = ((IJavaThread) thread).getThreadObject().getUniqueId();
    threadMap.put(threadId, thread);
    stackFrameMap.put(threadId, stackFrames);

    results.clear();

    process();
  }

  public synchronized void remove(IThread thread)
    throws DebugException
  {
    long threadId = ((IJavaThread) thread).getThreadObject().getUniqueId();
    threadMap.remove(threadId);
    stackFrameMap.remove(threadId);
  }

  public synchronized void removeStackFrames() {
    for (long threadId : stackFrameMap.keySet()) {
      stackFrameMap.put(threadId, null);
    }
  }

  private void process()
    throws DebugException
  {
    for (Map.Entry<Long, IStackFrame[]> entry : stackFrameMap.entrySet()) {
      long threadId = entry.getKey();
      IThread thread = threadMap.get(threadId);
      String threadName = thread.getName();
      IStackFrame[] stackFrames = entry.getValue();

      String status = thread.isSuspended() ? SUSPENDED : RUNNING;
      // Add 2 spaces for indentation
      results.add("  Thread-" + threadName +
          ":" + threadId  +
          " (" + status  + ")");

      if (stackFrames != null) {
        for (IStackFrame stackFrame : stackFrames) {
          // TODO Do formatting in VIM
          // Add 4 spaces for indentation under thread
          results.add("    " + getStackFrameText(stackFrame));
        }
      }
    }
  }

  private String getStackFrameText(IStackFrame stackFrame)
    throws DebugException
  {
    StringBuffer result = new StringBuffer();

    IJavaStackFrame frame = (IJavaStackFrame) stackFrame.getAdapter(
        IJavaStackFrame.class);
    if (frame != null) {
      String dec = frame.getDeclaringTypeName();

      if (frame.isObsolete()) {
        result.append(dec);
        result.append('>');
        return result.toString();
      }

      boolean javaStratum = true;
      javaStratum = frame.getReferenceType().getDefaultStratum().equals("Java");

      if (javaStratum) {
        // receiver name
        String rec = frame.getReceivingTypeName();
        result.append(getQualifiedName(rec));

        // append declaring type name if different
        if (!dec.equals(rec)) {
          result.append('(');
          result.append(getQualifiedName(dec));
          result.append(')');
        }
        // append a dot separator and method name
        result.append('.');
        result.append(frame.getMethodName());
        List<String> args = frame.getArgumentTypeNames();
        if (args.isEmpty()) {
          result.append("()"); //$NON-NLS-1$
        } else {
          result.append('(');
          Iterator<String> iter = args.iterator();
          while (iter.hasNext()) {
            result.append(getQualifiedName(iter.next()));
            if (iter.hasNext()) {
              result.append(", ");
            } else if (frame.isVarArgs()) {
              result.replace(result.length() - 2, result.length(), "...");
            }
          }
          result.append(')');
        }
      } else {
        result.append(frame.getSourcePath());
      }

      int lineNumber = frame.getLineNumber();
      result.append(' ');
      result.append(' ');
      if (lineNumber >= 0) {
        result.append(lineNumber);
      } else {
        if (frame.isNative()) {
          result.append(' ');
        }
      }

      if (!frame.wereLocalsAvailable()) {
        result.append(' ');
      }

      return result.toString();

    }
    return null;
  }

  private String getQualifiedName(String rec)
  {
    return rec;
  }

  /**
   * Suspends the given thread.
   */
  public void suspend(long threadId) throws DebugException {
    IThread thread = threadMap.get(threadId);
    if (thread != null) {
      thread.suspend();
    }
  }

  /**
   * Resumes execution of the given thread.
   */
  public void resume(long threadId) throws DebugException {
    IThread thread = threadMap.get(threadId);
    if (thread != null) {
      thread.resume();
    }
  }
}
