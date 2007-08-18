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
package org.eclim;

import java.io.InputStream;

import java.net.URL;

import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import org.eclim.plugin.AbstractPluginResources;
import org.eclim.plugin.PluginResources;

import org.eclim.preference.PreferenceFactory;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Class responsible for retrieving service implementations and provides access
 * to localized messages.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class Services
{
  private static Logger logger = Logger.getLogger(Services.class);

  private static final String MAIN = "main";

  private static AbstractApplicationContext context =
    new ClassPathXmlApplicationContext(
        System.getProperty("org.eclim.spring-factory.xml",
          "org/eclim/spring-factory.xml"));

  private static PluginResources defaultPluginResources;

  private static HashMap<String,PluginResources> pluginResources =
    new HashMap<String,PluginResources>();
  private static HashMap<String,String> serviceCache =
    new HashMap<String,String>();
  private static HashMap<String,String> messageCache =
    new HashMap<String,String>();

  /**
   * Gets a service by type.
   *
   * @param _type The service type.
   *
   * @return The service implementation.
   */
  public static Object getService (Class _type)
  {
    return getService(_type.getName(), _type);
  }

  /**
   * Gets a service by name and type.
   *
   * @param _name The service name.
   * @param _type The service type.
   *
   * @return The service implementation.
   */
  public static Object getService (String _name, Class _type)
  {
    String name = (String)serviceCache.get(_name);
    if(name == null){
      for(PluginResources resources : pluginResources.values()){
        if(resources.containsService(_name)){
          serviceCache.put(_name, resources.getName());
          return resources.getService(_name, _type);
        }
      }
    }else{
      if(!MAIN.equals(name)){
        PluginResources resources = (PluginResources)pluginResources.get(name);
        return resources.getService(_name, _type);
      }
      return context.getBean(_name, _type);
    }
    serviceCache.put(_name, MAIN);
    return context.getBean(_name, _type);
  }

  /**
   * Retrieves and optionally formats a message for the supplied message key.
   *
   * @param _key The message key.
   * @param _args Optional message args used when formatting the message.
   *
   * @return The formatted message.
   */
  public static String getMessage (String _key, Object... _args)
  {
    try{
      String name = (String)messageCache.get(_key);
      if(name == null){
        for(PluginResources resources : pluginResources.values()){
          try{
            String message = resources.getMessage(_key, _args);
            messageCache.put(_key, resources.getName());
            return message;
          }catch(MissingResourceException nsme){
            // message not found in this plugin.
          }
        }
      }else{
        if(!MAIN.equals(name)){
          PluginResources resources =
            (PluginResources)pluginResources.get(name);
          return resources.getMessage(_key, _args);
        }
        return getPluginResources().getMessage(_key, _args);
      }
      messageCache.put(_key, MAIN);
      return getPluginResources().getMessage(_key, _args);
    }catch(MissingResourceException nsme){
      return _key;
    }
  }

  /**
   * Gets the underlying resource bundle used for messages.
   *
   * @param _plugin The plugin to get the resources for (ant, jdt, etc.).
   *
   * @return The ResourceBundle.
   */
  public static ResourceBundle getResourceBundle (String _plugin)
  {
    if(_plugin != null){
      PluginResources resources = (PluginResources)pluginResources.get(_plugin);
      if(resources != null){
        return resources.getResourceBundle();
      }
    }
    return getResourceBundle();
  }

  /**
   * Gets the underlying resource bundle used for messages.
   * <p/>
   * Gets the resource bundle for the main eclim plugin.
   *
   * @return The ResourceBundle.
   */
  public static ResourceBundle getResourceBundle ()
  {
    return getPluginResources().getResourceBundle();
  }

  /**
   * Gets a resource by searching the available plugins for it.
   *
   * @param _resource The resource to find.
   * @return The URL of the resource or null if not found.
   */
  public static URL getResource (String _resource)
  {
    for(PluginResources resources : pluginResources.values()){
      URL url = resources.getResource(_resource);
      if(url != null){
        return url;
      }
    }
    return getPluginResources().getResource(_resource);
  }

  /**
   * Gets a resource stream by searching the available plugins for it.
   *
   * @param _resource The resource to find.
   * @return The resource stream or null if not found.
   */
  public static InputStream getResourceAsStream (String _resource)
  {
    for(PluginResources resources : pluginResources.values()){
      InputStream stream = resources.getResourceAsStream(_resource);
      if(stream != null){
        return stream;
      }
    }
    return getPluginResources().getResourceAsStream(_resource);
  }

  /**
   * Gets the PluginResources for the main eclim plugin.
   *
   * @return The PluginResources.
   */
  public static PluginResources getPluginResources ()
  {
    if(defaultPluginResources == null){
      defaultPluginResources = new DefaultPluginResources();
      ((DefaultPluginResources)defaultPluginResources)
        .initialize("org.eclim", null);
    }
    return defaultPluginResources;
  }

  /**
   * Gets the PluginResources for the plugin with the specified name.
   *
   * @param _plugin The plugin name.
   * @return The PluginResources or null if none found.
   */
  public static PluginResources getPluginResources (String _plugin)
  {
    PluginResources resources = (PluginResources)pluginResources.get(_plugin);
    if(resources == null){
      throw new IllegalArgumentException(
          Services.getMessage("plugin.resources.not.found", _plugin));
    }
    return resources;
  }

  /**
   * Closes and disposes of all services.
   */
  public static void close ()
  {
    for(PluginResources resources : pluginResources.values()){
      resources.close();
      logger.info("{} closed.", resources.getClass().getName());
    }
    context.close();
    logger.info("{} closed.", Services.class.getName());
  }

  /**
   * Adds the supplied PluginResources instance to the list of instances that
   * are used to locate services, messages, etc.
   *
   * @param _resources The PluginResources to add.
   */
  public static void addPluginResources (PluginResources _resources)
  {
    pluginResources.put(_resources.getName(), _resources);
  }

  /**
   * Implementation of PluginResources for the main eclim plugin.
   */
  private static class DefaultPluginResources
    extends AbstractPluginResources
  {
    /**
     * {@inheritDoc}
     * @see AbstractPluginResources#initialize(String,URL)
     */
    @Override
    public void initialize (String _name, URL _resource)
    {
      super.initialize(_name, _resource);

      PreferenceFactory.addPreferences("core",
        "General org.eclim.user.name\n" +
        "General org.eclim.user.email\n" +
        "General/Project org.eclim.project.version 1.0\n" +
        "General/Project org.eclim.project.copyright\n" +
        "General/Project org.eclim.project.vcs.viewvc"
      );
    }

    protected AbstractApplicationContext createContext (URL _resource)
    {
      return context;
    }

    /**
     * {@inheritDoc}
     * @see AbstractPluginResources#getBundleBaseName()
     */
    protected String getBundleBaseName ()
    {
      return "org/eclim/messages";
    }
  }
}
