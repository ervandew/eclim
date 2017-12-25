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
package org.eclim.plugin.core.command.refactoring;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Exception used to break out of a refactoring during initialization and return
 * a message.
 *
 * @author Eric Van Dewoestine
 */
public class RefactorException
  extends RuntimeException
{
  private static final long serialVersionUID = 1L;
  private RefactoringStatus status;

  public RefactorException()
  {
    super();
  }

  public RefactorException(String message)
  {
    super(message);
  }

  public RefactorException(RefactoringStatus status)
  {
    super();
    this.status = status;
  }

  public RefactoringStatus getStatus()
  {
    return status;
  }
}
