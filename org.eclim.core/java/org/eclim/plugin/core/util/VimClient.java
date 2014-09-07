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

  public void jumpToFilePosition(String fileName, int lineNum)
    throws Exception
  {
    String[] cmd = {
      "vim",
      "--servername",
      instanceId,
      "--remote-send",
      ":JavaDebugGoToFile " + fileName + " " + lineNum + "<CR>",
    };


    if (logger.isDebugEnabled()) {
      logger.debug("Jumping to file: " + Arrays.asList(cmd));
    }

    CommandExecutor.execute(cmd, TIMEOUT);
  }

  public void refreshDebugStatus()
    throws Exception
  {
    String[] cmd = {
      "vim",
      "--servername",
      instanceId,
      "--remote-send",
      ":JavaDebugStatus<CR>",
    };

    if (logger.isDebugEnabled()) {
      logger.debug("Refreshing debug status: " + Arrays.asList(cmd));
    }

    CommandExecutor.execute(cmd, TIMEOUT);
  }
}
