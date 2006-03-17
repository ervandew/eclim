/**
 * Copyright (c) 2004 - 2005
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclim.command;

import com.thoughtworks.xstream.XStream;

import org.apache.commons.cli.ParseException;

import org.eclim.Services;

import org.eclipse.swt.widgets.EclimDisplay;

/**
 * Entry point for client invocation.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class Main
{
  private static final String PORT_PROPERTY = "eclim.server.port";

  /**
   * Main method for executing the client.
   *
   * @param _args The command line args.
   */
  public static final void main (String[] _args)
  {
    try{
      // set dummy display's current thread
      ((EclimDisplay)org.eclipse.swt.widgets.Display.getDefault())
        .setThread(Thread.currentThread());

      CommandLine commandLine = null;
      Options options = new Options();
      try{
        commandLine = options.parse(_args);
      }catch(ParseException e){
        System.out.println(e.getMessage());
        options.usageSummary();
        System.exit(1);
      }

      if(commandLine.hasOption(options.HELP_OPTION)){
        options.usage(commandLine.getValue(options.HELP_OPTION));
        System.exit(0);
      }

      if(commandLine.hasOption(options.PORT_OPTION)){
        System.setProperty(
          PORT_PROPERTY, commandLine.getValue(options.PORT_OPTION));
      }

      String commandName = commandLine.getValue(options.COMMAND_OPTION);
      if(commandName == null || commandName.trim().equals("")){
        throw new IllegalArgumentException(
            Services.getMessage("command.required"));
      }
      Command command = (Command)Services.getService(commandName, Command.class);

      Object result = command.execute(commandLine);

      if(result instanceof String ||
         result instanceof Integer ||
         result instanceof Boolean)
      {
        System.out.println(result);
      }else if(result instanceof Throwable){
        ((Throwable)result).printStackTrace();
        System.exit(1);
      }else if(result != null){
        System.out.println(new XStream().toXML(result));
      }
    }catch(Exception e){
      e.printStackTrace();
      System.exit(1);
    }
  }
}
