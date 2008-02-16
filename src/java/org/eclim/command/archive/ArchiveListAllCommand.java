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
package org.eclim.command.archive;

import java.util.Arrays;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSelector;

/**
 * Command to list all contents of an archive.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class ArchiveListAllCommand
  extends ArchiveListCommand
{
  /**
   * {@inheritDoc}
   * @see ArchiveListCommand#getFiles(FileObject)
   */
  @Override
  protected FileObject[] getFiles (FileObject archive)
    throws Exception
  {
    return archive.findFiles(new FileSelector(){
      public boolean includeFile (FileSelectInfo info){
        return true;
      }
      public boolean traverseDescendents (FileSelectInfo info){
        return true;
      }
    });
  }

  /**
   * {@inheritDoc}
   * @see ArchiveListCommand#processFiles(FileObject[])
   */
  @Override
  protected String[] processFiles (FileObject[] files)
    throws Exception
  {
    String[] results = super.processFiles(files);
    Arrays.sort(results);
    return results;
  }
}
