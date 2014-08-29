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

import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks all active debugging sessions.
 */
public class DebuggerContextManager
{
  private static Map<String, DebuggerContext> ctxMap =
    new ConcurrentHashMap<String, DebuggerContext>();

  // TODO Temp hack until multiple debugging sessions are supported.
  public static DebuggerContext getDefault()
  {
    for (Map.Entry<String, DebuggerContext> entry : ctxMap.entrySet()) {
      return entry.getValue();
    }

    return null;
  }

  public static DebuggerContext get(String name)
  {
    return ctxMap.get(name);
  }

  public static void add(DebuggerContext ctx)
  {
    ctxMap.put(ctx.getId(), ctx);
  }

  public static void remove(String id)
  {
    ctxMap.remove(id);
  }
}
