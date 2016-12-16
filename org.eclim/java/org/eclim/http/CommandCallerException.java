/**
 * Copyright (C) 2005 - 2016  Eric Van Dewoestine
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
package org.eclim.http;

import org.eclim.command.CommandException;

/**
 * Exception the CommandCaller throws.
 *
 * If the command has returned a {@code ReturnableException} we throw it inside
 * this class.
 *
 * @author Lukas Roth
 *
 */
public class CommandCallerException extends Exception
{
  private static final long serialVersionUID = 8692866089672811919L;
  private final CommandException returnableException;

  public CommandCallerException(String message)
  {
    super(message);
    this.returnableException = null;
  }

  public CommandCallerException(String message, Exception cause)
  {
    super(message, cause);
    this.returnableException = null;
  }

  public CommandCallerException(CommandException returnableException)
  {
    this.returnableException  = returnableException;
  }

  public CommandException getReturnableException()
  {
    return returnableException;
  }

  public boolean isReturnableException()
  {
    return returnableException != null;
  }
}