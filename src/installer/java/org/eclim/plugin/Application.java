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
package org.eclim.plugin;

import org.eclipse.core.runtime.IPlatformRunnable;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.swt.widgets.Display;

import org.eclipse.update.standalone.CmdLineArgs;
import org.eclipse.update.standalone.ScriptedCommand;

/**
 * Entry point for installer application.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class Application
  implements IPlatformRunnable
{
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
      CmdLineArgs cmdLineArgs = new CmdLineArgs(params);
      ScriptedCommand cmd = cmdLineArgs.getCommand();
      boolean installed = cmd.run(new ProgressMonitor());
      if(!installed){
        throw new RuntimeException("Feature not installed.");
      }
    }catch(Throwable t){
      t.printStackTrace();
      return new Integer(1);
    }

    return new Integer(0);
  }

  private static class ProgressMonitor
    implements IProgressMonitor
  {
    private double totalWorked;
    private boolean canceled;

    /**
     * {@inheritDoc}
     * @see IProgressMonitor#beginTask(String,int)
     */
    public void beginTask (String name, int totalWork)
    {
// FIXME: validate that the requested plugin was found.
      System.out.println("beginTask: totalWork=" + totalWork + " name=" + name);
    }

    /**
     * {@inheritDoc}
     * @see IProgressMonitor#done()
     */
    public void done ()
    {
      System.out.println("done");
    }

    /**
     * {@inheritDoc}
     * @see IProgressMonitor#internalWorked(double)
     */
    public void internalWorked (double work)
    {
      totalWorked += work;
      System.out.println("internalWorked: " + totalWorked);
    }

    /**
     * {@inheritDoc}
     * @see IProgressMonitor#isCanceled()
     */
    public boolean isCanceled ()
    {
      return canceled;
    }

    /**
     * {@inheritDoc}
     * @see IProgressMonitor#setCanceled(boolean)
     */
    public void setCanceled (boolean canceled)
    {
      this.canceled = canceled;
    }

    /**
     * {@inheritDoc}
     * @see IProgressMonitor#setTaskName(String)
     */
    public void setTaskName (String name)
    {
      System.out.println("setTaskName: " + name);
    }

    /**
     * {@inheritDoc}
     * @see IProgressMonitor#subTask(String)
     */
    public void subTask (String name)
    {
      System.out.println("subTask: " + name);
    }

    /**
     * {@inheritDoc}
     * @see IProgressMonitor#worked(int)
     */
    public void worked (int work)
    {
      totalWorked += work;
      System.out.println("worked: " + totalWorked);
    }
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
