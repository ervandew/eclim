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
package org.eclim.command.patch;

import java.io.ByteArrayOutputStream;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.ArrayList;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclim.Services;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.util.IOUtils;

/**
 * Command to fetch available revisions for a give vim script file.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class RevisionsCommand
  extends AbstractCommand
{
  private static final String URL =
    "http://eclim.svn.sourceforge.net/viewvc/eclim/trunk/src/vim/<file>?view=log";
  private static final Pattern REVISION_REGEX =
    Pattern.compile("Revision\\s+<a\\s+.*?><strong>\\s*(.*?)\\s*</strong>");
  private static int TIMEOUT = 5 * 1000;

  /**
   * {@inheritDoc}
   */
  public String execute(CommandLine commandLine)
    throws Exception
  {
    String file = commandLine.getValue(Options.FILE_OPTION);
    String url = URL.replaceFirst("<file>", file);

    HttpURLConnection client = (HttpURLConnection)new URL(url).openConnection();
    client.setReadTimeout(TIMEOUT);
    try{
      client.connect();
      if(client.getResponseCode() != HttpURLConnection.HTTP_OK){
        throw new RuntimeException(
            Services.getMessage("http.error", client.getResponseMessage()));
      }

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      IOUtils.copy(client.getInputStream(), out);

      ArrayList<String> revisions = new ArrayList<String>();
      Matcher matcher = REVISION_REGEX.matcher(out.toString());
      while(matcher.find()){
        revisions.add(matcher.group(1));
      }

      return RevisionsFilter.instance.filter(commandLine, revisions);
    }finally{
      try{
        client.disconnect();
      }catch(Exception ignore){
      }
    }
  }
}
