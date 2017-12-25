/**
 * Copyright (C) 2005 - 2017  Eric Van Dewoestine
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
package org.eclim.plugin.wst.command.validate;

import java.net.URLDecoder;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclim.plugin.core.util.ProjectUtils;

/**
 * Abstract super class for wst based validators.
 *
 * @author Eric Van Dewoestine
 */
public abstract class WstValidateCommand
  extends AbstractCommand
{
  // eclipse looks for 3 leading slashes on every os, so 3 are necessary here.
  private static final String URI_PREFIX = "file:///";

  /**
   * Converts the supplied file name to a uri if necessary.
   *
   * @param project The project name.
   * @param filename The file.
   * @return The uri.
   */
  protected String toUri(String project, String filename)
  {
    if (filename.indexOf("://") == -1){
      filename = URI_PREFIX + ProjectUtils.getFilePath(project, filename);
    }
    return filename.replace('\\', '/');
  }

  /**
   * Converts the supplied uri to a file name if necessary.
   *
   * @param uri The uri.
   * @return The file name.
   */
  protected String toFile(String uri)
  {
    String file = uri.startsWith(URI_PREFIX) ?
      uri.substring(URI_PREFIX.length()) : uri;
    try{
      return URLDecoder.decode(file, "utf-8");
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }
}
