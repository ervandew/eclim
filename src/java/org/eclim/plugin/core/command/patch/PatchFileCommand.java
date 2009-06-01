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
package org.eclim.plugin.core.command.patch;

import java.io.FileOutputStream;

import java.net.HttpURLConnection;
import java.net.URL;

import org.eclim.Services;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.util.IOUtils;

import org.eclim.util.file.FileUtils;

/**
 * Command to patch a vim script file.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "patch_file",
  options =
    "REQUIRED f file ARG," +
    "REQUIRED r revision ARG," +
    "REQUIRED b basedir ARG"
)
public class PatchFileCommand
  extends AbstractCommand
{
  private static final String URL =
    "http://eclim.git.sourceforge.net/git/gitweb.cgi?" +
    "p=eclim;a=blob_plain;f=src/vim/<file>;hb=<revision>";
  private static int TIMEOUT = 8 * 1000;

  /**
   * {@inheritDoc}
   */
  public String execute(CommandLine commandLine)
    throws Exception
  {
    String file = commandLine.getValue(Options.FILE_OPTION);
    String revision = commandLine.getValue(Options.REVISION_OPTION);
    String basedir = commandLine.getValue(Options.BASEDIR_OPTION);

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
