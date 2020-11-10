/**
 * Copyright (C) 2005 - 2020  Eric Van Dewoestine
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

import java.io.Serializable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;

import org.apache.logging.log4j.core.appender.AbstractAppender;

import org.apache.logging.log4j.core.config.Property;

import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import org.eclipse.ui.part.ViewPart;

/**
 * Appender for logging messages to an eclipse view if the view is open and has
 * a static accessor (getLog) for a Text widget.
 *
 * @author Eric Van Dewoestine
 */
@Plugin(
  name = EclipseViewAppender.PLUGIN_NAME,
  category = Core.CATEGORY_NAME,
  elementType = Appender.ELEMENT_TYPE,
  printObject = true)
public class EclipseViewAppender
  extends AbstractAppender
{
  public static final String PLUGIN_NAME = "EclipseView";

  private Method logAccessor;

  private EclipseViewAppender(
      final String name,
      final String view,
      final Layout<? extends Serializable> layout)
  {
    super(name, null, layout, true, Property.EMPTY_ARRAY);

    try{
      Class<? extends ViewPart> viewClass =
        Class.forName(view).asSubclass(ViewPart.class);
      logAccessor = viewClass.getMethod("getLog");
    }catch(
        ClassNotFoundException |
        NoSuchMethodException ex)
    {
      throw new RuntimeException(ex);
    }
  }

  @PluginFactory
  public static EclipseViewAppender createAppender(
      @PluginAttribute(value = "name") final String name,
      @PluginAttribute(value = "view") final String view,
      @PluginElement("Layout") final Layout<? extends Serializable> layout)
  {
    return new EclipseViewAppender(name, view, layout);
  }

  @Override
  public void append(final LogEvent event)
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
    }catch(
        IllegalAccessException |
        InvocationTargetException ex)
    {
      throw new RuntimeException(ex);
    }
  }

  private void write(final Text log, final LogEvent event)
  {
    if (!log.isDisposed()){
      Layout layout = getLayout();
      log.append(new String(layout.toByteArray(event)));
      /*if(layout.ignoresThrowable()) {
        String[] s = event.getThrowableStrRep();
        if (s != null) {
          int len = s.length;
          for(int i = 0; i < len; i++) {
            log.append(s[i]);
            log.append(Layout.LINE_SEP);
          }
        }
      }*/
    }
  }
}
