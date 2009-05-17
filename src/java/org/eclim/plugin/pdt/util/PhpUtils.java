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
package org.eclim.plugin.pdt.util;

import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Utility methods for working with php.
 *
 * @author Eric Van Dewoestine
 */
public class PhpUtils
{
  /**
   * Big hack which forces the current operation to wait on any pending auto
   * build jobs which the pdt checks for and returns no results in its selection
   * and completion engines if such a job exists.
   *
   * The offending pdt code is located at the following 2 places:
   * org.eclipse.php.internal.core.codeassist.PHPSelectionEngine.select
   * org.eclipse.php.internal.core.codeassist.PHPCompletionEngine.complete
   */
  public static void waitOnBuild()
  {
    IJobManager manager = Job.getJobManager();
    Job[] jobs = manager.find(ResourcesPlugin.FAMILY_AUTO_BUILD);

    if(jobs != null && jobs.length > 0){
      // force the jobs that are holding us up to execute.
      for (Job job : jobs){
        job.wakeUp();
      }

      int tries = 0;
      while(tries < 10 && jobs != null && jobs.length > 0){
        try{
          Thread.sleep(100);
        }catch(Exception ignore){
        }
        jobs = manager.find(ResourcesPlugin.FAMILY_AUTO_BUILD);
        tries++;
      }
    }
  }
}
