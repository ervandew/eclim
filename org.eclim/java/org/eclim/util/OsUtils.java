package org.eclim.util;

import java.util.Locale;

/**
 * Some OS related utility methods.
 *
 * @author Eric Van Dewoestine
 */
public class OsUtils
{
  // borrowed from ant's Os condition.
  private static final String OS_NAME =
    System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
  private static final String FAMILY_WINDOWS = "windows";

  private OsUtils ()
  {
  }

  /**
   * Determine if the current OS is Windows.
   *
   * @return true if the current OS is Windows, false otherwise.
   */
  public static boolean isWindows()
  {
    // borrowed from ant's Os condition.
    return OS_NAME.indexOf(FAMILY_WINDOWS) > -1;
  }
}
