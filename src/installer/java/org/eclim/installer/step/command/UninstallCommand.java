/**
 * Copyright (C) 2005 - 2010  Eric Van Dewoestine
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
package org.eclim.installer.step.command;

/**
 * Command to uninstall a feature.
 *
 * @author Eric Van Dewoestine
 */
public class UninstallCommand
  extends Command
{
  public UninstallCommand(OutputHandler handler, String url, String id)
    throws Exception
  {
    this(handler, url, id, "org.eclim.installer.application");
  }

  public UninstallCommand(
      OutputHandler handler, String url, String id, String application)
    throws Exception
  {
    super(handler, new String[]{
      "-repository", url,
      "-uninstallIU", id + ".feature.group",
    }, application);
  }
}
