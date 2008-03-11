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

/**
 * Command to enable a feature.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class EnableCommand
  extends Command
{
  public EnableCommand (OutputHandler handler, String id, String version, String to)
  {
    super(handler, new String[]{
      "-command", "enable",
      "-featureId", id,
      "-version", version
    }, to);
  }
}
