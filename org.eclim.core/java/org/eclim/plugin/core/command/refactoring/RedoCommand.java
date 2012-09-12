/**
 * Copyright (C) 2005 - 2012  Eric Van Dewoestine
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

import org.eclim.annotation.Command;

import org.eclim.command.CommandLine;
import org.eclim.command.Options;

import org.eclim.plugin.core.command.AbstractCommand;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.IUndoManager;
import org.eclipse.ltk.core.refactoring.RefactoringCore;

/**
 * Command to redo the last refactoring on the stack.
 *
 * @author Eric Van Dewoestine
 */
@Command(
  name = "refactor_redo",
  options = "OPTIONAL p peek NOARG"
)
public class RedoCommand
  extends AbstractCommand
{
  @Override
  public Object execute(CommandLine commandLine)
    throws Exception
  {
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    ResourceChangeListener rcl = new ResourceChangeListener();
    workspace.addResourceChangeListener(rcl);
    try{
      IUndoManager manager = RefactoringCore.getUndoManager();
      if(commandLine.hasOption(Options.PEEK_OPTION)){
        return manager.peekUndoName();
      }
      manager.performRedo(null, null);
      return rcl.getChangedFiles();
    }finally{
      workspace.removeResourceChangeListener(rcl);
    }
  }
}
