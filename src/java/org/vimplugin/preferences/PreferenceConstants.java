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

  /** the path to gvim */
  public static final String P_GVIM = "gvim";

  /** additional startup options */
  public static final String P_OPTS = "opts";

  /** embed vim into eclipse */
  public static final String P_EMBED = "embedded";

  /** open files in new tabs in external gvim */
  public static final String P_TABBED = "tabbed";

  /** initiate document listening events via gvim netbeans interface */
  public static final String P_DOCUMENT_LISTEN = "documentListen";

  /** use auto click to force gvim focus */
  public static final String P_FOCUS_AUTO_CLICK = "focus.click";

  /** open eclimd view automatically */
  public static final String P_START_ECLIMD = "eclimd.start";
}
