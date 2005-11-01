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

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
  private static Log log = LogFactory.getLog(Services.class);
  private static AbstractApplicationContext context =
    new ClassPathXmlApplicationContext(
        System.getProperty("org.eclim.spring-factory.xml",
          "org/eclim/spring-factory.xml"));

  /**
   * Checks if the service exists.
   *
   * @param _type The service type.
   * @return true if the services exists, false otherwise.
   */
  public static boolean containsService (Class _type)
  {
    return containsService(_type.getName());
  }

  /**
   * Checks if the service exists.
   *
   * @param _name The service name.
   * @return true if the services exists, false otherwise.
   */
  public static boolean containsService (String _name)
  {
    return context.containsBean(_name);
  }

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
    return context.getBean(_name, _type);
  }

  /**
   * Gets all registered service names.
   *
   * @param _class The service type to get all names of.
   * @return Array of service names.
   */
  public static String[] getAllServiceNames (Class _class)
  {
    return context.getBeanNamesForType(_class);
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
      return context.getMessage(_key, _args, Locale.getDefault());
    }catch(NoSuchMessageException nsme){
      return _key;
    }
  }

  /**
   * Gets the underlying resource bundle used for messages.
   *
   * @return The ResourceBundle.
   */
  public static ResourceBundle getResourceBundle ()
  {
    return ((ResourceBundleMessageSource)getService("messageSource",
          ResourceBundleMessageSource.class)).getResourceBundle();
  }

  /**
   * Closes and disposes of all services.
   */
  public static void close ()
  {
    context.close();
    log.info(Services.class.getName() + " closed.");
  }
}
