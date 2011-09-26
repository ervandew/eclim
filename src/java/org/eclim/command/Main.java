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

import java.io.PrintStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;

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

      if (arguments.isEmpty() || arguments.contains("-?")){
        usage(context.out);
        System.exit(arguments.isEmpty() ? 1 : 0);
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

  public static void usage(PrintStream out)
  {
    ArrayList<org.eclim.annotation.Command> commands =
      new ArrayList<org.eclim.annotation.Command>();
    for (Class<? extends Command> command : Services.getCommandClasses()){
      commands.add(command.getAnnotation(org.eclim.annotation.Command.class));
    }
    Collections.sort(commands, new Comparator<org.eclim.annotation.Command>(){
      public int compare(
        org.eclim.annotation.Command o1,
        org.eclim.annotation.Command o2)
      {
        return o1.name().compareTo(o2.name());
      }
    });

    String osOpts = StringUtils.EMPTY;
    if (SystemUtils.IS_OS_UNIX){
      osOpts = " [-f eclimrc] [--nailgun-port port]";
    }
    out.println("Usage: eclim" + osOpts + " -command command [args]");
    out.println("  Available Commands:");
    for (org.eclim.annotation.Command command : commands){
      Collection<Option> options = new Options()
        .parseOptions(command.options());
      StringBuffer opts = new StringBuffer();
      Iterator<Option> iterator = options.iterator();
      for (int ii = 0; iterator.hasNext(); ii++){
        Option option = iterator.next();
        opts.append(option.isRequired() ? " " : " [");
        opts.append('-').append(option.getOpt());
        if (option.hasArg()){
          opts.append(' ').append(option.getLongOpt());
        }
        if (!option.isRequired()){
          opts.append(']');
        }
        // wrap every 4 options
        if ((ii + 1) % 4 == 0 && ii != options.size() - 1){
          opts.append(StringUtils.rightPad("\n", command.name().length() + 5));
        }
      }
      StringBuffer info = new StringBuffer()
        .append("    ").append(command.name()).append(opts);
      out.println(info);
      if (!command.description().equals(StringUtils.EMPTY)){
        out.println("      " + command.description());
      }
    }
  }
}
