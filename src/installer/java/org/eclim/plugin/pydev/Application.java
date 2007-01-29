/**
 * Copyright (c) 2005 - 2006
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
