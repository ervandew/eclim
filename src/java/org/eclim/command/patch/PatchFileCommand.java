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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;

import org.apache.commons.httpclient.methods.GetMethod;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import org.eclim.Services;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

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
    "http://eclim.cvs.sourceforge.net/*checkout*/" +
    "eclim/eclim/src/vim/<file>?revision=<revision>";
  private static int TIMEOUT = 8 * 1000;

  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
  {
    try{
      String file = _commandLine.getValue(Options.FILE_OPTION);
      String revision = _commandLine.getValue(Options.REVISION_OPTION);
      String basedir = _commandLine.getValue(Options.BASEDIR_OPTION);

      String url = URL.replaceFirst("<file>", file);
      url = url.replaceFirst("<revision>", revision);

      HttpClient client = new HttpClient();
      client.getHttpConnectionManager().getParams().setConnectionTimeout(TIMEOUT);

      HttpMethod method = new GetMethod(url);

      FileOutputStream out = null;
      try{
        int status = client.executeMethod(method);
        if(status != HttpStatus.SC_OK){
          throw new RuntimeException(
              Services.getMessage("http.error", method.getStatusLine()));
        }

        IOUtils.copy(method.getResponseBodyAsStream(),
            out = new FileOutputStream(FilenameUtils.concat(basedir, file)));
      }finally{
        method.releaseConnection();
        IOUtils.closeQuietly(out);
      }

      return Services.getMessage("vim.script.updated", file);
    }catch(Exception e){
      return e;
    }
  }
}
