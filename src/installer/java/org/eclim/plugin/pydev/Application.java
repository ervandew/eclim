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
package org.eclim.plugin.pydev;

import org.eclipse.core.runtime.IPlatformRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.swt.widgets.Display;

import org.python.pydev.core.IInterpreterManager;

import org.python.pydev.plugin.PydevPlugin;

/**
 * Entry point for installer application.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class Application
  implements IPlatformRunnable
{
  private static final String LIST = "list";
  private static final String SET = "set";

  /**
   * Runs this runnable with the supplied args.
   *
   * @param _args The arguments (typically a String[]).
   * @return The result (returned Integer is treated as exit code).
   */
  public Object run (Object _args)
    throws Exception
  {
    try{
      // create the eclipse workbench.
      org.eclipse.ui.PlatformUI.createAndRunWorkbench(
          Display.getDefault(), new WorkbenchAdvisor());

      String[] params = (String[])_args;

      IInterpreterManager manager = PydevPlugin.getPythonInterpreterManager();
      if(LIST.equals(params[params.length - 1])){
        String [] executables = manager.getInterpretersFromPersistedString(
            manager.getPersistedString());
        for (int ii = 0; ii < executables.length; ii++){
          System.out.println(executables[ii]);
        }
      }else if(SET.equals(params[params.length - 2])){
        String interpreter = params[params.length - 1];
        manager.getInterpreterInfo(interpreter, new NullProgressMonitor());
        manager.setPersistedString(
            manager.getStringToPersist(new String[]{interpreter}));
        PydevPlugin.getDefault().savePluginPreferences();
      }
    }catch(Throwable t){
      return new Integer(1);
    }

    return new Integer(0);
  }

  private static class WorkbenchAdvisor
    extends org.eclipse.ui.application.WorkbenchAdvisor
  {
    /**
     * {@inheritDoc}
     */
    public String getInitialWindowPerspectiveId ()
    {
      return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean openWindows ()
    {
      return false;
    }
  }
}
