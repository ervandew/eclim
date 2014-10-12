/**
 * Copyright (C) 2012 - 2013  Eric Van Dewoestine
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

import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * Startup class invoked by eclipse after the workbench is available.
 * This class handles starting eclimd when running eclipse in headless mode.
 *
 * @author Eric Van Dewoestine
 */
public class EclimStartup
  implements IStartup
{
  private static final Logger logger = Logger.getLogger(EclimStartup.class);

  @Override
  public void earlyStartup()
  {
    logger.debug("EclimApplication enabled: {}", EclimApplication.isEnabled());
    if (EclimApplication.isEnabled()){
      final IWorkbench workbench = PlatformUI.getWorkbench();
      logger.debug("Creating thread to start EclimDaemon...");
      workbench.getDisplay().asyncExec(new Runnable() {
        public void run() {
          try {
            // counter act apache felix ThreadPrintStream behavior which gives
            // each thread a different PrintStream, but those streams don't seem
            // to be writing to the console for some reason.
            System.setOut(EclimApplication.stdout);
            System.setErr(EclimApplication.stderr);
            new Thread(){
              public void run(){
                logger.debug("Starting EclimDaemon...");
                EclimDaemon.getInstance().start();
              }
            }.start();
          }catch(Exception ex) {
            logger.error("Error starting eclimd.", ex);
          }
        }
      });
    }
  }
}
