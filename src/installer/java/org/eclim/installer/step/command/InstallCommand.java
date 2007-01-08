/**
 * Copyright (c) 2005 - 2007
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
 * Command to install a feature.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class InstallCommand
  extends Command
{
  public InstallCommand (
      OutputHandler handler, String url, String id, String version)
  {
    super(handler, new String[]{
      "-command", "install",
      "-from", url,
      "-featureId", id,
      "-version", version
    });
  }
}
