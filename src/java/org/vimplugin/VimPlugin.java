/*
 * Vimplugin
 *
 * Copyright (c) 2007 by The Vimplugin Project.
 *
 * Released under the GNU General Public License
 * with ABSOLUTELY NO WARRANTY.
 *
 * See the file COPYING for more information.
 */
package org.vimplugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.text.MessageFormat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import org.eclim.logging.Logger;

import org.eclim.util.CommandExecutor;
import org.eclim.util.IOUtils;

import org.eclipse.core.runtime.Platform;

import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.swt.widgets.Display;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.osgi.framework.BundleContext;

import org.vimplugin.editors.VimEditorPartListener;

import org.vimplugin.preferences.PreferenceConstants;

/**
 * The main plugin class to be used in the desktop.
 */
public class VimPlugin
  extends AbstractUIPlugin
{
  private static final Logger logger = Logger.getLogger(VimPlugin.class);

  private static final String GVIM_FEATURE_TEST =
    "redir! > <file> | silent! <command> | quit";
  private static final String EMBED_COMMAND_UNIX = "echo v:version >= 700";
  private static final String EMBED_COMMAND_WINDOWS =
    "echo v:version > 701 || (v:version == 701 && has('patch091'))";
  private static final String NB_COMMAND = "echo has('netbeans_intg')";

  /**
   * The shared instance.
   */
  private static VimPlugin plugin;

  /**
   * ID of the default Vim instance.
   */
  public static final int DEFAULT_VIMSERVER_ID = 0;

  private Boolean nbSupported;
  private Boolean embedSupported;

  private VimEditorPartListener partListener;

  /**
   * Returns the shared instance.
   *
   * @return the default plugin instance
   */
  public static VimPlugin getDefault() {
    return plugin;
  }

  /**
   * Returns an image descriptor for the image file at the given plug-in
   * relative path.
   *
   * @param path the path
   * @return the image descriptor
   */
  public static ImageDescriptor getImageDescriptor(String path) {
    return AbstractUIPlugin.imageDescriptorFromPlugin("VimNB", path);
  }

  /**
   * Counts number of instances of vimServerNewWindow
   */
  private int nextServerID;

  /**
   * Counts number of total buffers opened so far. If we close one buffer this
   * value doesn't change.
   */
  private int numberOfBuffers;

  /**
   * Counts number of commands executed so far. Will be useful for checking
   * functions and replies.
   */
  private int seqNo;

  /**
   * Store all the vim instances using their id as the key.
   */
  private final HashMap<Integer, VimServer> vimServers =
    new HashMap<Integer, VimServer>();

  /**
   * Properties instance for plugin.properties
   */
  private Properties properties;

  /**
   * ResourceBundle containing vimplugin messages.
   */
  private ResourceBundle messages;

  /**
   * The constructor.
   */
  public VimPlugin() {
    plugin = this;

    properties = new Properties();
    try{
      properties.load(getClass().getResourceAsStream("/plugin.properties"));
    }catch(IOException ioe){
      MessageDialog.openError(
          getWorkbench().getActiveWorkbenchWindow().getShell(),
          "Vimplugin", "Unable to load plugin.properties");
      ioe.printStackTrace();
    }

    messages = ResourceBundle.getBundle(
        "org/vimplugin/messages",
        Locale.getDefault(),
        getClass().getClassLoader());
  }

  /**
   * Creates a {@link VimServer} for each open action.
   *
   * @return the server instance
   */
  public int getDefaultVimServer() {
    return createVimServer(DEFAULT_VIMSERVER_ID);
  }

  /**
   * Creates a VimServer.
   *
   * @return The VimServer ID.
   */
  public int createVimServer() {
    return createVimServer(nextServerID++);
  }

  /**
   * Create a new VimServer with the ID Specified. If a VimServer with the ID
   * specified already exists, then don't do anything.
   *
   * @param id ID to use for the new VimServer.
   * @return ID of the new VimServer.
   */
  private int createVimServer(int id) {
    if (!vimServers.containsKey(id)) {
      VimServer vimserver = new VimServer(id);
      vimServers.put(id, vimserver);
    }
    return id;
  }

  /**
   * Stops the VimServer specified.
   *
   * @param id The ID of the VimServer to stop.
   * @return Success.
   */
  public boolean stopVimServer(int id) {
    boolean b = false;
    try {
      b = getVimserver(id).stop();
      vimServers.remove(id);
    } catch (IOException ioe) {
      MessageDialog.openError(
          getWorkbench().getActiveWorkbenchWindow().getShell(),
          "Vimplugin", "VimServer to stop not found.");
      ioe.printStackTrace();
    }
    return b;
  }

  /**
   * Returns VimServer with the id specified.
   *
   * @param id The ID of the VimServer.
   * @return The VimServer with the ID specified.
   */
  public VimServer getVimserver(int id) {
    return vimServers.get(id);
  }

  /**
   * starts the plugin.
   *
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    nextServerID = 1; // 0 is for the DEFAULT VimServer
    numberOfBuffers = 1; // Vim starts buffer count from 1
    seqNo = 0;

    partListener = new VimEditorPartListener();
    Display.getDefault().asyncExec(new Runnable(){
      public void run()
      {
        IWorkbench workbench = PlatformUI.getWorkbench();
        workbench.getActiveWorkbenchWindow()
          .getActivePage().addPartListener(partListener);
      }
    });
  }

  /**
   * stop the plugin.
   *
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(BundleContext context) throws Exception {
    super.stop(context);
    plugin = null;

    IWorkbench workbench = PlatformUI.getWorkbench();
    workbench.getActiveWorkbenchWindow()
      .getActivePage().removePartListener(partListener);
  }

  /**
   * increment {@link #seqNo} by one.
   * @return the next sequence Number.
   */
  public int nextSeqNo() {
    return seqNo++;
  }

  /**
   * Simple Setter.
   * @param numberOfBuffers the numberOfBuffers to set
   */
  public void setNumberOfBuffers(int numberOfBuffers) {
    this.numberOfBuffers = numberOfBuffers;
  }

  /**
   * Simple Getter.
   * @return the numberOfBuffers
   */
  public int getNumberOfBuffers() {
    return numberOfBuffers;
  }

  /**
   * Determines if the configured gvim path exists.
   *
   * @return true if the configured gvim path exists, false otherwise.
   */
  public boolean gvimAvailable() {
    String gvim = VimPlugin.getDefault().getPreferenceStore()
      .getString(PreferenceConstants.P_GVIM);
    File file = new File(gvim);
    if (file.exists()){
      return true;
    }
    return false;
  }

  /**
   * Determines if the configured gvim instance supports embedding.
   *
   * @return true if embedding is supported, false otherwise.
   */
  public boolean gvimEmbedSupported() {
    if (embedSupported == null){
      logger.debug("Checking gvim for embed support.");
      String command = EMBED_COMMAND_UNIX;
      if (Platform.getOS().equals(Platform.OS_WIN32)) {
        command = EMBED_COMMAND_WINDOWS;
      }
      embedSupported = new Boolean(testGvimFeature(command));
    }
    return embedSupported.booleanValue();
  }

  /**
   * Determines if the configured gvim instance supports the required netbeans
   * interface.
   *
   * @return true if netbeans supported, false otherwise.
   */
  public boolean gvimNbSupported() {
    if (nbSupported == null){
      logger.debug("Checking gvim for netbeans support.");
      nbSupported = new Boolean(testGvimFeature(NB_COMMAND));
    }
    return nbSupported.booleanValue();
  }

  public void resetGvimState() {
    embedSupported = null;
    nbSupported = null;
  }

  /**
   * Gets the partListener used to monitor (un)focus of vim editors.
   *
   * @return The partListener.
   */
  public VimEditorPartListener getPartListener()
  {
    return this.partListener;
  }

  private boolean testGvimFeature(String command) {
    String gvim = VimPlugin.getDefault().getPreferenceStore()
      .getString(PreferenceConstants.P_GVIM);
    try{
      File tempFile = File.createTempFile("eclim_gvim", null);
      command = GVIM_FEATURE_TEST.replaceFirst("<command>", command);
      command = command.replaceFirst("<file>",
          tempFile.getAbsolutePath().replace('\\', '/').replaceAll(" ", "\\ "));

      String[] cmd = {
        gvim, "-f", "-X", "-u", "NONE", "-U", "NONE", "--cmd", command};
      logger.debug(Arrays.toString(cmd));
      CommandExecutor process = CommandExecutor.execute(cmd, 5000);
      if(process.getReturnCode() != 0){
        logger.error("Failed to execute gvim: " + process.getErrorMessage());
        return false;
      }

      FileInputStream in = null;
      try{
        String result = IOUtils.toString(in = new FileInputStream(tempFile));
        result = result.trim();
        logger.debug("gvim feature supported: " + result);

        return result.equals("1");
      }catch(IOException ioe){
        logger.error("Unable to read temp file.", ioe);
        IOUtils.closeQuietly(in);
        return false;
      }
    }catch(Exception e){
      logger.error("Unable to execute gvim.", e);
      return false;
    }
  }

  /**
   * Gets the specified property from plugin.properties.
   *
   * @param name The property name.
   * @return The property value or null if not found.
   */
  public String getProperty(String name){
    return properties.getProperty(name);
  }

  /**
   * Gets the specified property from plugin.properties.
   *
   * @param name The property name.
   * @param def Default value if property is not found.
   * @return The property value or def if not found.
   */
  public String getProperty(String name, String def){
    return properties.getProperty(name, def);
  }

  /**
   * Used to obtain a message from the plugin's resource bundle.
   *
   * Supports optional var args which will be used to format the message via
   * MessageFormat.
   *
   * @param key The message key.
   * @param args Optional arguments to format the message with.
   * @return The message.
   */
  public String getMessage(String key, Object... args)
  {
    String message = messages.getString(key);
    if (args != null && args.length > 0){
      return MessageFormat.format(message, args);
    }
    return message;
  }
}
