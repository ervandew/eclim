/**
 * Copyright (C) 2005 - 2009  Eric Van Dewoestine
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
 * @author Eric Van Dewoestine
 */
public class TaglistCommand
  extends AbstractCommand
{
  private static final Logger logger = Logger.getLogger(TaglistCommand.class);

  private static final String LANGUAGE = "--language-force";
  private static final String SORT = "--sort";
  private static final String CTAGS_OPTION = "c";
  private static final long MAX_FILE_SIZE = 500 * 1024;

  private static final Map<String, TaglistScript> scriptCache =
    new HashMap<String, TaglistScript>();

  /**
   * {@inheritDoc}
   */
  public String execute(CommandLine commandLine)
    throws Exception
  {
    String[] args = commandLine.getArgs();
    String file = args[args.length - 1];

    // check file first
    File theFile = new File(file);
    if(!theFile.exists() || theFile.length() > MAX_FILE_SIZE){
      logger.debug(
          "File '{}' not processed: exists = {} size = " + theFile.length(),
          file, Boolean.valueOf(theFile.exists()));
      return "";
    }

    String ctags = commandLine.getValue(CTAGS_OPTION);
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
      return TaglistFilter.instance.filter(commandLine, results);
    }
    return executeCtags(ctagArgs);
  }

  /**
   * Executes the ctags command and returns the result.
   *
   * @param args The arguments for the command.
   * @return The result.
   */
  private String executeCtags(String[] args)
    throws Exception
  {
    CommandExecutor process = CommandExecutor.execute(args, 10000);
    if(process.getReturnCode() == -1){
      process.destroy();
      throw new RuntimeException("ctags command timed out.");
    }else if(process.getReturnCode() > 0){
      throw new RuntimeException("ctags error: " + process.getErrorMessage());
    }

    return process.getResult().trim();
  }
}
