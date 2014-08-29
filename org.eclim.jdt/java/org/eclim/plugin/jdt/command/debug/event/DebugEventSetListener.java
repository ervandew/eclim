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
package org.eclim.plugin.jdt.command.debug.event;

import org.eclim.logging.Logger;

import org.eclim.plugin.jdt.command.debug.context.DebuggerContext;
import org.eclim.plugin.jdt.command.debug.context.DebuggerContextManager;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.IDebugEventSetListener;

import org.eclipse.jdt.internal.debug.core.model.JDIDebugElement;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;

/**
 * Handler for events triggered during a debug session.
 */
public class DebugEventSetListener
  implements IDebugEventSetListener
{
  private static final Logger logger = Logger.getLogger(
      DebugEventSetListener.class);

  private DebugEventHandler threadEventhandler;
  private DebugEventHandler debugTargetEventhandler;

  public DebugEventSetListener()
  {
    this.threadEventhandler = new ThreadEventHandler();
    this.debugTargetEventhandler = new DebugTargetEventHandler();
  }

  public void handleDebugEvents(DebugEvent[] events)
  {
    for (DebugEvent event : events) {
      JDIDebugElement src = (JDIDebugElement) event.getSource();
      int kind = event.getKind();
      int detail = event.getDetail();

      if (logger.isDebugEnabled()) {
        logger.debug("Got event from src: " + src.getClass().getName() +
            " " + kind + " " + detail);
      }

      DebuggerContext ctx = DebuggerContextManager.getDefault();
      if (ctx == null) {
        if (logger.isDebugEnabled()) {
          logger.debug("No debugging session present");
        }
        return;
      }

      try {
        if (src instanceof JDIThread) {
          threadEventhandler.handle(ctx, src, kind, detail);
        } else if (src instanceof JDIDebugTarget) {
          debugTargetEventhandler.handle(ctx, src, kind, detail);
        }
      } catch (Exception e) {
        logger.error("Listener failed", e);
        throw new RuntimeException(e);
      }
    }
  }
}
