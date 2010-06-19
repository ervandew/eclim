/*
 * Vimplugin
 *
 * Copyright (c) 2007 - 2010 by The Vimplugin Project.
 *
 * Released under the GNU General Public License
 * with ABSOLUTELY NO WARRANTY.
 *
 * See the file COPYING for more information.
 */
package org.vimplugin;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.eclim.logging.Logger;

import org.eclim.util.CommandExecutor;

import org.eclipse.core.runtime.Platform;

import org.vimplugin.editors.VimEditor;

import org.vimplugin.preferences.PreferenceConstants;

/**
 * Class that implements as much as of the Vim Server functions as possible so
 * that VimServer and VimServerNewWindow can hopefully be combined eventually to
 * one class or at least reduced to very tiny class which just extend this class
 * in a trivial manner.
 */
public class VimServer
{
  private static final Logger logger = Logger.getLogger(VimServer.class);

  /**
   * the id of this instance. IDs are counted in
   * {@link org.vimplugin.VimPlugin#nextServerID Vimplugin}.
   */
  private final int ID;

  /**
   * The editors associated with the vim instance. For same window opening.
   */
  private HashSet<VimEditor> editors = new HashSet<VimEditor>();

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

  public int getID() {
    return ID;
  }

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

    return "-nb::" + port;
  }

  /**
   * Get netbeans-port,host and pass and start vim with -nb option.
   *
   * @param workingDir
   * @param filePath
   * @param tabbed
   * @param first
   */
  public void start(
      String workingDir, String filePath, boolean tabbed, boolean first)
  {
    String gvim = VimPlugin.getDefault().getPreferenceStore().getString(
        PreferenceConstants.P_GVIM);
    String[] addopts = getUserArgs();
    String[] args = null;

    if (!tabbed || first){
      args = new String[4 + addopts.length];
      // NOTE: for macvim, the --servername arg must be before the netbeans arg
      args[0] = gvim;
      args[1] = "--servername";
      args[2] = String.valueOf(ID);
      args[3] = getNetbeansString(ID);
      System.arraycopy(addopts, 0, args, 4, addopts.length);

      start(workingDir, (tabbed && !first), args);
    }else{
      args = new String[5 + addopts.length];
      args[0] = gvim;
      args[1] = "--servername";
      args[2] = String.valueOf(ID);
      args[3] = "--remote-tab";
      args[4] = filePath;
      System.arraycopy(addopts, 0, args, 5, addopts.length);

      start(workingDir, (tabbed && !first), args);

      // wait on file to finish opening
      // on windows we need to use vim.exe instead of gvim.exe otherwise popups
      // will be generated.
      String vim = gvim;
      if (Platform.getOS().equals(Platform.OS_WIN32)){
        vim = gvim.replace("gvim.exe", "vim.exe");
      }
      args = new String[5];
      args[0] = vim;
      args[1] = "--servername";
      args[2] = String.valueOf(ID);
      args[3] = "--remote-expr";
      args[4] = "bufname('%')";

      int tries = 0;
      while(tries < 5){
        try{
          String result = CommandExecutor.execute(args, 1000).getResult().trim();
          if(filePath.equals(result)){
            break;
          }
          Thread.sleep(500);
          tries++;
        }catch(Exception e){
          logger.error("Error waiting on vim tab to open:", e);
        }
      }
    }
  }

  /**
   * Start vim and embed it in the Window with the <code>wid</code>
   * (platform-dependent!) given.
   *
   * @param workingDir
   * @param wid The id of the window to embed vim into
   */
  public void start(String workingDir, long wid) {

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
    String[] addopts = getUserArgs();

    // build args-array (dynamic size due to addopts.split)
    String[] args = new String[7 + addopts.length];
    args[0] = gvim;
    args[1] = netbeans;
    args[2] = dontfork;
    args[3] = socketid;
    args[4] = stringwid;
    args[5] = "--servername";
    args[6] = String.valueOf(ID);

    // copy addopts to args
    System.arraycopy(addopts, 0, args, 7, addopts.length);

    start(workingDir, false, args);
  }

  /**
   * start gvim with args using a ProcessBuilder and setup
   * {@link #vc VimConnection} .
   *
   * @param args
   */
  private void start(String workingDir, boolean tabbed, String... args) {
    if (!tabbed && vc != null && vc.isServerRunning()){
      return;
    }

    VimPlugin plugin = VimPlugin.getDefault();

    if (vc == null || !vc.isServerRunning()){
      vc = new VimConnection(ID);
      t = new Thread(vc);
      t.setUncaughtExceptionHandler(new VimExceptionHandler());
      t.setDaemon(true);
      t.start();
    }

    try {
      logger.debug("Trying to start vim");
      logger.debug(Arrays.toString(args));
      ProcessBuilder builder = new ProcessBuilder(args);
      /*java.util.Map<String, String> env = builder.environment();
      env.put("SPRO_GVIM_DEBUG", "/tmp/netbeans.log");
      env.put("SPRO_GVIM_DLEVEL", "0xffffffff");*/
      if (workingDir != null){
        builder.directory(new File(workingDir));
      }

      p = builder.start();
      logger.debug("Started vim");
    } catch (IOException e) {
      logger.error("error:", e);
    }

    // Waits until server starts.. vim should return startupDone
    long maxTime = System.currentTimeMillis() + 10000L; // 10 seconds
    while (!vc.isServerRunning()) {
      if (System.currentTimeMillis() >= maxTime){
        try{
          vc.close();
        }catch(Exception e){
          logger.error("error:", e);
        }
        String message = plugin.getMessage(
            "gvim.startup.failed",
            plugin.getMessage("gvim.startupDone.event"));
        throw new RuntimeException(message);
      }

      // sleep so that we don't have a messy cpu-hogging infinite loop
      // here
      long stoptime = 2000L; // 2 Seconds
      logger.debug("Waiting to connect to vim server");
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

    if (p != null){
      // give the process some time to finish up before destroying it.
      Thread waiter = new Thread(){
        public void run(){
          try{
            logger.debug("Waiting on vim to exit normally...");
            p.waitFor();
          }catch(InterruptedException ie){
            // ignore
          }
        }
      };
      waiter.start();

      try{
        waiter.join(10000); // wait up to 10 seconds.
      }catch(InterruptedException ie){
        // ignore
      }
      p.destroy();
      logger.debug("Vim closed.");
    }

    if (vc != null){
      result = vc.close();
      vc = null;
    }

    if (t != null){
      t.interrupt();
    }

    return result;
  }

  /**
   * Simple setter.
   *
   * @param editors the editors to set
   */
  public void setEditors(HashSet<VimEditor> editors) {
    this.editors = editors;
  }

  /**
   * Simple getter.
   *
   * @return the editors
   */
  public HashSet<VimEditor> getEditors() {
    return editors;
  }

  /**
   * Gets an {@link VimEditor} by the vim buffer-id.
   *
   * @param bufid the id to lookup
   * @return the corresponding editor or null if none is found.
   */
  public VimEditor getEditor(int bufid) {
    for (VimEditor veditor : getEditors()) {
      if (veditor.getBufferID() == bufid) {
        return veditor;
      }
    }
    return null;
  }

  /**
   * Gets the user supplied gvim arguments from the preferences.
   *
   * @return Array of arguments to be passed to gvim.
   */
  protected String[] getUserArgs(){
    String opts = VimPlugin.getDefault().getPreferenceStore()
      .getString(PreferenceConstants.P_OPTS);

    // FIXME: doesn't currently handle escaped spaces/quotes
    char[] chars = opts.toCharArray();
    char quote = ' ';
    StringBuffer arg = new StringBuffer();
    ArrayList<String> args = new ArrayList<String>();
    for(char c : chars){
      if (c == ' ' && quote == ' '){
        if (arg.length() > 0){
          args.add(arg.toString());
          arg = new StringBuffer();
        }
      }else if (c == '"' || c == '\''){
        if (quote != ' ' && c == quote){
          quote = ' ';
        }else if (quote == ' '){
          quote = c;
        }else{
          arg.append(c);
        }
      }else{
        arg.append(c);
      }
    }

    if (arg.length() > 0){
      args.add(arg.toString());
    }

    return args.toArray(new String[args.size()]);
  }
}
