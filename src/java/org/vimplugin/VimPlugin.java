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

import java.io.IOException;
import java.util.HashMap;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class VimPlugin extends AbstractUIPlugin {

  /**
   * The shared instance.
   */
  private static VimPlugin plugin;

  /**
   * ID of the default Vim instance.
   */
  public static final int DEFAULT_VIMSERVER_ID = 0;

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
  private final HashMap<Integer, VimServer> vimServers = new HashMap<Integer, VimServer>();

  /**
   * The constructor.
   */
  public VimPlugin() {
    plugin = this;
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
      MessageDialog.openError(getWorkbench().getActiveWorkbenchWindow().getShell(), "Vimplugin", "VimServer to stop not found.");
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
}
