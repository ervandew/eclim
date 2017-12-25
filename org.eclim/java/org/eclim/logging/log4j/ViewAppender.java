/**
 * Copyright (C) 2005 - 2017  Eric Van Dewoestine
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;

import org.apache.log4j.spi.LoggingEvent;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

/**
 * Appender for logging messages to an eclipse view if the view is open and has
 * a static accessor (getLog) for a Text widget.
 *
 * @author Eric Van Dewoestine
 */
public class ViewAppender
  extends AppenderSkeleton
{
  private Method logAccessor;

  /**
   * Sets the class name of the view to log to.
   *
   * @param view The fully qualified class name.
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  public void setView(String view)
  {
    try{
      Class viewClass = Class.forName(view);
      logAccessor = viewClass.getMethod("getLog");
    }catch(ClassNotFoundException cnfe){
      throw new RuntimeException(cnfe);
    }catch(NoSuchMethodException nsme){
      throw new RuntimeException(nsme);
    }
  }

  @Override
  protected void append(final LoggingEvent event)
  {
    try{
      final Text log = (Text)logAccessor.invoke(null);
      if (log != null && !log.isDisposed()){
        Display.getDefault().asyncExec(new Runnable(){
          public void run()
          {
            write(log, event);
          }
        });
      }
    }catch(IllegalAccessException iae){
      throw new RuntimeException(iae);
    }catch(InvocationTargetException ite){
      throw new RuntimeException(ite);
    }
  }

  private void write(Text log, LoggingEvent event)
  {
    if (!log.isDisposed()){
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
  }

  @Override
  public boolean requiresLayout()
  {
    return true;
  }

  @Override
  public void close()
  {
  }
}
