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
package org.eclim.command.patch;

import java.io.FileOutputStream;

import java.net.HttpURLConnection;
import java.net.URL;

import org.eclim.Services;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.util.IOUtils;

import org.eclim.util.file.FileUtils;

/**
 * Command to patch a vim script file.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class PatchFileCommand
  extends AbstractCommand
{
  private static final String URL =
    "http://eclim.svn.sourceforge.net/viewvc/*checkout*/" +
    "eclim/trunk/src/vim/<file>?revision=<revision>";
  private static int TIMEOUT = 8 * 1000;

  /**
   * {@inheritDoc}
   */
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    String file = _commandLine.getValue(Options.FILE_OPTION);
    String revision = _commandLine.getValue(Options.REVISION_OPTION);
    String basedir = _commandLine.getValue(Options.BASEDIR_OPTION);

    String url = URL.replaceFirst("<file>", file);
    url = url.replaceFirst("<revision>", revision);

    HttpURLConnection client = (HttpURLConnection)new URL(url).openConnection();
    client.setReadTimeout(TIMEOUT);

    FileOutputStream out = null;
    try{
      client.connect();
      if(client.getResponseCode() != HttpURLConnection.HTTP_OK){
        throw new RuntimeException(
            Services.getMessage("http.error", client.getResponseMessage()));
      }

      IOUtils.copy(client.getInputStream(),
          out = new FileOutputStream(FileUtils.concat(basedir, file)));
    }finally{
      try{
        client.disconnect();
      }catch(Exception ignore){
      }
      IOUtils.closeQuietly(out);
    }

    return Services.getMessage("vim.script.updated", file);
  }
}
