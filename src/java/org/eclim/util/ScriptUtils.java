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
package org.eclim.util;

import java.io.InputStream;

import java.util.Map;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;

import org.eclim.Services;

import org.eclim.plugin.PluginResources;

import org.eclim.util.file.FileUtils;

/**
 * Utility classes for working with scripts.
 * <p/>
 * Currently all scripts are expected to be implemented in groovy.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ScriptUtils
{
  private static final String SCRIPT_PATH = "/resources/scripts/";

  /**
   * Evaluates the specified script and returns the result.
   *
   * @param _resources The plugin resources.
   * @param _script The script path relative to the scripts directory.
   * @param _values Any variable name / value pairs for the script.
   * @return The result of evaluating the supplied script.
   */
  public static Object evaluateScript (
      PluginResources _resources, String _script, Map<String,Object> _values)
    throws Exception
  {
    Binding binding = new Binding(_values);
    GroovyShell shell = new GroovyShell(binding);

    String script = FileUtils.separatorsToUnix(
        FileUtils.concat(SCRIPT_PATH, _script));
    InputStream stream = _resources.getResourceAsStream(script);
    if (stream == null){
      throw new IllegalArgumentException(
          Services.getMessage("script.not.found", script));
    }
    return shell.evaluate(stream);
  }

  /**
   * Searches all plugin resources for and parses the names script and returns
   * the Class that can be used to create instances to invoke methods on.
   *
   * @param _script The script path relative to the scripts directory.
   * @return The resulting class.
   */
  public static Class parseClass (String _script)
    throws Exception
  {
    String script = FileUtils.separatorsToUnix(
        FileUtils.concat(SCRIPT_PATH, _script));
    InputStream stream = Services.getResourceAsStream(script);
    if (stream == null){
      throw new IllegalArgumentException(
          Services.getMessage("script.not.found", script));
    }
    return parseClass(stream);
  }

  /**
   * Parses the named script from the supplied PluginResources and returns the
   * Class that can be used to create instances to invoke methods on.
   *
   * @param _resources The plugin resources.
   * @param _script The script path relative to the scripts directory.
   * @return The resulting class.
   */
  public static Class parseClass (PluginResources _resources, String _script)
    throws Exception
  {
    String script = FileUtils.separatorsToUnix(
        FileUtils.concat(SCRIPT_PATH, _script));
    InputStream stream = _resources.getResourceAsStream(script);
    if (stream == null){
      throw new IllegalArgumentException(
          Services.getMessage("script.not.found", script));
    }
    return parseClass(stream);
  }

  /**
   * Parses the supplied script stream and returns the Class that can be used to
   * create instances to invoke methods on.
   *
   * @param _stream The stream for the script.
   * @return The resulting class.
   */
  private static Class parseClass (InputStream _stream)
    throws Exception
  {
    GroovyClassLoader gcl = new GroovyClassLoader();
    return gcl.parseClass(_stream);
  }
}
