/**
 * Copyright (C) 2005 - 2010  Eric Van Dewoestine
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

import org.eclipse.core.internal.resources.Workspace;

import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.Platform;

import org.eclipse.osgi.service.datalocation.Location;

import org.eclipse.swt.widgets.EclimDisplay;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

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
  public boolean onStart()
    throws Exception
  {
    Location data = Platform.getInstanceLocation();
    if (data == null || !data.isSet()){
      logger.error("No workspace location set.");
      return false;
    }

    if (!data.lock()) {
      logger.error(
          "Unable to lock the workspace. Check that you have write " +
          "permissions and that no other eclipse/eclimd instance is " +
          "currently using the workspace.");
      return false;
    }

    // it would be nice to write the workspace version info at this point, but
    // according to org.eclipse.ui.internal.ide.application.IDEApplication, it's
    // not crucial.

    // create the eclipse workbench.
    PlatformUI.createAndRunWorkbench(
        new EclimDisplay(), //PlatformUI.createDisplay()),
        new WorkbenchAdvisor());

    return true;
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
      Workspace workspace = (Workspace)ResourcesPlugin.getWorkspace();
      if (workspace != null){
        workspace.save(true, null);
      }
      logger.info("Workspace saved.");
    }catch(IllegalStateException ise){
      logger.warn(ise.getMessage());
    }catch(Exception e){
      logger.warn("Error saving workspace.", e);
    }

    final IWorkbench workbench = PlatformUI.getWorkbench();
    if (workbench != null){
      // set dummy display's current thread
      EclimDisplay display = (EclimDisplay)
        org.eclipse.swt.widgets.Display.getDefault();
      display.setThread(Thread.currentThread());
      logger.info("Closing workbench...");
      workbench.close();
      logger.info("Workbench closed.");
    }
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
