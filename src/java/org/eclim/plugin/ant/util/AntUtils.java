/**
 * Copyright (c) 2004 - 2006
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

import java.io.File;

import org.eclipse.ant.internal.ui.AntUtil;

import org.eclipse.ant.internal.ui.model.AntModel;
import org.eclipse.ant.internal.ui.model.IAntModel;
import org.eclipse.ant.internal.ui.model.IProblemRequestor;
import org.eclipse.ant.internal.ui.model.LocationProvider;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;

import org.eclipse.core.resources.IFile;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
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
   * Gets the IDocument instance for the given ant file.
   * <p/>
   * Borrowed from org.eclipse.ant.internal.ui.AntUtil
   *
   * @param _antFile The ant file.
   * @return The IDocument.
   */
  public static IDocument getDocument (String _antFile)
    throws Exception
  {
    File file = new File(_antFile);
    if(!file.exists()){
      return null;
    }

    ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
    IPath location= new Path(file.getAbsolutePath());
    boolean connected= false;
    try {
      ITextFileBuffer buffer= manager.getTextFileBuffer(location);
      if (buffer == null) {
        //no existing file buffer..create one
        manager.connect(location, new NullProgressMonitor());
        connected= true;
        buffer= manager.getTextFileBuffer(location);
        if (buffer == null) {
          return null;
        }
      }
      return buffer.getDocument();
    } finally {
      if (connected) {
        try {
          manager.disconnect(location, new NullProgressMonitor());
        } catch (Exception e) {
        }
      }
    }
  }

  /**
   * Gets an ant model for the given file.
   *
   * @param _antFile The ant file.
   * @return The ant model.
   */
  public static IAntModel getAntModel (String _antFile)
    throws Exception
  {
    return getAntModel(_antFile, null);
  }

  /**
   * Gets an ant model for the given file.
   * <p/>
   * Based on similar method in org.eclipse.ant.internal.ui.AntUtil
   *
   * @param _antFile The ant file.
   * @param _requestor Optional IProblemRequestor to be notified of errors in
   * the ant file.
   * @return The ant model.
   */
  public static IAntModel getAntModel (
      final String _antFile, IProblemRequestor _requestor)
    throws Exception
  {
    IDocument doc = getDocument(_antFile);
    if (doc == null) {
      return null;
    }

    final IFile file = AntUtil.getFileForLocation(_antFile, null);
    LocationProvider provider = new LocationProvider(null) {
      public IFile getFile() {
        return file;
      }
      public IPath getLocation() {
        if (file == null) {
          return new Path(_antFile);
        }
        return file.getLocation();
      }
    };

    return new AntModel(doc, _requestor, provider, true, true, true);
  }
}
