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
package org.eclim.plugin.core.util;

import java.io.InputStream;

import java.util.Map;

import org.eclim.Services;

import org.eclim.plugin.PluginResources;

import org.eclim.util.file.FileUtils;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;

/**
 * Utility classes for working with scripts.
 * <p/>
 * Currently all scripts are expected to be implemented in groovy.
 *
 * @author Eric Van Dewoestine
 */
@SuppressWarnings("rawtypes")
public class ScriptUtils
{
  private static final String SCRIPT_PATH = "/resources/scripts/";

  /**
   * Evaluates the specified script and returns the result.
   *
   * @param resources The plugin resources.
   * @param script The script path relative to the scripts directory.
   * @param values Any variable name / value pairs for the script.
   * @return The result of evaluating the supplied script.
   */
  public static Object evaluateScript(
      PluginResources resources, String script, Map<String, Object> values)
    throws Exception
  {
    Binding binding = new Binding(values);
    GroovyShell shell = new GroovyShell(binding);

    String path = FileUtils.separatorsToUnix(
        FileUtils.concat(SCRIPT_PATH, script));
    InputStream stream = resources.getResourceAsStream(path);
    if (stream == null){
      throw new IllegalArgumentException(
          Services.getMessage("script.not.found", path));
    }
    return shell.evaluate(stream);
  }

  /**
   * Searches all plugin resources for and parses the names script and returns
   * the Class that can be used to create instances to invoke methods on.
   *
   * @param script The script path relative to the scripts directory.
   * @return The resulting class.
   */
  public static Class parseClass(String script)
    throws Exception
  {
    String path = FileUtils.separatorsToUnix(
        FileUtils.concat(SCRIPT_PATH, script));
    InputStream stream = Services.getResourceAsStream(path);
    if (stream == null){
      throw new IllegalArgumentException(
          Services.getMessage("script.not.found", path));
    }
    return parseClass(stream);
  }

  /**
   * Parses the named script from the supplied PluginResources and returns the
   * Class that can be used to create instances to invoke methods on.
   *
   * @param resources The plugin resources.
   * @param script The script path relative to the scripts directory.
   * @return The resulting class.
   */
  public static Class parseClass(PluginResources resources, String script)
    throws Exception
  {
    String path = FileUtils.separatorsToUnix(
        FileUtils.concat(SCRIPT_PATH, script));
    InputStream stream = resources.getResourceAsStream(path);
    if (stream == null){
      throw new IllegalArgumentException(
          Services.getMessage("script.not.found", path));
    }
    return parseClass(stream);
  }

  /**
   * Parses the supplied script stream and returns the Class that can be used to
   * create instances to invoke methods on.
   *
   * @param stream The stream for the script.
   * @return The resulting class.
   */
  private static Class parseClass(InputStream stream)
    throws Exception
  {
    GroovyClassLoader gcl = new GroovyClassLoader();
    return gcl.parseClass(stream);
  }
}
