/**
 * Copyright (c) 2005 - 2008
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
package org.eclim.installer.step.command;

import org.apache.log4j.Logger;

/**
 * Uninstall a feature.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class UninstallCommand
  extends Command
{
  private static final Logger logger =
    Logger.getLogger(UninstallCommand.class);

  private String feature;
  private String version;

  public UninstallCommand (OutputHandler handler, String id, String version)
  {
    super(handler, new String[]{
      "-command", "uninstall",
      "-featureId", id,
      "-version", version
    });
    this.feature = id;
    this.version = version;
  }

  /**
   * {@inheritDoc}
   * @see Command#getReturnCode()
   */
  public int getReturnCode ()
  {
    if (super.getReturnCode() != 0){
      logger.warn("Unable to uninstall feature: " + feature + " " + version);
    }

    // don't fail on uninstall error.  later versions of eclipse may auto
    // uninstall.
    return 0;
  }
}
