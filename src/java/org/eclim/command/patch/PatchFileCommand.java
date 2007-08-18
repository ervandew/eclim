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
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
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
