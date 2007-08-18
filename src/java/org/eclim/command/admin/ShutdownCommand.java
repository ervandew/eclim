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
package org.eclim.command.admin;

import org.apache.log4j.Logger;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;

import org.eclim.eclipse.EclimApplication;

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
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    try{
      EclimApplication.getInstance().stop();
    }catch(Exception e){
      logger.error("Error shutting down eclim:", e);
    }
    return null;
  }
}
