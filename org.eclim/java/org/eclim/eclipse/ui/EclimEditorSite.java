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
package org.eclim.eclipse.ui;

import org.apache.commons.lang.StringUtils;

import org.eclim.eclipse.EclimPlugin;

import org.eclim.eclipse.ui.internal.EclimWorkbenchWindow;

import org.eclipse.jface.action.MenuManager;

import org.eclipse.jface.viewers.ISelectionProvider;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * IEditorSite implementation for eclim.
 *
 * @author Eric Van Dewoestine
 */
@SuppressWarnings("rawtypes")
public class EclimEditorSite
  implements IEditorSite
{
  private IWorkbenchWindow window;
  private ISelectionProvider selectionProvider;

  public EclimEditorSite ()
  {
    window = new EclimWorkbenchWindow();
  }

  @Override
  public String getId()
  {
    return StringUtils.EMPTY;
  }

  @Override
  public String getPluginId()
  {
    return "org.eclim";
  }

  @Override
  public String getRegisteredName()
  {
    return StringUtils.EMPTY;
  }

  @Override
  public void registerContextMenu(
      String arg0, MenuManager arg1, ISelectionProvider arg2)
  {
  }

  @Override
  public void registerContextMenu(MenuManager arg0, ISelectionProvider arg1)
  {
  }

  @Override
  @SuppressWarnings("deprecation")
  public org.eclipse.ui.IKeyBindingService getKeyBindingService()
  {
    return null;
  }

  @Override
  public IWorkbenchPart getPart()
  {
    return null;
  }

  @Override
  public IWorkbenchPage getPage()
  {
    return window.getActivePage();
  }

  @Override
  public ISelectionProvider getSelectionProvider()
  {
    return selectionProvider;
  }

  @Override
  public Shell getShell()
  {
    return EclimPlugin.getShell();
  }

  @Override
  public IWorkbenchWindow getWorkbenchWindow()
  {
    return window;
  }

  @Override
  public void setSelectionProvider(ISelectionProvider provider)
  {
    this.selectionProvider = provider;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object getAdapter(Class arg0)
  {
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object getService(Class arg0)
  {
    return null;
  }

  @Override
  public boolean hasService(Class arg0)
  {
    return false;
  }

  @Override
  public IEditorActionBarContributor getActionBarContributor()
  {
    return null;
  }

  @Override
  public IActionBars getActionBars()
  {
    return null;
  }

  @Override
  public void registerContextMenu(
      MenuManager arg0, ISelectionProvider arg1, boolean arg2)
  {
  }

  @Override
  public void registerContextMenu(
      String arg0, MenuManager arg1, ISelectionProvider arg2, boolean arg3)
  {
  }
}
