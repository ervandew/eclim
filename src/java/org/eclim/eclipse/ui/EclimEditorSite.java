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
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * IEditorSite implementation for eclim.
 *
 * @author Eric Van Dewoestine
 */
public class EclimEditorSite
  implements IEditorSite
{
  private IWorkbenchWindow window;
  private ISelectionProvider selectionProvider;

  public EclimEditorSite ()
  {
    window = new EclimWorkbenchWindow(EclimPlugin.getShell());
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.ui.IWorkbenchPartSite#getId()
   */
  public String getId()
  {
    return StringUtils.EMPTY;
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.ui.IWorkbenchPartSite#getPluginId()
   */
  public String getPluginId()
  {
    return "org.eclim";
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.ui.IWorkbenchPartSite#getRegisteredName()
   */
  public String getRegisteredName()
  {
    return StringUtils.EMPTY;
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.ui.IWorkbenchPartSite#registerContextMenu(String,MenuManager,ISelectionProvider)
   */
  public void registerContextMenu(String arg0, MenuManager arg1, ISelectionProvider arg2)
  {
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.ui.IWorkbenchPartSite#registerContextMenu(MenuManager,ISelectionProvider)
   */
  public void registerContextMenu(MenuManager arg0, ISelectionProvider arg1)
  {
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.ui.IWorkbenchPartSite#getKeyBindingService()
   */
  public IKeyBindingService getKeyBindingService()
  {
    return null;
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.ui.IWorkbenchPartSite#getPart()
   */
  public IWorkbenchPart getPart()
  {
    return null;
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.ui.IWorkbenchSite#getPage()
   */
  public IWorkbenchPage getPage()
  {
    return window.getActivePage();
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.ui.IWorkbenchSite#getSelectionProvider()
   */
  public ISelectionProvider getSelectionProvider()
  {
    return selectionProvider;
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.ui.IWorkbenchSite#getShell()
   */
  public Shell getShell()
  {
    return EclimPlugin.getShell();
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.ui.IWorkbenchSite#getWorkbenchWindow()
   */
  public IWorkbenchWindow getWorkbenchWindow()
  {
    return window;
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.ui.IWorkbenchSite#setSelectionProvider(ISelectionProvider)
   */
  public void setSelectionProvider(ISelectionProvider provider)
  {
    this.selectionProvider = provider;
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
   */
  public Object getAdapter(Class arg0)
  {
    return null;
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.ui.services.IServiceLocator#getService(Class)
   */
  public Object getService(Class arg0)
  {
    return null;
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.ui.services.IServiceLocator#hasService(Class)
   */
  public boolean hasService(Class arg0)
  {
    return false;
  }

  /**
   * {@inheritDoc}
   * @see IEditorSite#getActionBarContributor()
   */
  public IEditorActionBarContributor getActionBarContributor()
  {
    return null;
  }

  /**
   * {@inheritDoc}
   * @see IEditorSite#getActionBars()
   */
  public IActionBars getActionBars()
  {
    return null;
  }

  /**
   * {@inheritDoc}
   * @see IEditorSite#registerContextMenu(MenuManager,ISelectionProvider,boolean)
   */
  public void registerContextMenu(MenuManager arg0, ISelectionProvider arg1, boolean arg2)
  {
  }

  /**
   * {@inheritDoc}
   * @see IEditorSite#registerContextMenu(String,MenuManager,ISelectionProvider,boolean)
   */
  public void registerContextMenu(String arg0, MenuManager arg1, ISelectionProvider arg2, boolean arg3)
  {
  }
}
