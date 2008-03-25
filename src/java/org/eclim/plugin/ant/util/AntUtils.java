/**
 * Copyright (C) 2005 - 2008  Eric Van Dewoestine
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
package org.eclim.plugin.ant.util;

import org.eclim.util.ProjectUtils;

import org.eclim.util.file.FileUtils;

import org.eclipse.ant.internal.ui.AntUtil;

import org.eclipse.ant.internal.ui.model.AntModel;
import org.eclipse.ant.internal.ui.model.IAntModel;
import org.eclipse.ant.internal.ui.model.IProblemRequestor;
import org.eclipse.ant.internal.ui.model.LocationProvider;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.jface.text.IDocument;

/**
 * Utility methods for working with ant files.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class AntUtils
{
  /**
   * Gets an ant model for the given file.
   *
   * @param _project The project name.
   * @param _antFile The ant file.
   * @return The ant model.
   */
  public static IAntModel getAntModel (String _project, String _antFile)
    throws Exception
  {
    return getAntModel(_project, _antFile, null);
  }

  /**
   * Gets an ant model for the given file.
   * <p/>
   * Based on similar method in org.eclipse.ant.internal.ui.AntUtil
   *
   * @param _project The project name.
   * @param _antFile The ant file.
   * @param _requestor Optional IProblemRequestor to be notified of errors in
   * the ant file.
   * @return The ant model.
   */
  public static IAntModel getAntModel (
      String _project, String _antFile, IProblemRequestor _requestor)
    throws Exception
  {
    // must refres the file before grabbing the document.
    final IFile file = AntUtil.getFileForLocation(
        ProjectUtils.getFilePath(_project, _antFile), null);
    if (file == null) {
      throw new RuntimeException("Invalid project or file location");
    }
    file.refreshLocal(IResource.DEPTH_INFINITE, null);

    IDocument doc = ProjectUtils.getDocument(_project, _antFile);
    final String filepath = FileUtils.concat(
        ProjectUtils.getPath(_project), _antFile);

    LocationProvider provider = new LocationProvider(null) {
      public IFile getFile() {
        return file;
      }
      public IPath getLocation() {
        if (file == null) {
          return new Path(filepath);
        }
        return file.getLocation();
      }
    };

    return new AntModel(doc, _requestor, provider, true, true, true);
  }
}
