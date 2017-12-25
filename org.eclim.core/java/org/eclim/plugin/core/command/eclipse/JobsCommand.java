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
package org.eclim.plugin.core.command.eclipse;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.util.StringUtils;

import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Command to list all current jobs or all jobs belonging to a specific family.
 *
 * @author Eric Van Dewoestine
 */
@Command(name = "jobs", options = "OPTIONAL f family ARG")
public class JobsCommand
  extends AbstractCommand
{
  public static final String AUTO_BUILD = "auto_build";
  public static final String AUTO_REFRESH = "auto_refresh";
  public static final String MANUAL_BUILD = "manual_build";
  public static final String MANUAL_REFRESH = "manual_refresh";

  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    Object family = getFamily(commandLine.getValue(Options.FAMILY_OPTION));
    IJobManager manager = Job.getJobManager();
    Job[] jobs = manager.find(family);

    ArrayList<HashMap<String,String>> results =
      new ArrayList<HashMap<String,String>>();

    for (Job job : jobs){
      HashMap<String,String> result = new HashMap<String,String>();
      result.put("job", job.toString());
      result.put("status", getStatus(job));
      results.add(result);
    }
    return results;
  }

  private Object getFamily(String name)
  {
    if (name != null){
      if (AUTO_BUILD.equals(name)){
        return ResourcesPlugin.FAMILY_AUTO_BUILD;
      }else if (AUTO_REFRESH.equals(name)){
        return ResourcesPlugin.FAMILY_AUTO_REFRESH;
      }else if (MANUAL_BUILD.equals(name)){
        return ResourcesPlugin.FAMILY_MANUAL_BUILD;
      }else if (MANUAL_REFRESH.equals(name)){
        return ResourcesPlugin.FAMILY_MANUAL_REFRESH;
      }
    }
    return null;
  }

  private String getStatus(Job job)
  {
    int status = job.getState();
    switch(status){
      case Job.RUNNING:
        return "running";
      case Job.SLEEPING:
        return "sleeping";
      case Job.WAITING:
        return "waiting";
      case Job.NONE:
        return "none";
    }
    return StringUtils.EMPTY;
  }
}
