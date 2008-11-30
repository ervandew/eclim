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
package org.vimplugin.preferences;

/**
 * Constant definitions for plug-in preferences.
 */
public class PreferenceConstants {

  /** the port vim listens on */
  public static final String P_PORT = "port";

  /** the host vim runs on */
  public static final String P_HOST = "host";

  /** the password vim was started with */
  public static final String P_PASS = "pass";

  /** the path to gvim */
  public static final String P_GVIM = "gvim";

  /** additional startup options */
  public static final String P_OPTS = "opts";

  /** turn on debug to std-out */
  public static final String P_DEBUG = "debug";

  /** embed vim into eclipse */
  public static final String P_EMBD = "embedded";

  /** HotKeys */
  public static final String[] P_KEYS = {"hotkey1","hotkey2","hotkey3","hotkey4","hotkey5"};

  /** Commands */
  public static final String[] P_COMMANDS = {"command1","command2","command3","command4","command5"};
}
