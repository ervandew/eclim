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
package org.eclim.plugin.wst.command.validate;

import org.apache.commons.io.FilenameUtils;

import org.eclim.command.AbstractCommand;

import org.eclim.util.ProjectUtils;

import org.eclipse.core.resources.IProject;

/**
 * Abstract super class for wst based validators.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public abstract class WstValidateCommand
  extends AbstractCommand
{
  private static final String URI_PREFIX = "file://";

  /**
   * Converts the supplied file name to a uri if necessary.
   *
   * @param project The project name.
   * @param file The file.
   * @return The uri.
   */
  protected String toUri (String project, String filename)
    throws Exception
  {
    if (filename.indexOf("://") == -1){
      filename = URI_PREFIX + FilenameUtils.concat(
          ProjectUtils.getPath(project), filename);
    }
    return filename;
  }

  /**
   * Converts the supplied uri to a file name if necessary.
   *
   * @param uri The uri.
   * @return The file name.
   */
  protected String toFile (String uri)
  {
    return uri.startsWith(URI_PREFIX) ? uri.substring(URI_PREFIX.length()) : uri;
  }
}
