/**
 * Copyright (C) 2005 - 2009  Eric Van Dewoestine
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
package org.eclim.eclipse;

import org.eclim.logging.Logger;

import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.swt.widgets.EclimDisplay;

/**
 * Eclim application implementation which runs in its own headless eclipse
 * instance.
 */
public class EclimApplicationHeadless
  extends AbstractEclimApplication
{
  private static final Logger logger =
    Logger.getLogger(EclimApplicationHeadless.class);

  /**
   * {@inheritDoc}
   * @see AbstractEclimApplication#onStart()
   */
  @Override
  public void onStart()
    throws Exception
  {
    // create the eclipse workbench.
    org.eclipse.ui.PlatformUI.createAndRunWorkbench(
        new EclimDisplay(), //org.eclipse.ui.PlatformUI.createDisplay()),
        new WorkbenchAdvisor());
  }

  /**
   * {@inheritDoc}
   * @see AbstractEclimApplication#onStop()
   */
  @Override
  public void onStop()
    throws Exception
  {
    logger.info("Saving workspace...");

    try{
      ResourcesPlugin.getWorkspace().save(true, null);
    }catch(Exception e){
      logger.warn("Error saving workspace.", e);
    }
    logger.info("Workspace saved.");
  }

  /**
   * {@inheritDoc}
   * @see AbstractEclimApplication#isHeaded()
   */
  @Override
  public boolean isHeaded()
  {
    return false;
  }
}
