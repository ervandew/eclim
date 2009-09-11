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
package org.eclim.plugin.core.command.project;

import java.net.URI;

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.util.StringUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * Gets the project that the file at the supplied absolute path belongs to.
 * This command honors project links.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "project_by_resource",
  options = "REQUIRED f file ARG"
)
public class ProjectByResource
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public String execute(CommandLine commandLine)
    throws Exception
  {
    String file = commandLine.getValue(Options.FILE_OPTION);

    // can't use URLEncoder on the full file since the colon in 'C:' gets
    // encoded as well.
    //URI uri = new URI("file://" + URLEncoder.encode(file, "UTF-8"));
    URI uri = new URI("file://" + file.replaceAll(" ", "%20"));
    IFile[] files = ResourcesPlugin
      .getWorkspace().getRoot().findFilesForLocationURI(uri);

    if (files.length > 0){
      return files[0].getProject().getName();
    }
    return StringUtils.EMPTY;
  }
}
