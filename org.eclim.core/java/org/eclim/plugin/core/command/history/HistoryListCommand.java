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
package org.eclim.plugin.core.command.history;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclim.util.StringUtils;

import org.eclipse.core.internal.resources.File;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.team.core.history.IFileRevision;

import org.eclipse.team.internal.core.history.LocalFileHistory;

import org.joda.time.Period;

/**
 * Command to list available local history revisions for a given file.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "history_list",
  options =
    "REQUIRED p project ARG," +
    "REQUIRED f file ARG"
)
public class HistoryListCommand
  extends AbstractCommand
{
  private static final SimpleDateFormat DATE_FORMATTER =
    new SimpleDateFormat("HH:mm EEE MMM dd yyyy");

  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    String project = commandLine.getValue(Options.PROJECT_OPTION);
    String filename = commandLine.getValue(Options.FILE_OPTION);

    File file = (File)ProjectUtils.getFile(project, filename);
    LocalFileHistory history =
      new LocalFileHistory(file, false /* include current */);
    history.refresh(new NullProgressMonitor());
    IFileRevision[] revisions = history.getFileRevisions();
    Arrays.sort(revisions, new Comparator<IFileRevision>(){
      public int compare(IFileRevision r1, IFileRevision r2){
        return (int)(r2.getTimestamp() - r1.getTimestamp());
      }
    });

    ArrayList<HashMap<String,Object>> results =
      new ArrayList<HashMap<String,Object>>();
    for (IFileRevision revision : revisions){
      HashMap<String,Object> result = new HashMap<String,Object>();
      result.put("timestamp", String.valueOf(revision.getTimestamp()));
      result.put("datetime",
          DATE_FORMATTER.format(new Date(revision.getTimestamp())));
      result.put("delta", delta(revision.getTimestamp()));
      results.add(result);
    }

    return results;
  }

  private String delta(long time)
  {
    // FIXME: a formatter can probably do this.
    Period period = new Period(time, System.currentTimeMillis());
    ArrayList<String> parts = new ArrayList<String>();

    int years = period.getYears();
    if(years > 0){
      parts.add(years + " year" + (years == 1 ? "" : "s"));
    }

    int months = period.getMonths();
    if(months > 0){
      parts.add(months + " month" + (months == 1 ? "" : "s"));
    }

    int weeks = period.getWeeks();
    if(weeks > 0){
      parts.add(weeks + " week" + (weeks == 1 ? "" : "s"));
    }

    int days = period.getDays();
    if(days > 0){
      parts.add(days + " day" + (days == 1 ? "" : "s"));
    }

    int hours = period.getHours();
    if(hours > 0){
      parts.add(hours + " hour" + (hours == 1 ? "" : "s"));
    }

    int minutes = period.getMinutes();
    if(minutes > 0){
      parts.add(minutes + " minute" + (minutes == 1 ? "" : "s"));
    }

    int seconds = period.getSeconds();
    if(seconds > 0){
      parts.add(seconds + " second" + (seconds == 1 ? "" : "s"));
    }

    if(parts.size() == 0){
      int millis = period.getMillis();
      if(millis > 0){
        parts.add(millis + " millis");
      }
    }

    return StringUtils.join(parts.toArray(), ' ') + " ago";
  }
}
