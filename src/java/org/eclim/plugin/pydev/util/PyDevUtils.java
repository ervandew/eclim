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
package org.eclim.plugin.pydev.util;

import org.eclim.util.ProjectUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IEditorSite;

import org.eclipse.ui.internal.EditorReference;
import org.eclipse.ui.internal.EditorSite;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;

import org.eclipse.ui.internal.registry.EditorDescriptor;

import org.eclipse.ui.part.FileEditorInput;

import org.python.pydev.editor.PyEdit;

/**
 * Utilities for working with pydev.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class PyDevUtils
{
  private static PyEdit pyedit;

  /**
   * Gets the PyEdit instance for the supplied file.
   *
   * @param _project The project the file belongs to.
   * @param _file The filename.
   * @return The PyEdit instance.
   */
  public static PyEdit getEditor (IProject _project, String _file)
    throws Exception
  {
    IEditorInput input = getEditorInput(_project, _file);
    if(pyedit == null){
      pyedit = new PyEdit();
      pyedit.init(getEditorSite(input), input);
    }else{
      pyedit.setInput(input);
    }

    return pyedit;
  }

  /**
   * Gets the IEditorSite instance to use.
   *
   * @param input IEditorInput.
   * @return The IEditorSite.
   */
  private static IEditorSite getEditorSite (IEditorInput input)
    throws Exception
  {
    WorkbenchWindow window = new WorkbenchWindow(1);
    window.create();

    WorkbenchPage page = new WorkbenchPage(window,
        "org.python.pydev.ui.PythonPerspective", null);

    // get EditorDescriptor
    IEditorRegistry reg = WorkbenchPlugin.getDefault().getEditorRegistry();
    EditorDescriptor desc = (EditorDescriptor)
      reg.findEditor("org.python.pydev.editor.PythonEditor");

    // get IEditorReference
    IEditorReference ref =
      new EditorReference(page.getEditorManager(), input, desc);

    // get IEditorPart
    IEditorPart editor = desc.createEditor();

    return new EditorSite(ref, editor, page, desc);
  }

  /**
   * Gets the IEditorInput for the supplied file.
   *
   * @param _project The project the file belongs to.
   * @param _file The file.
   * @return The IEditorInput.
   */
  private static IEditorInput getEditorInput (IProject _project, String _file)
    throws Exception
  {
    IFile file = ProjectUtils.getFile(_project, _file);
    file.refreshLocal(IResource.DEPTH_INFINITE, null);
    return new FileEditorInput(file);
  }
}
