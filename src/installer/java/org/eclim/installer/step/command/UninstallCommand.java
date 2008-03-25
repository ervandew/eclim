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
package org.eclim.installer.step.command;

import org.apache.log4j.Logger;

/**
 * Uninstall a feature.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class UninstallCommand
  extends Command
{
  private static final Logger logger =
    Logger.getLogger(UninstallCommand.class);

  private String feature;
  private String version;

  public UninstallCommand (OutputHandler handler, String id, String version, String to)
  {
    super(handler, new String[]{
      "-command", "uninstall",
      "-featureId", id,
      "-version", version
    }, to);
    this.feature = id;
    this.version = version;
  }

  /**
   * {@inheritDoc}
   * @see Command#getReturnCode()
   */
  public int getReturnCode ()
  {
    if (super.getReturnCode() != 0){
      logger.warn("Unable to uninstall feature: " + feature + " " + version);
    }

    // don't fail on uninstall error.  later versions of eclipse may auto
    // uninstall.
    return 0;
  }
}
