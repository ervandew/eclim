/**
 * Copyright (C) 2005 - 2008  Eric Van Dewoestine
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
 * @author Eric Van Dewoestine (ervandew@gmail.com)
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
