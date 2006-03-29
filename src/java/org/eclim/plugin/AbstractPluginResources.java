/**
 * Copyright (c) 2004 - 2006
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
package org.eclim.plugin;

import java.net.URL;

import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import org.eclim.util.spring.ResourceBundleMessageSource;
import org.eclim.util.spring.UrlXmlApplicationContext;

import org.springframework.context.support.AbstractApplicationContext;

/**
 * Abstract implementation of {@link PluginResources} that uses spring to manage
 * the services and messages.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public abstract class AbstractPluginResources
  implements PluginResources
{
  private static final Logger logger =
    Logger.getLogger(AbstractPluginResources.class);

  private static final String MESSAGE_SOURCE = "messageSource";
  private static final String PLUGIN_PROPERTIES = "/plugin.properties";
  private AbstractApplicationContext context;
  private Properties properties;

  /**
   * Initializes this instance with the resource at the supplied url.
   *
   * @param _resource The resource url.
   */
  public void initialize (URL _resource)
  {
    context = new UrlXmlApplicationContext(_resource, getClass().getClassLoader());
    ResourceBundleMessageSource messages = ((ResourceBundleMessageSource)getService(
          MESSAGE_SOURCE, ResourceBundleMessageSource.class));
    messages.setClassLoader(getClass().getClassLoader());

    properties = new Properties();
    try{
      properties.load(getClass().getResourceAsStream(PLUGIN_PROPERTIES));
    }catch(Exception e){
      logger.warn(
          "Error loading plugin.properties for plugin '" + getName() + "'", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public Object getService (String _name, Class _type)
  {
    return context.getBean(_name, _type);
  }

  /**
   * {@inheritDoc}
   */
  public boolean containsService (String _name)
  {
    return context.containsBean(_name);
  }

  /**
   * {@inheritDoc}
   */
  public String getMessage (String _key, Object[] _args)
  {
    return context.getMessage(_key, _args, Locale.getDefault());
  }

  /**
   * {@inheritDoc}
   */
  public ResourceBundle getResourceBundle ()
  {
    return ((ResourceBundleMessageSource)getService(MESSAGE_SOURCE,
          ResourceBundleMessageSource.class)).getResourceBundle();
  }

  /**
   * {@inheritDoc}
   */
  public String getProperty (String _name)
  {
    return properties.getProperty(_name);
  }

  /**
   * {@inheritDoc}
   */
  public String getProperty (String _name, String _default)
  {
    return properties.getProperty(_name, _default);
  }

  /**
   * {@inheritDoc}
   */
  public void close ()
  {
    context.close();
  }
}
