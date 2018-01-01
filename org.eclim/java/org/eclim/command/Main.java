/**
 * Copyright (C) 2005 - 2017  Eric Van Dewoestine
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

import java.lang.reflect.Type;

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

import org.eclim.logging.Logger;

import org.eclipse.swt.widgets.Display;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

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
  public static final void nailMain(final NGContext context)
  {
    try{
      logger.debug("args: " + Arrays.toString(context.getArgs()));

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
        int index = arguments.indexOf("-?");
        String cmd = arguments.size() > index + 1 ? arguments.get(index + 1) : null;
        usage(cmd, context.out);
        System.exit(arguments.isEmpty() ? 1 : 0);
      }

      Options options = new Options();
      final CommandLine commandLine = options.parse(
          (String[])arguments.toArray(new String[arguments.size()]));

      String commandName = commandLine.getValue(Options.COMMAND_OPTION);
      logger.debug("Main - command: {}", commandName);

      final Command command = commandLine.getCommand();
      command.setContext(context);

      final Object[] results = new Object[1];
      Display.getDefault().syncExec(new Runnable(){
        public void run() {
          try{
            results[0] = command.execute(commandLine);
          }catch(RuntimeException re){
            Throwable cause = re.getCause();
            results[0] = cause != null ? cause : re;
            logger.error("Command failed", re);
          }catch(Exception e){
            results[0] = e;
            logger.error("Command failed", e);
          }finally{
            command.cleanup(commandLine);
          }
        }
      });
      Object result = results[0];

      if (result != null){
        if (result instanceof Throwable){
          throw (Throwable)result;
        }
        GsonBuilder builder = new GsonBuilder();
        if (commandLine.hasOption(Options.PRETTY_OPTION)){
          builder = builder.setPrettyPrinting();
        }
        if (commandLine.hasOption(Options.EDITOR_OPTION) &&
            commandLine.getValue(Options.EDITOR_OPTION).equals("vim"))
        {
          builder = builder
            .registerTypeAdapter(Boolean.TYPE, new BooleanSerializer())
            .registerTypeAdapter(Boolean.class, new BooleanSerializer());
        }
        context.out.println(builder.create().toJson(result));
      }
    }catch(ParseException pe){
      context.out.println(
          Services.getMessage(pe.getClass().getName(), pe.getMessage()));
      logger.debug("Main - exit on error");
      System.exit(1);
    }catch(Throwable t){
      logger.debug("Command triggered exception: " +
          Arrays.toString(context.getArgs()), t);
      t.printStackTrace(context.err);

      logger.debug("Main - exit on error");
      System.exit(1);
    }
  }

  public static void usage(String cmd, PrintStream out)
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

    boolean cmdFound = cmd == null;
    if (cmd == null){
      String osOpts = StringUtils.EMPTY;
      if (SystemUtils.IS_OS_UNIX){
        osOpts = " [-f eclimrc] [--nailgun-port port]";
      }
      out.println("Usage: eclim" + osOpts + " -command command [args]");
      out.println("  To view a full list of available commands:");
      out.println("    eclim -? commands");
      out.println("  To view info for a specific command:");
      out.println("    eclim -? <command_name>");
      out.println("  Ex.");
      out.println("    eclim -? project_create");
    }else if(cmd.equals("commands")){
      out.println("Available Commands:");
    }else{
      out.println("Requested Command:");
    }

    for (org.eclim.annotation.Command command : commands){
      if (cmd == null || (!cmd.equals(command.name()) && !cmd.equals("commands"))){
        continue;
      }
      cmdFound = true;

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

    if (!cmdFound){
      out.println("    No Such Command: " + cmd);
    }
  }

  private static class BooleanSerializer
    implements JsonSerializer<Boolean>
  {
    public JsonElement serialize(
        Boolean bool, Type typeOfSrc, JsonSerializationContext context)
    {
      // vim doesn't have a boolean type, so use an int.
      return new JsonPrimitive(bool.booleanValue() ? 1 : 0);
    }
  }
}
