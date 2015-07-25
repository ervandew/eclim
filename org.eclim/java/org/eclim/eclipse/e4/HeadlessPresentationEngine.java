/**
 * Copyright (C) 2012 - 2015  Eric Van Dewoestine
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
package org.eclim.eclipse.e4;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;

import org.eclipse.e4.ui.internal.workbench.E4Workbench;

import org.eclipse.e4.ui.internal.workbench.swt.PartRenderingEngine;

import org.eclipse.e4.ui.model.application.ui.MUIElement;

import org.eclipse.e4.ui.model.application.ui.basic.MWindow;

/**
 * Presentation engine which prevents the workbench from being displayed.
 *
 * @author Eric Van Dewoestine
 */
public class HeadlessPresentationEngine
  extends PartRenderingEngine
{
  @Inject
  public HeadlessPresentationEngine(
      @Named(E4Workbench.RENDERER_FACTORY_URI) @Optional String factoryUrl)
  {
    super(factoryUrl);
  }

  @Override
  protected Object createWidget(MUIElement element, Object parent)
  {
    Object widget = super.createWidget(element, parent);
    // hide all elements
    element.setVisible(false);
    return widget;
  }

  @Override
  protected boolean someAreVisible(List<MWindow> windows)
  {
    // prevent event loop from exiting on non-visible top window.
    for (MWindow win : windows) {
      if (win.isToBeRendered() /*&& win.isVisible()*/ && win.getWidget() != null) {
        return true;
      }
    }
    return false;
  }
}
