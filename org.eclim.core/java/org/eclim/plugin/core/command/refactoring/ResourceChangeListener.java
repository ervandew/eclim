/**
 * Copyright (C) 2005 - 2020  Eric Van Dewoestine
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
package org.eclim.plugin.core.command.refactoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclim.plugin.core.util.ProjectUtils;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * Resource change listener which can be used to collect a list of relevant
 * resource deltas and generate a list of file moves, renames, etc when applying
 * a Refactoring or Change.
 *
 * @author Eric Van Dewoestine
 */
public class ResourceChangeListener
  implements IResourceChangeListener, IResourceDeltaVisitor
{
  private List<IResourceDelta> deltas = new ArrayList<IResourceDelta>();

  @Override
  public void resourceChanged(IResourceChangeEvent event)
  {
    try{
      event.getDelta().accept(this);
    }catch(CoreException ce){
      throw new RuntimeException(ce);
    }
  }

  @Override
  public boolean visit(IResourceDelta delta)
    throws CoreException
  {
    IResource resource = delta.getResource();
    if (delta.getKind() != IResourceDelta.NO_CHANGE && (
          resource.getType() == IResource.FILE ||
          resource.getType() == IResource.FOLDER))
    {
      deltas.add(delta);
    }
    return true;
  }

  /**
   * Gets a list of relevant leaf node resource deltas.
   *
   * @return list of IResourceDelta.
   */
  public List<IResourceDelta> getResourceDeltas()
  {
    return deltas;
  }

  /**
   * Builds a list of changed files which can be returned from a command
   * informing the client of files changed, moved, etc.
   *
   * @return The list of changed files.
   */
  public List<Map<String, String>> getChangedFiles()
  {
    List<Map<String, String>> results = new ArrayList<Map<String, String>>();
    HashSet<String> seen = new HashSet<String>();

    for (IResourceDelta delta : getResourceDeltas()){
      int flags = delta.getFlags();
      // the moved_from entry should handle this
      if ((flags & IResourceDelta.MOVED_TO) != 0){
        continue;
      }

      HashMap<String, String> result = new HashMap<String, String>();

      IResource resource = delta.getResource();
      IPath location = resource.getLocation();

      // ignore reported moves of .class files
      if ("class".equals(location.getFileExtension())){
        continue;
      }

      String file = location.toOSString().replace('\\', '/');

      if ((flags & IResourceDelta.MOVED_FROM) != 0){
        String path = ProjectUtils.getFilePath(
            resource.getProject(),
            delta.getMovedFromPath().toOSString());
        result.put("from", path);
        result.put("to", file);

      // filter out all the dirs leading up to the actual changed files
      }else if (resource.getLocation().getFileExtension() != null){
        if (!seen.contains(file)){
          result.put("file", file);
          seen.add(file);
        }
      }
      if (result.size() > 0){
        results.add(result);
      }
    }
    return results;
  }
}
