/**
 * Copyright (c) 2004 - 2005
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
package org.eclim.command.admin;

import java.io.IOException;

import com.martiansoftware.nailgun.NGServer;

import org.apache.log4j.Logger;

import org.eclim.Services;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;

/**
 * Command to shutdown the eclim server.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ShutdownCommand
  extends AbstractCommand
{
  private static final Logger logger = Logger.getLogger(ShutdownCommand.class);

  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
    throws IOException
  {
    try{
      NGServer server = (NGServer)Services.getService(NGServer.class);

      Services.close();

      server.shutdown(false);
    }catch(IllegalStateException ise){
      // workspace already closed.
    }catch(Exception e){
      logger.error("Error shutting down eclim:", e);
    }
    return null;
  }
}
