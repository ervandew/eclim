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
package org.eclim.plugin.pydev.command.src;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.Iterator;
import java.util.List;

import org.eclim.logging.Logger;

import org.eclipse.core.resources.IResource;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import org.eclipse.jface.text.IDocument;

import org.eclipse.ui.console.IOConsoleOutputStream;

import org.python.pydev.builder.PydevMarkerUtils;

import org.python.pydev.builder.pylint.PyLintVisitor;

/**
 * Extension to PyLintVisitor.PyLintThread to allow it to integrate more easily
 * into eclim.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class PyLintThread
  extends PyLintVisitor.PyLintThread
{
  private static final Logger logger = Logger.getLogger(PyLintThread.class);

  private IResource resource;
  private IDocument document;
  private IPath location;

  public PyLintThread (IResource resource, IDocument document, IPath location)
  {
    super(resource, document, location);

    this.resource = resource;
    this.document = document;
    this.location = location;
  }

  public void run()
  {
    try {
      Method passPyLint = PyLintVisitor.PyLintThread.class
        .getDeclaredMethod("passPyLint",
            new Class[]{IResource.class, IOConsoleOutputStream.class});
      passPyLint.setAccessible(true);
      passPyLint.invoke(this, new Object[]{resource, null});

      try {
        resource.deleteMarkers(
            PyLintVisitor.PYLINT_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
      } catch (CoreException ce) {
        logger.error("Could not delete markers.", ce);
      }

      Field field = PyLintVisitor.PyLintThread.class.getDeclaredField("markers");
      field.setAccessible(true);
      List markers = (List)field.get(this);

      for (Iterator iter = markers.iterator(); iter.hasNext();) {
        Object[] el = (Object[]) iter.next();

        String tok   = (String) el[0];
        String type  = (String) el[1];
        int priority = ((Integer)el[2]).intValue();
        String id    = (String) el[3];
        int line     = ((Integer)el[4]).intValue();
        PydevMarkerUtils.createMarker(resource, document, "ID:"+id+" "+tok ,
            line, 0, line, 0,
            type, priority);
      }

    } catch(Exception e) {
      logger.error("Failed to run pylint.", e);
    }
  }
}
