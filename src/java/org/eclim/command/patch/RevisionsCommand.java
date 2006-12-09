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

import java.io.ByteArrayOutputStream;

import java.util.ArrayList;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;

import org.apache.commons.httpclient.methods.GetMethod;

import org.apache.commons.io.IOUtils;

import org.eclim.Services;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

/**
 * Command to fetch available revisions for a give vim script file.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
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
  public Object execute (CommandLine _commandLine)
  {
    try{
      String file = _commandLine.getValue(Options.FILE_OPTION);
      String url = URL.replaceFirst("<file>", file);

      HttpClient client = new HttpClient();
      client.getHttpConnectionManager().getParams().setConnectionTimeout(TIMEOUT);

      HttpMethod method = new GetMethod(url);
      try{
        int status = client.executeMethod(method);
        if(status != HttpStatus.SC_OK){
          throw new RuntimeException(
              Services.getMessage("http.error", method.getStatusLine()));
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(method.getResponseBodyAsStream(), out);

        List revisions = new ArrayList();
        Matcher matcher = REVISION_REGEX.matcher(out.toString());
        while(matcher.find()){
          revisions.add(matcher.group(1));
        }

        return super.filter(_commandLine, revisions);
      }finally{
        method.releaseConnection();
      }
    }catch(Exception e){
      return e;
    }
  }
}
