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
import java.util.HashSet;

import org.eclipse.core.runtime.Platform;
import org.vimplugin.editors.AbstractVimEditor;
import org.vimplugin.preferences.PreferenceConstants;

/**
 * Abstract class that implements as much as of the Vim Server functions as
 * possible so that VimServer and VimServerNewWindow can hopefully be combined
 * eventually to one class or at least reduced to very tiny class which just
 * extend this class in a trivial manner.
 */
public class VimServer {

  /**
   * the id of this instance. IDs are counted in
   * {@link org.vimplugin.VimPlugin#nextServerID Vimplugin}.
   */
  private final int ID;

  /**
   * The editors associated with the vim instance. For same window opening.
   */
  private HashSet<AbstractVimEditor> editors = new HashSet<AbstractVimEditor>();

  /**
   * Initialise the class.
   *
   * @param instanceID The ID for this VimServer.
   */
  public VimServer(int instanceID) {
    ID = instanceID;
  }

  /**
   * The Vim process.
   */
  protected Process p;

  /**
   * The thread used to communicate with vim (runs {@link #vc}).
   */
  protected Thread t;

  /**
   * Used to communicate with vim.
   */
  protected VimConnection vc = null;

  /**
   * @return The {@link VimConnection} Used to communicate with this Vim
   *         instance
   */
  public VimConnection getVc() {
    return vc;
  }

  /**
   * Gives the vim argument with the port depending on the portID.
   *
   * @param portID
   * @return The argument for vim for starting the Netbeans interface.
   */
  protected String getNetbeansString(int portID) {

    int port = VimPlugin.getDefault().getPreferenceStore().getInt(
        PreferenceConstants.P_PORT)
        + portID;
    String host = VimPlugin.getDefault().getPreferenceStore().getString(
        PreferenceConstants.P_HOST);
    String pass = VimPlugin.getDefault().getPreferenceStore().getString(
        PreferenceConstants.P_PASS);

    return "-nb:" + host + ":" + port + ":" + pass;
  }

  /**
   * Get netbeans-port,host and pass and start vim with -nb option.
   *
   */
  public void start() {
    String gvim = VimPlugin.getDefault().getPreferenceStore().getString(
        PreferenceConstants.P_GVIM);
    String arg0 = getNetbeansString(ID);

    start(gvim, arg0);
  }

  /**
   * Start vim and embed it in the Window with the <code>wid</code> (platform-dependent!) given.
   *
   * @param wid The id of the window to embed vim into
   */
  public void start(long wid) {

    // gather Strings (nice names for readbility)
    String gvim = VimPlugin.getDefault().getPreferenceStore().getString(
        PreferenceConstants.P_GVIM);

    String netbeans = getNetbeansString(ID);
    String dontfork = "-f"; // foreground -- dont fork

    // Platform specific code
    String socketid = "--socketid";
    // use --windowid, under win32
    if (Platform.getOS().equals(Platform.OS_WIN32)) {
      socketid = "--windowid";
    }

    String stringwid = String.valueOf(wid);
    String[] addopts = VimPlugin.getDefault().getPreferenceStore()
        .getString(PreferenceConstants.P_OPTS).split("\\s");

    // build args-array (dynamic size due to addopts.split)
    String[] args = new String[5 + addopts.length];
    args[0] = gvim;
    args[1] = netbeans;
    args[2] = dontfork;
    args[3] = socketid;
    args[4] = stringwid;

    // copy addopts to args
    System.arraycopy(addopts, 0, args, 5, addopts.length);

    start(args);
  }

  /**
   * start gvim with args using a ProcessBuilder and setup
   * {@link #vc VimConnection} .
   *
   * @param args
   */
  public void start(String... args) {
    if (vc != null && vc.isServerRunning())
      return;

    // setup VimConnection and start server thread
    vc = new VimConnection(ID);
    t = new Thread(vc);
    t.setUncaughtExceptionHandler(new VimExceptionHandler());
    t.setDaemon(true);
    t.start();

    // starting gvim with Netbeans interface
    try {
      System.out.println("Trying to start vim");
      p = new ProcessBuilder(args).start();
      System.out.println("Started vim");
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Waits until server starts.. vim should return startupDone
    while (!vc.isServerRunning()) {
      // sleep so that we don't have a messy cpu-hogging infinite loop
      // here
      Long stoptime = 2000L; // 2 Seconds
      System.out.println("Waiting to connect to vim server…");
      try {
        Thread.sleep(stoptime);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Stops the server.. closes the vimconnection
   *
   * @return Success
   */
  public boolean stop() throws IOException {
    boolean result = false; // If error raised

    result = vc.close();

    vc = null;
    t.interrupt();
    p.destroy();
    return result;
  }

  /**
   * Simple setter.
   *
   * @param editors the editors to set
   */
  public void setEditors(HashSet<AbstractVimEditor> editors) {
    this.editors = editors;
  }

  /**
   * Simple getter.
   *
   * @return the editors
   */
  public HashSet<AbstractVimEditor> getEditors() {
    return editors;
  }

  /**
   * Gets an {@link AbstractVimEditor} by the vim buffer-id.
   *
   * @param bufid the id to lookup
   * @return the corresponding editor or null if none is found.
   */
  public AbstractVimEditor getEditor(int bufid) {
    for (AbstractVimEditor veditor : getEditors()) {
      if (veditor.getBufferID() == bufid) {
        return veditor;
      }
    }
    return null;
  }
}
