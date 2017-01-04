/**
 * Copyright (C) 2005 - 2016  Eric Van Dewoestine
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
package org.eclim.http;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclim.Services;
import org.eclim.command.Command;
import org.eclim.command.CommandException;
import org.eclim.command.CommandException.ErrorType;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;
import org.eclim.logging.Logger;
import org.eclipse.swt.widgets.Display;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.martiansoftware.nailgun.NGContext;

/**
 * Class which replaces the {@code org.eclim.command.Main} class when commands
 * are executed over the HTTP Interface.
 *
 * @author Lukas Roth
 *
 */
public class CommandCaller implements Runnable
{
  private static final Logger logger = Logger.getLogger(CommandCaller.class);

  private final Command command;
  private final Object[] results = new Object[1];
  private final CommandLine commandLine;

  public CommandCaller(Map<String, String> parameters,
      InputStream fileInputStream)
      throws InvalidCommandException
  {
    try {
      /**
       * We want to add the {@code fileInputStream} to the command line.
       *
       * To do so we add a mock -c parameter which we will later override with
       * the addRawOption method with the actual {@code fileInputStream}.
       *
       * If we would not add a (-c,"") pair before the parser gets called (in
       * method createCommandLine) the parser would complain that the required
       * option is not there.
       */
      if(parameters.containsKey(Options.CONTENT_OPTION)){
        throw new InvalidCommandException(Services.getMessage(
            "command.caller.file.post.not.allowed.key", Options.CONTENT_OPTION));
      }
      parameters.put(Options.CONTENT_OPTION, "");
      commandLine = createCommandLine(parameters);
      commandLine.addRawOption(Options.CONTENT_OPTION, fileInputStream);
      command = commandLine.getCommand();
      logger.debug("created command {} from commandLine {}", command, commandLine);
    } catch (Exception e) {
      throw new InvalidCommandException(
          "Could not create a command line out of " + parameters, e);
    }
  }

  public CommandCaller(Map<String, String> parameters)
      throws InvalidCommandException
  {
    try {
      commandLine = createCommandLine(parameters);
      command = commandLine.getCommand();
      logger.debug("created command {} from commandLine {}", command, commandLine);
    } catch (Exception e) {
      throw new InvalidCommandException(
          "Could not create a command line out of " + parameters, e);
    }
  }

  public EclimHTTPResponse callCommand()
      throws CommandCallerException
  {
    ByteArrayOutputStream outOutputStream = new ByteArrayOutputStream();
    ByteArrayOutputStream errOutputStream = new ByteArrayOutputStream();
    NGContext context = createMockContext(outOutputStream, errOutputStream);

    try {
      command.setContext(context);
      logger.debug("call command {}", command);
      executeCommandsInsideEclipse();
      return parseResult(results[0], commandLine, outOutputStream, errOutputStream);
    } catch (CommandCallerException e) {
      logStreams(outOutputStream, errOutputStream);
      throw e;
    }
  }

  private CommandLine createCommandLine(Map<String, String> parameters)
      throws Exception
  {
    List<String> paramList = new ArrayList<String>();
    for (Entry<String, String> entry : parameters.entrySet()) {
      // we add a '-' in front of every key such that the parser can parse our
      // parameters
      paramList.add("-" + entry.getKey());
      paramList.add(entry.getValue());
    }

    Options options = new Options();
    logger.debug("Creating a command line from arguments: " +
        Arrays.toString(paramList.toArray()));
    return options
        .parse((String[]) paramList.toArray(new String[paramList.size()]));
  }

  private void executeCommandsInsideEclipse()
  {
    Display.getDefault().syncExec(this);
  }

  /**
   * Logs the streams which were passed to into the Eclim plugin (inside the
   * NGContext). The logging order may be wrong since we can log this
   * information only in the end.
   *
   * @param outOutputStream
   * @param errOutputStream
   */
  private void logStreams(ByteArrayOutputStream outOutputStream,
      ByteArrayOutputStream errOutputStream)
  {
    String out = outOutputStream.toString();
    if (!out.isEmpty()) {
      logger.debug("Output stream passed to the commands:" + out);
    }
    String err = errOutputStream.toString();
    if (!err.isEmpty()) {
      logger.debug("Error stream passed to the commands:" + err);
    }
  }

  private EclimHTTPResponse parseResult(Object result, CommandLine commandLine,
      ByteArrayOutputStream outOutputStream, ByteArrayOutputStream errOutputStream)
      throws CommandCallerException
  {
    if (result != null) {
      String messageCommandException = "Command throwed an exception";
      if (result instanceof Exception) {
        logger.error(messageCommandException, (Exception) result);
        throw new CommandCallerException(messageCommandException,
            (Exception) result);
      } else if (result instanceof CommandException) {
        CommandException commandException = (CommandException) result;
        int statusCode = getStatusCode(commandException);
        String stringResult = (new Gson()).toJson(commandException);
        logger.error(messageCommandException, stringResult);
        return new EclimHTTPResponse(stringResult, outOutputStream.toString(),
            errOutputStream.toString(), statusCode);
      }
      GsonBuilder builder = new GsonBuilder();
      if (commandLine.hasOption(Options.PRETTY_OPTION)) {
        builder = builder.setPrettyPrinting();
      }
      String stringResult = builder.create().toJson(result);
      return new EclimHTTPResponse(stringResult, outOutputStream.toString(),
          errOutputStream.toString(), 200);
    } else {
      String msg = "System error: The result of the call to eclim is null";
      logger.error(msg);
      throw new CommandCallerException(msg);
    }
  }

  private int getStatusCode(CommandException commandException)
  {
    ErrorType errorType = commandException.getErrorType();
    if(errorType == ErrorType.SYSTEM_ERROR){
      return 500;
    }else if(errorType == ErrorType.CLIENT_ERROR){
      return 400;
    }
    return 500;
  }

  /**
   * Create a "mock" NGcontext with out- and error streams set.
   *
   * @return NGContext
   * @throws CommandCallerException
   */
  private NGContext createMockContext(ByteArrayOutputStream outputStream,
      ByteArrayOutputStream errStream)
      throws RuntimeException
  {
    try {
      // We use reflection to get the private constructor of the NGContext.
      // We did not see a nicer way to get an ngContext instance.
      Constructor<NGContext> constructor = NGContext.class
          .getDeclaredConstructor(new Class[0]);
      constructor.setAccessible(true);
      NGContext ngContext = constructor.newInstance(new Object[0]);
      ngContext.in = null;
      ngContext.out = new PrintStream(outputStream);
      ngContext.err = new PrintStream(errStream);
      return ngContext;
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      logger.error("Failed to construct a NGContext.", e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public void run()
  {
      try {
        results[0] = command.execute(commandLine);
      } catch (Exception e) {
        results[0] = e;
        logger.error("Command failed", e);
      } finally {
        command.cleanup(commandLine);
      }
  }
}