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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;

import org.eclim.logging.Logger;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclipse.debug.core.DebugException;

import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;

import org.eclipse.jdt.debug.core.IJavaStackFrame;

/**
 * Command to get stack frames of suspended threads.
 */
@Command(
  name = "java_debug_stackframe",
  options = ""
)
public class StackFrameCommand extends AbstractCommand
{
  private static final Logger logger = Logger.getLogger(StackFrameCommand.class);

  @Override
  public Object execute(CommandLine commandLine) throws Exception
  {
    if (logger.isInfoEnabled()) {
      logger.info("Command: " + commandLine);
    }

    List<String> results = new ArrayList<String>();

    Map<IThread, IStackFrame[]> stackFrameMap = DebuggerContext.getInstance()
      .getStackFramesMap();

    if (stackFrameMap == null) {
      return results;
    }

    for (Map.Entry<IThread, IStackFrame[]> entry : stackFrameMap.entrySet()) {
      String threadName = entry.getKey().getName();
      IStackFrame[] stackFrames = entry.getValue();
      if (stackFrames == null) {
        continue;
      }

      results.add("Thread-" + threadName);

      for (IStackFrame stackFrame : stackFrames) {
        // TODO Do formatting in VIM
        results.add("  " + getStackFrameText(stackFrame));
      }

      results.add("");
    }

    return results;
  }

  private String getStackFrameText(IStackFrame stackFrame)
    throws DebugException
  {
    StringBuffer result = new StringBuffer();

    IJavaStackFrame frame = (IJavaStackFrame) stackFrame.getAdapter(
        IJavaStackFrame.class);
    if (frame != null) {
      String dec = frame.getDeclaringTypeName();
      if (logger.isDebugEnabled()) {
        logger.debug("Declaring type name = " + dec);
      }

      if (frame.isObsolete()) {
        result.append(dec);
        result.append('>');
        return result.toString();
      }

      boolean javaStratum = true;
      javaStratum = frame.getReferenceType().getDefaultStratum().equals("Java");
      if (logger.isDebugEnabled()) {
        logger.debug("Stratum = " + javaStratum);
      }

      if (javaStratum) {
        // receiver name
        String rec = frame.getReceivingTypeName();
        result.append(getQualifiedName(rec));
        if (logger.isDebugEnabled()) {
          logger.debug("Recv type name = " + rec);
        }

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
}
