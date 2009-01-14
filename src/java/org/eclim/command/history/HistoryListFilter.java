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
package org.eclim.command.history;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import org.eclim.command.CommandLine;
import org.eclim.command.OutputFilter;

import org.eclipse.team.core.history.IFileRevision;

import org.joda.time.Period;

/**
 * Filter for formatting array of file revision results.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class HistoryListFilter
  implements OutputFilter<IFileRevision[]>
{
  public static final HistoryListFilter instance = new HistoryListFilter();

  private static final SimpleDateFormat dateFormatter =
    new SimpleDateFormat("HH:mm EEE MMM dd yyyy");

  /**
   * {@inheritDoc}
   * @see OutputFilter#filter(CommandLine,T)
   */
  public String filter(CommandLine commandLine, IFileRevision[] results)
  {
    StringBuffer out = new StringBuffer();
    out.append('[');
    for(int ii = 0; ii < results.length; ii++){
      IFileRevision rev = results[ii];
      if(ii > 0){
        out.append(',');
      }
      long timestamp = rev.getTimestamp();
      out.append("{'timestamp': '").append(timestamp).append("',");
      out.append("'datetime': '").append(format(timestamp)).append("',");
      out.append("'delta': '").append(delta(timestamp)).append("'}");
    }
    out.append(']');
    return out.toString();
  }

  private String format(long time)
  {
    return dateFormatter.format(new Date(time));
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
