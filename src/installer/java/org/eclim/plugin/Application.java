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
package org.eclim.plugin;

import org.eclipse.core.runtime.IPlatformRunnable;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.swt.widgets.Display;

import org.eclipse.update.standalone.CmdLineArgs;
import org.eclipse.update.standalone.ScriptedCommand;
import org.eclipse.update.standalone.UninstallCommand;

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
      if(!installed && !(cmd instanceof UninstallCommand)){
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
