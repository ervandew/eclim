/**
 * Copyright (c) 2005 - 2006
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
package org.eclim.command.taglist;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import org.apache.log4j.Logger;

import org.eclim.Services;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.util.ScriptUtils;

/**
 * Command to generate taglist from a file.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class TaglistCommand
  extends AbstractCommand
{
  private static final Logger logger = Logger.getLogger(TaglistCommand.class);

  private static final String LANGUAGE = "--language-force";
  private static final String CTAGS_OPTION = "c";

  private static final Map scriptCache = new HashMap();

  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
    throws IOException
  {
    try{
      String ctags = _commandLine.getValue(CTAGS_OPTION);
      String[] args = _commandLine.getArgs();
      String file = args[args.length - 1];
      String lang = null;

      String[] ctagArgs = new String[args.length - 3];
      ctagArgs[0] = ctags;
      for (int ii = 0; ii < args.length; ii++){
        // first four args are for this command.
        if(ii > 3){
          ctagArgs[ii - 3] = args[ii];
        }

        if(args[ii].startsWith(LANGUAGE)){
          lang = args[ii].substring(args[ii].indexOf('=') + 1);
        }
      }

      TaglistScript script = (TaglistScript)scriptCache.get(lang);
      if(!scriptCache.containsKey(lang) && script == null){
        try{
          Class scriptClass = ScriptUtils.parseClass(
              Services.getPluginResources(), "taglist/" + lang + ".groovy");
          script = (TaglistScript)scriptClass.newInstance();
          scriptCache.put(lang, script);
        }catch(IllegalArgumentException iae){
          // script not found.
          logger.debug("No taglist script found for '" + lang + "'", iae);
          scriptCache.put(lang, null);
        }
      }

      if(script != null){
        return getFilter("vim").filter(script.execute(file));
      }
      return executeCtags(ctagArgs);
    }catch(Exception e){
      return e;
    }
  }

  /**
   * Executes the ctags command and returns the result.
   *
   * @param _args The arguments for the command.
   * @return The result.
   */
  private String executeCtags (String[] _args)
    throws Exception
  {
    Ctags process = new Ctags(_args);
    Thread thread = new Thread(process);
    thread.start();

    long timeout = System.currentTimeMillis() + 10000;
    // wait for the thread to end.
    while(process.getReturnCode() == -1 && System.currentTimeMillis() < timeout){
      Thread.sleep(50);
    }

    if(process.getReturnCode() == -1){
      throw new RuntimeException("ctags command timed out.");
    }else if(process.getReturnCode() > 0){
      throw new RuntimeException("ctags error: " + process.getErrorMessage());
    }

    return process.getResult();
  }

  /**
   * Thread to run the external ctags process.
   */
  private class Ctags
    implements Runnable
  {
    private int returnCode = -1;
    private String[] args;
    private String result;
    private String error;

    /**
     * Construct a new instance.
     */
    public Ctags (String[] _args)
    {
      args = _args;
    }

    /**
     * Run the thread.
     */
    public void run ()
    {
      try{
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(args);

        returnCode = process.waitFor();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(process.getInputStream(), out);

        result = out.toString();

        out = new ByteArrayOutputStream();
        IOUtils.copy(process.getErrorStream(), out);

        error = out.toString();
      }catch(Exception e){
        returnCode = 12;
        error = e.getMessage();
        logger.error("run()", e);
      }
    }

    /**
     * Gets the output of the command.
     *
     * @return The command result.
     */
    public String getResult ()
    {
      return result;
    }

    /**
     * Get the return code from the process.
     *
     * @return The return code.
     */
    public int getReturnCode ()
    {
      return returnCode;
    }

    /**
     * Gets the error message from the command if there was one.
     *
     * @return The possibly empty error message.
     */
    public String getErrorMessage ()
    {
      return error;
    }
  }
}
