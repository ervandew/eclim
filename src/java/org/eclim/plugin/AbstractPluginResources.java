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
package org.eclim.plugin;

import java.io.InputStream;

import java.net.URL;

import java.text.MessageFormat;

import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import org.eclim.util.spring.UrlXmlApplicationContext;

import org.eclipse.core.runtime.FileLocator;

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

  private static final String PLUGIN_PROPERTIES = "/plugin.properties";

  private String name;
  private AbstractApplicationContext context;
  private Properties properties;
  private ResourceBundle bundle;

  /**
   * Initializes this instance with the resource at the supplied url.
   *
   * @param _name The plugin name.
   * @param _resource The resource url.
   */
  public void initialize (String _name, URL _resource)
  {
    name = _name;
    context = createContext(_resource);
    properties = createProperties();
  }

  /**
   * Creates the application context for locating services.
   *
   * @param _resource The url of the context configuration file.
   * @return The application context.
   */
  protected AbstractApplicationContext createContext (URL _resource)
  {
    AbstractApplicationContext context =
      new UrlXmlApplicationContext(_resource, getClass().getClassLoader());

    return context;
  }

  /**
   * Create the Properties for this instance.
   *
   * @return The Properties.
   */
  protected Properties createProperties ()
  {
    Properties properties = new Properties();
    try{
      properties.load(getClass().getResourceAsStream(PLUGIN_PROPERTIES));
    }catch(Exception e){
      logger.warn(
          "Error loading plugin.properties for plugin '" + getName() + "'", e);
    }
    return properties;
  }

  /**
   * {@inheritDoc}
   */
  public String getName ()
  {
    return name;
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
  public String getMessage (String _key, Object... _args)
  {
    ResourceBundle bundle = getResourceBundle();
    String message = bundle.getString(_key);
    return MessageFormat.format(message, _args);
  }

  /**
   * {@inheritDoc}
   */
  public ResourceBundle getResourceBundle ()
  {
    if (bundle == null){
      bundle = ResourceBundle.getBundle(
          getBundleBaseName(), Locale.getDefault(), getClass().getClassLoader());
    }
    return bundle;
  }

  /**
   * {@inheritDoc}
   */
  public String getProperty (String _name)
  {
    return System.getProperty(_name, properties.getProperty(_name));
  }

  /**
   * {@inheritDoc}
   */
  public String getProperty (String _name, String _default)
  {
    return System.getProperty(_name, properties.getProperty(_name, _default));
  }

  /**
   * {@inheritDoc}
   */
  public URL getResource (String _resource)
  {
    try{
      return FileLocator.resolve(getClass().getResource(_resource));
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public InputStream getResourceAsStream (String _resource)
  {
    return getClass().getResourceAsStream(_resource);
  }

  /**
   * {@inheritDoc}
   */
  public void close ()
  {
    context.close();
  }

  /**
   * Gets the base name used to lookup the plugin's ResourceBundle.
   *
   * @return The ResourceBundle base name.
   */
  protected abstract String getBundleBaseName ();
}
