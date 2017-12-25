/**
 * Copyright (C) 2014 - 2017 Eric Van Dewoestine
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

  /** we need a bit of extra time to read the output */
  private static final long TIMEOUT_INPUT = 480;

  /**
   * Executable to use to send commands
   */
  private final String executable;

  /**
   * VIM server instance to connect to send commands.
   */
  private final String instanceId;

  public VimClient(String instanceId)
  {
    this("vim", instanceId);
  }

  public VimClient(String executable, String instanceId)
  {
    this.instanceId = instanceId;
    this.executable = executable;
  }

  public String getId()
  {
    return instanceId;
  }

  public void remoteSend(String arg)
    throws Exception
  {
    // redraw at end to prevent "ENTER to continue"
    //  for long commands
    String[] cmd = {
      executable,
      "--servername",
      instanceId,
      "--remote-send",
      arg + "<cr> | :redraw!<cr>",
    };

    if (logger.isDebugEnabled()) {
      logger.debug("VIM command: " + Arrays.asList(cmd));
    }

    CommandExecutor.execute(cmd, TIMEOUT);
  }

  public String remoteExpr(String arg)
  {
    String[] cmd = {
      executable,
      "--servername",
      instanceId,
      "--remote-expr",
      arg,
    };

    if (logger.isDebugEnabled()) {
      logger.debug("VIM expr: " + Arrays.asList(cmd));
    }

    CommandExecutor exec = CommandExecutor.execute(cmd, TIMEOUT_INPUT);
    return exec.getResult();
  }

  public void remoteFunctionCall(String function, String... args)
    throws Exception
  {
    StringBuilder call = new StringBuilder()
      .append(":call ").append(function).append('(');
    for (int i = 0; i < args.length; i++){
      call.append('"').append(args[i]).append('"');
      if (i < args.length - 1){
        call.append(',');
      }
    }
    call.append(')');
    remoteSend(call.toString());
  }

  /* Like remoteFunctionCall but returns the result as a String */
  public String remoteFunctionExpr(String function, String...args)
  {
    StringBuilder call = new StringBuilder()
      .append(function).append('(');
    for (int i = 0; i < args.length; i++){
      call.append('"').append(args[i]).append('"');
      if (i < args.length - 1){
        call.append(',');
      }
    }
    call.append(')')
        .append(" | :redraw!<cr>"); // special for func calls
    return remoteExpr(call.toString());
  }
}
