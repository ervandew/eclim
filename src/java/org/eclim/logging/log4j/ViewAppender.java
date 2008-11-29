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
package org.eclim.logging.log4j;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;

import org.apache.log4j.spi.LoggingEvent;

import org.eclim.eclipse.headed.EclimdView;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

/**
 * Appender for logging messages to the EclimdView log if the view is open.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class ViewAppender
  extends AppenderSkeleton
{
  /**
   * {@inheritDoc}
   * @see AppenderSkeleton#append(LoggingEvent)
   */
  @Override
  protected void append (final LoggingEvent event)
  {
    final Text log = EclimdView.getLog();
    if (log != null && !log.isDisposed()){
      Display.getDefault().asyncExec(new Runnable(){
        public void run (){
          write(log, event);
        }
      });
    }
  }

  private void write (Text log, LoggingEvent event){
    log.append(layout.format(event));

    if(layout.ignoresThrowable()) {
      String[] s = event.getThrowableStrRep();
      if (s != null) {
        int len = s.length;
        for(int i = 0; i < len; i++) {
          log.append(s[i]);
          log.append(Layout.LINE_SEP);
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   * @see AppenderSkeleton#requiresLayout()
   */
  public boolean requiresLayout ()
  {
    return true;
  }

  /**
   * {@inheritDoc}
   * @see org.apache.log4j.Appender#close()
   */
  public void close ()
  {
  }
}
