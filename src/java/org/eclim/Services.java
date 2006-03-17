/**
 * Copyright (c) 2004 - 2005
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import org.eclim.plugin.PluginResources;

import org.eclim.util.spring.ResourceBundleMessageSource;

import org.springframework.context.NoSuchMessageException;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Class responsible for retrieving service implementations.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class Services
{
  private static Logger logger = Logger.getLogger(Services.class);

  private static AbstractApplicationContext context =
    new ClassPathXmlApplicationContext(
        System.getProperty("org.eclim.spring-factory.xml",
          "org/eclim/spring-factory.xml"));

  private static List pluginResources = new ArrayList();
  private static Map serviceCache = new HashMap();
  private static Map messageCache = new HashMap();

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
    Integer index = (Integer)serviceCache.get(_name);
    if(index == null){
      Iterator iterator = pluginResources.iterator();
      for(int ii = 0; iterator.hasNext(); ii++){
        PluginResources resources = (PluginResources)iterator.next();
        if(resources.containsService(_name)){
          serviceCache.put(_name, new Integer(ii));
          return resources.getService(_name, _type);
        }
      }
    }else{
      if(index.intValue() >= 0){
        PluginResources resources =
          (PluginResources)pluginResources.get(index.intValue());
        return resources.getService(_name, _type);
      }
      return context.getBean(_name, _type);
    }
    serviceCache.put(_name, new Integer(-1));
    return context.getBean(_name, _type);
  }

  /**
   * Retrieves a message for the supplied message key.
   *
   * @param _key The message key.
   *
   * @return The message.
   */
  public static String getMessage (String _key)
  {
    return getMessage(_key, null);
  }

  /**
   * Retrieves a message for the supplied message key.
   *
   * @param _key The message key.
   * @param _object Optional message arg used to format the message.
   *
   * @return The message.
   */
  public static String getMessage (String _key, Object _object)
  {
    return getMessage(_key, new Object[]{_object});
  }

  /**
   * Retrieves and optionally formats a message for the supplied message key.
   *
   * @param _key The message key.
   * @param _args Optional message args used to format the message.
   *
   * @return The formatted message.
   */
  public static String getMessage (String _key, Object[] _args)
  {
    try{
      Integer index = (Integer)messageCache.get(_key);
      if(index == null){
        Iterator iterator = pluginResources.iterator();
        for(int ii = 0; iterator.hasNext(); ii++){
          try{
            PluginResources resources = (PluginResources)iterator.next();
            String message = resources.getMessage(_key, _args);
            messageCache.put(_key, new Integer(ii));
            return message;
          }catch(NoSuchMessageException nsme){
            // message not found in this plugin.
          }
        }
      }else{
        if(index.intValue() >= 0){
          PluginResources resources =
            (PluginResources)pluginResources.get(index.intValue());
          return resources.getMessage(_key, _args);
        }
        return context.getMessage(_key, _args, Locale.getDefault());
      }
      messageCache.put(_key, new Integer(-1));
      return context.getMessage(_key, _args, Locale.getDefault());
    }catch(NoSuchMessageException nsme){
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
      for(Iterator ii = pluginResources.iterator(); ii.hasNext();){
        PluginResources resources = (PluginResources)ii.next();
        if(_plugin.equals(resources.getName())){
          return resources.getResourceBundle();
        }
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
    return ((ResourceBundleMessageSource)context.getBean("messageSource",
          ResourceBundleMessageSource.class)).getResourceBundle();
  }

  /**
   * Closes and disposes of all services.
   */
  public static void close ()
  {
    for(Iterator ii = pluginResources.iterator(); ii.hasNext();){
      PluginResources resources = (PluginResources)ii.next();
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
    pluginResources.add(_resources);
  }
}
