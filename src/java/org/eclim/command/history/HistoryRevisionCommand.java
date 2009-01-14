/**
 * Copyright (C) 2005 - 2009  Eric Van Dewoestine
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
package org.eclim.command.history;

import org.apache.commons.lang.StringUtils;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.util.IOUtils;
import org.eclim.util.ProjectUtils;

import org.eclipse.core.internal.resources.File;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.team.core.history.IFileRevision;

import org.eclipse.team.internal.core.history.LocalFileHistory;

/**
 * Command which outputs the contents of a specific revision.
 *
 * @author Eric Van Dewoestine
 * @version $Revision$
 */
public class HistoryRevisionCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   * @see org.eclim.command.Command#execute(CommandLine)
   */
  public String execute(CommandLine commandLine)
    throws Exception
  {
    String project = commandLine.getValue(Options.PROJECT_OPTION);
    String filename = commandLine.getValue(Options.FILE_OPTION);
    long revision = commandLine.getLongValue(Options.REVISION_OPTION);

    File file = (File)ProjectUtils.getFile(project, filename);
    LocalFileHistory history = new LocalFileHistory(file, false);
    history.refresh(new NullProgressMonitor());
    IFileRevision[] revisions = history.getFileRevisions();
    for(IFileRevision rev : revisions){
      if (rev.getTimestamp() == revision){
        return IOUtils.toString(
            rev.getStorage(new NullProgressMonitor()).getContents());
      }
    }
    return StringUtils.EMPTY;
  }
}
