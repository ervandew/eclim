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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringWriter;

import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import org.apache.log4j.Logger;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.VelocityContext;

import org.eclim.Services;

import org.eclim.plugin.PluginResources;

/**
 * Utility class for running evaluating templates.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class TemplateUtils
{
  private static final Logger logger =
    Logger.getLogger(TemplateUtils.class);

  private static final String TEMPLATE_ROOT = "/resources/templates/";

  static{
    try{
      // tell velocity to use log4j for all logging
      Properties properties = new Properties();
      properties.setProperty("runtime.log.logsystem.class",
          org.apache.velocity.runtime.log.SimpleLog4JLogSystem.class.getName());
      properties.setProperty("runtime.log.logsystem.log4j.category",
          "org.apache.velocity");
      properties.setProperty("directive.foreach.counter.initial.value", "0");
      properties.setProperty("resource.loader", "file");

      // stop annoying error regarding VM_global_library.vm not found.
      properties.setProperty("velocimacro.library", "");
      /*properties.setProperty("file.resource.loader.path",
          System.getProperty("eclim.home") + TEMPLATE_ROOT);*/
      Velocity.init(properties);
    }catch(Exception e){
      logger.error("", e);
    }
  }

  /**
   * Evaluates the template supplied via the specfied reader into the supplied
   * writer w/ the specified values.
   *
   * @param _resources The plugin resources.
   * @param _template The template file name.
   * @param _values The template values.
   * @return The evaluation result.
   */
  public static String evaluate (
      PluginResources _resources, String _template, Map _values)
    throws Exception
  {
    _template = TEMPLATE_ROOT + _template;
    BufferedReader reader = null;
    try{
      reader = new BufferedReader(new InputStreamReader(
            _resources.getResourceAsStream(_template)));
    }catch(NullPointerException npe){
      IllegalArgumentException iae = new IllegalArgumentException(
          Services.getMessage("template.not.found", _template));
      iae.initCause(npe);
      throw iae;
    }

    StringWriter writer = new StringWriter();
    VelocityContext context = new VelocityContext(_values);
    try{
      Velocity.evaluate(context, writer, TemplateUtils.class.getName(), reader);
    }catch(Exception e){
      throw new RuntimeException(
          Services.getMessage("template.eval.error", _template), e);
    }finally{
      IOUtils.closeQuietly(reader);
    }

    return writer.toString();
  }
}
