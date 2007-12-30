/**
 * Copyright (c) 2005 - 2008
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

import java.io.File;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;

import org.eclim.logging.Logger;

import org.eclim.util.CommandExecutor;
import org.eclim.util.ScriptUtils;

/**
 * Command to generate taglist from a file.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class TaglistCommand
  extends AbstractCommand
{
  private static final Logger logger = Logger.getLogger(TaglistCommand.class);

  private static final String LANGUAGE = "--language-force";
  private static final String SORT = "--sort";
  private static final String CTAGS_OPTION = "c";
  private static final long MAX_FILE_SIZE = 500 * 1024;

  private static final Map<String,TaglistScript> scriptCache =
    new HashMap<String,TaglistScript>();

  /**
   * {@inheritDoc}
   */
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    String[] args = _commandLine.getArgs();
    String file = args[args.length - 1];

    // check file first
    File theFile = new File(file);
    if(!theFile.exists() || theFile.length() > MAX_FILE_SIZE){
      logger.debug(
          "File '{}' not processed: exists = {} size = " + theFile.length(),
          file, Boolean.valueOf(theFile.exists()));
      return "";
    }

    String ctags = _commandLine.getValue(CTAGS_OPTION);
    String lang = null;
    boolean sort = false;

    String[] ctagArgs = new String[args.length - 3];
    ctagArgs[0] = ctags;
    for (int ii = 0; ii < args.length; ii++){
      // first four args are for this command.
      if(ii > 3){
        ctagArgs[ii - 3] = args[ii];
      }

      if(args[ii].startsWith(LANGUAGE)){
        lang = args[ii].substring(args[ii].indexOf('=') + 1);
      }else if(args[ii].startsWith(SORT)){
        if("yes".equals(args[ii].substring(args[ii].indexOf('=') + 1))){
          sort = true;
        }
      }
    }

    TaglistScript script = (TaglistScript)scriptCache.get(lang);
    if(!scriptCache.containsKey(lang) && script == null){
      try{
        Class scriptClass = ScriptUtils.parseClass(
            "taglist/" + lang + ".groovy");
        script = (TaglistScript)scriptClass.newInstance();
// After some extended period of time groovy starts losing the ability to
// resolve eclim classes.  Until this is resolved, don't cache groovy scripts.
// If not a groovy issue, may be an issue with eclipse classloaders.
//          scriptCache.put(lang, script);
      }catch(IllegalArgumentException iae){
        // script not found.
        logger.debug("No taglist script found for '" + lang + "'", iae);
        scriptCache.put(lang, null);
      }
    }

    if(script != null){
      TagResult[] results = script.execute(file);
      if(sort){
        Arrays.sort(results);
      }
      return TaglistFilter.instance.filter(_commandLine, results);
    }
    return executeCtags(ctagArgs);
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
    CommandExecutor process = CommandExecutor.execute(_args, 10000);
    if(process.getReturnCode() == -1){
      process.destroy();
      throw new RuntimeException("ctags command timed out.");
    }else if(process.getReturnCode() > 0){
      throw new RuntimeException("ctags error: " + process.getErrorMessage());
    }

    return process.getResult().trim();
  }
}
