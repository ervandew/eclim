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
package org.eclim.eclipse.headed;

import org.eclim.eclipse.AbstractEclimApplication;
import org.eclim.eclipse.EclimApplicationHeaded;

import org.eclim.logging.Logger;

import org.eclipse.equinox.app.IApplication;

import org.eclipse.swt.SWT;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import org.eclipse.ui.part.ViewPart;

/**
 * Eclipse view which when open, runs the eclimd server providing eclipse
 * functionality to vim instances.
 *
 * @author Anton Sharonov
 * @author Eric Van Dewoestine
 */
public class EclimdView
  extends ViewPart
{
  private static final Logger logger =
    Logger.getLogger(EclimdView.class);

  private static Text log;

  private EclimThread eclimThread;
  private AbstractEclimApplication application;

  /**
   * {@inheritDoc}
   * @see ViewPart#createPartControl(Composite)
   */
  @Override
  public void createPartControl(Composite parent)
  {
    log = new Text(
        parent,
        SWT.LEFT | SWT.MULTI | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL);

    try {
      application = EclimApplicationHeaded.class.newInstance();
      eclimThread = new EclimThread(application);
      eclimThread.start();
    }catch(Exception ex) {
      logger.error("Error starting eclimd.", ex);
    }
  }

  /**
   * {@inheritDoc}
   * @see ViewPart#dispose()
   */
  @Override
  public void dispose()
  {
    if (eclimThread != null) {
      try{
        new Thread(){
          public void run(){
            try{
              while(application.isStarting()){
                Thread.sleep(500);
              }
            }catch(Exception e){
              e.printStackTrace();
            }
            application.stop();
          }
        }.start();
      }catch(Exception ex){
        logger.error("Failed to shutdown eclimd.", ex);
      }
    }
  }

  /**
   * {@inheritDoc}
   * @see ViewPart#setFocus()
   */
  @Override
  public void setFocus()
  {
    log.setFocus();
  }

  public static Text getLog()
  {
    return log;
  }

  private class EclimThread
    extends Thread
  {
    IApplication application;

    public EclimThread (IApplication application)
    {
      this.application = application;
    }

    public void run()
    {
      try {
        application.start(null);
      } catch (Exception e) {
        logger.error("Error starting eclimd", e);
      }
    }
  }
}
