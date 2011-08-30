/**
 * Copyright (C) 2005 - 2011  Eric Van Dewoestine
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
package org.eclim.command;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.cli.ParseException;

import org.apache.commons.lang.StringUtils;

import org.eclim.Services;

import org.eclim.eclipse.AbstractEclimApplication;

import org.eclim.logging.Logger;

import org.eclipse.swt.widgets.EclimDisplay;

import com.martiansoftware.nailgun.NGContext;

/**
 * Entry point for client invocation.
 *
 * @author Eric Van Dewoestine
 */
public class Main
{
  private static final Logger logger = Logger.getLogger(Main.class);

  /**
   * Main method for executing the client.
   *
   * @param context The nailgun context.
   */
  public static final void nailMain(NGContext context)
  {
    try{
      logger.debug("args: " + Arrays.toString(context.getArgs()));

      if (!AbstractEclimApplication.getInstance().isHeaded()){
        // set dummy display's current thread
        ((EclimDisplay)org.eclipse.swt.widgets.Display.getDefault())
          .setThread(Thread.currentThread());
      }

      ArrayList<String> arguments = new ArrayList<String>();
      for(String arg : context.getArgs()){
        if(arg.startsWith("-D")){
          String[] prop = StringUtils.split(arg.substring(2), '=');
          System.setProperty(prop[0], prop[1]);
        }else{
          arguments.add(arg);
        }
      }

      CommandLine commandLine = null;
      Options options = new Options();
      try{
        commandLine = options.parse(
            (String[])arguments.toArray(new String[arguments.size()]));
      }catch(ParseException e){
        context.out.println(
            Services.getMessage(e.getClass().getName(), e.getMessage()));
        logger.debug("Main - exit on error");
        System.exit(1);
      }

      String commandName = commandLine.getValue(Options.COMMAND_OPTION);
      logger.debug("Main - command: {}", commandName);
      if(commandName == null || commandName.trim().equals(StringUtils.EMPTY)){
        throw new IllegalArgumentException(
            Services.getMessage("command.required"));
      }
      Command command = commandLine.getCommand();
      command.setContext(context);

      String result = command.execute(commandLine);
      context.out.println(result);
    }catch(Exception e){
      logger.debug("Command triggered exception: " + Arrays.toString(context.getArgs()), e);
      e.printStackTrace(context.err);

      logger.debug("Main - exit on error");
      System.exit(1);
    }
  }
}
