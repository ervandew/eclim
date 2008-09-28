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
package org.eclim;

import java.io.InputStream;

import java.net.URL;

import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclim.command.Command;

import org.eclim.logging.Logger;

import org.eclim.plugin.AbstractPluginResources;
import org.eclim.plugin.PluginResources;

import org.eclim.preference.PreferenceFactory;

/**
 * Class responsible for retrieving service implementations and provides access
 * to localized messages.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class Services
{
  private static Logger logger = Logger.getLogger(Services.class);

  private static final String MAIN = "main";

  private static PluginResources defaultPluginResources;

  private static HashMap<String,PluginResources> pluginResources =
    new HashMap<String,PluginResources>();
  private static HashMap<String,String> serviceCache =
    new HashMap<String,String>();
  private static HashMap<String,String> messageCache =
    new HashMap<String,String>();

  /**
   * Gets a command by name.
   *
   * @param _name The command name.
   *
   * @return The command implementation.
   */
  public static Command getCommand (String _name)
    throws Exception
  {
    String name = (String)serviceCache.get(_name);
    if(name == null){
      for(PluginResources resources : pluginResources.values()){
        if(resources.containsCommand(_name)){
          serviceCache.put(_name, resources.getName());
          return resources.getCommand(_name);
        }
      }
    }else{
      if(!MAIN.equals(name)){
        PluginResources resources = pluginResources.get(name);
        return resources.getCommand(_name);
      }
      return getPluginResources().getCommand(_name);
    }
    serviceCache.put(_name, MAIN);
    return getPluginResources().getCommand(_name);
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
      ((DefaultPluginResources)defaultPluginResources).initialize("org.eclim");
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
      try{
        resources.close();
      }catch(Exception e){
        logger.error(
            "Error closing plugin: " + resources.getClass().getName(), e);
      }
      logger.info("{} closed.", resources.getClass().getName());
    }
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
     * @see AbstractPluginResources#initialize(String)
     */
    @Override
    public void initialize (String _name)
    {
      super.initialize(_name);

      PreferenceFactory.addPreferences("core",
        "General org.eclim.user.name\n" +
        "General org.eclim.user.email\n" +
        "General/Project org.eclim.project.version 1.0\n" +
        "General/Project org.eclim.project.copyright\n" +
        "General/Project org.eclim.project.tracker\n" +
        "General/Project org.eclim.project.vcs.web.viewer viewvc (viewvc|trac|redmine|hgcgi|hgserve|gitweb)\n" +
        "General/Project org.eclim.project.vcs.web.url"
      );

      registerCommand("ping", org.eclim.command.admin.PingCommand.class);
      registerCommand("shutdown",
          org.eclim.command.admin.ShutdownCommand.class);
      registerCommand("workspace_dir",
          org.eclim.command.eclipse.WorkspaceCommand.class);

      registerCommand("project_create",
          org.eclim.command.project.ProjectCreateCommand.class);
      registerCommand("project_delete",
          org.eclim.command.project.ProjectDeleteCommand.class);
      registerCommand("project_refresh",
          org.eclim.command.project.ProjectRefreshCommand.class);
      registerCommand("project_info",
          org.eclim.command.project.ProjectInfoCommand.class);
      registerCommand("project_open",
          org.eclim.command.project.ProjectOpenCommand.class);
      registerCommand("project_close",
          org.eclim.command.project.ProjectCloseCommand.class);
      registerCommand("project_nature_aliases",
          org.eclim.command.project.ProjectNatureAliasesCommand.class);
      registerCommand("project_update",
          org.eclim.command.project.ProjectUpdateCommand.class);
      registerCommand("project_list",
          org.eclim.command.project.ProjectListCommand.class);
      registerCommand("project_settings",
          org.eclim.command.project.ProjectSettingsCommand.class);
      registerCommand("project_natures",
          org.eclim.command.project.ProjectNaturesCommand.class);
      registerCommand("project_nature_add",
          org.eclim.command.project.ProjectNatureAddCommand.class);
      registerCommand("project_nature_remove",
          org.eclim.command.project.ProjectNatureRemoveCommand.class);
      registerCommand("settings_update",
          org.eclim.command.admin.SettingsUpdateCommand.class);
      registerCommand("settings",
          org.eclim.command.admin.SettingsCommand.class);

      registerCommand("taglist",
          org.eclim.command.taglist.TaglistCommand.class);

      registerCommand("archive_list",
          org.eclim.command.archive.ArchiveListCommand.class);
      registerCommand("archive_list_all",
          org.eclim.command.archive.ArchiveListAllCommand.class);
      registerCommand("archive_read",
          org.eclim.command.archive.ArchiveReadCommand.class);

      registerCommand("patch_revisions",
          org.eclim.command.patch.RevisionsCommand.class);
      registerCommand("patch_file",
          org.eclim.command.patch.PatchFileCommand.class);

      registerCommand("xml_format",
          org.eclim.command.xml.format.FormatCommand.class);
      registerCommand("xml_validate",
          org.eclim.command.xml.validate.ValidateCommand.class);
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
