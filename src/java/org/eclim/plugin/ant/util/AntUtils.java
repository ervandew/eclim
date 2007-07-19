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
package org.eclim.plugin.ant.util;

import org.apache.commons.io.FilenameUtils;

import org.eclim.util.ProjectUtils;

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
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
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
    final String filepath = FilenameUtils.concat(
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
