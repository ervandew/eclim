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
package org.eclim.plugin.ant.util;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclipse.ant.internal.ui.AntUtil;

import org.eclipse.ant.internal.ui.model.AntModel;
import org.eclipse.ant.internal.ui.model.IAntModel;
import org.eclipse.ant.internal.ui.model.IProblemRequestor;
import org.eclipse.ant.internal.ui.model.LocationProvider;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import org.eclipse.jface.text.IDocument;

/**
 * Utility methods for working with ant files.
 *
 * @author Eric Van Dewoestine
 */
public class AntUtils
{
  /**
   * Gets an ant model for the given file.
   *
   * @param project The project name.
   * @param antFile The ant file.
   * @return The ant model.
   */
  public static IAntModel getAntModel(String project, String antFile)
  {
    return getAntModel(project, antFile, null);
  }

  /**
   * Gets an ant model for the given file.
   * Based on similar method in org.eclipse.ant.internal.ui.AntUtil
   *
   * @param project The project name.
   * @param antFile The ant file.
   * @param requestor Optional IProblemRequestor to be notified of errors in
   * the ant file.
   * @return The ant model.
   */
  public static IAntModel getAntModel(
      String project, String antFile, IProblemRequestor requestor)
  {
    // must refresh the file before grabbing the document.
    final IFile file = AntUtil.getFileForLocation(
        ProjectUtils.getFilePath(project, antFile), null);
    if (file == null) {
      throw new RuntimeException("Invalid project or file location");
    }
    try{
      file.refreshLocal(IResource.DEPTH_INFINITE, null);
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }

    IDocument doc = ProjectUtils.getDocument(project, antFile);
    //final String filepath = ProjectUtils.getFilePath(project, antFile);

    LocationProvider provider = new LocationProvider(null) {
      public IFile getFile()
      {
        return file;
      }
      public IPath getLocation()
      {
        /*if (file == null) {
          return new Path(filepath);
        }*/
        return file.getLocation();
      }
    };

    return new AntModel(doc, requestor, provider, true, true, true);
  }
}
