/**
 * Copyright (C) 2005 - 2014 Eric Van Dewoestine
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
package org.eclim.plugin.core.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclim.logging.Logger;

import org.eclim.util.CommandExecutor;

/**
 * Client to interact with VIM server instance.
 */
public class VimClient
{
  private static final Logger logger =
    Logger.getLogger(VimClient.class);

  private static final long TIMEOUT = 60;

  /**
   * VIM server instance to connect to send commands.
   */
  private final String instanceId;

  public VimClient(String instanceId)
  {
    this.instanceId = instanceId;
  }

  public String getId()
  {
    return instanceId;
  }

  private void remoteSend(String arg)
    throws Exception
  {
    String[] cmd = {
      "vim",
      "--servername",
      instanceId,
      "--remote-send",
      arg + "<CR>",
    };

    if (logger.isDebugEnabled()) {
      logger.debug("VIM command: " + Arrays.asList(cmd));
    }

    CommandExecutor.execute(cmd, TIMEOUT);
  }

  public void jumpToFilePosition(String fileName, int lineNum)
    throws Exception
  {
    remoteSend(":JavaDebugGoToFile " + fileName + " " + lineNum);
  }

  public void refreshDebugStatus()
    throws Exception
  {
      remoteSend(":JavaDebugStatus");
  }

  public void signalSessionTermination()
    throws Exception
  {
    remoteSend(":JavaDebugSessionTerminated");
  }

  public void updateThreadView(long threadId, String kind, List<String> results)
    throws Exception
  {
    remoteSend(":JavaDebugThreadViewUpdate " + threadId + " " +
        kind + " " + concatenateList(results));
  }

  public void updateVariableView(List<String> results)
    throws Exception
  {
    remoteSend(":JavaDebugVariableViewUpdate " +
        concatenateList(results));
  }

  /**
   * Returns a string by concatenating all the given entries using <eol> as
   * delimiter.
   */
  private String concatenateList(List<String> entries)
  {
    if (entries == null || entries.isEmpty()) {
      // Return a placeholder string since the remote VIM command expects an arg
      return "-";
    }

    StringBuilder sb = new StringBuilder();
    Iterator<String> iter = entries.iterator();
    while (iter.hasNext()) {
      sb.append(iter.next().replaceAll(" ", "\\\\ "));
      sb.append("<eol>");
    }

    return sb.toString();
  }
}
