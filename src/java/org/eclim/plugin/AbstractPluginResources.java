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

import java.io.File;
import java.io.InputStream;

import java.net.URL;

import java.text.MessageFormat;

import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import org.eclim.Services;

import org.eclim.command.Command;

import org.eclim.logging.Logger;

import org.eclim.util.file.FileUtils;

import org.eclipse.core.runtime.FileLocator;

/**
 * Abstract implementation of {@link PluginResources}.
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
  private String pluginName;
  private Properties properties;
  private ResourceBundle bundle;

  private HashMap<String,Class> commands =
    new HashMap<String,Class>();
  private HashMap<String,Command> instances = new HashMap<String,Command>();

  /**
   * Initializes this instance.
   *
   * @param _name The plugin name.
   */
  public void initialize (String _name)
  {
    name = _name;
    int index = name.lastIndexOf('.');
    pluginName = index != -1 ? _name.substring(index + 1) : name;
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
  public Command getCommand (String _name)
    throws Exception
  {
    Command command = instances.get(_name);
    if(command == null){
      if(!containsCommand(_name)){
        throw new RuntimeException(
            Services.getMessage("command.not.found", _name));
      }
      Class cc = commands.get(_name);
      command = (Command)cc.newInstance();
      instances.put(_name, command);
    }
    return command;
  }

  /**
   * {@inheritDoc}
   */
  public boolean containsCommand (String _name)
  {
    return commands.containsKey(_name);
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
    return getProperty(_name, null);
  }

  /**
   * {@inheritDoc}
   */
  public String getProperty (String _name, String _default)
  {
    if (properties == null){
      properties = new Properties();
      try{
        properties.load(getClass().getResourceAsStream(PLUGIN_PROPERTIES));
      }catch(Exception e){
        logger.warn(
            "Error loading plugin.properties for plugin '" + getName() + "'", e);
      }
    }
    return System.getProperty(_name, properties.getProperty(_name, _default));
  }

  /**
   * {@inheritDoc}
   */
  public URL getResource (String _resource)
  {
    try{
    // try vim resources first
    // Ex: /home/ervandew/.vim/eclim/resources/jdt/templates/logger.gst

      // inject the pluginName ("jdt", "wst", etc)
      String vimResource = _resource;
      int index = vimResource.indexOf("resources");
      if(index != -1){
        vimResource = FileUtils.concat(
            vimResource.substring(0, index + 9),
            pluginName,
            vimResource.substring(index + 9));
      }
      String file = FileUtils.concat(
          System.getProperty("vim.files"), "eclim", vimResource);
      if (new File(file).exists()){
        return new URL("file://" + FileUtils.separatorsToUnix(file));
      }

    // next try plugin resources
      URL resource = getClass().getResource(_resource);
      if (resource != null){
        // convert any eclipse specific url to a native java one.
        return FileLocator.resolve(resource);
      }

      // not found
      return null;
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public InputStream getResourceAsStream (String _resource)
  {
    try{
      URL resource = getResource(_resource);
      if (resource != null){
        return resource.openStream();
      }
      return null;
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void close ()
    throws Exception
  {
  }

  /**
   * Registers the supplied command under the specified name.
   *
   * @param _name The name of the command.
   * @param _command The command class.
   */
  protected void registerCommand (String _name, Class _command)
  {
    commands.put(_name, _command);
  }

  /**
   * Gets the base name used to lookup the plugin's ResourceBundle.
   *
   * @return The ResourceBundle base name.
   */
  protected abstract String getBundleBaseName ();
}
