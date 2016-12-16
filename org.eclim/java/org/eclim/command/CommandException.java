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
package org.eclim.command;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * If a command wants to throw an exception and return it over the eclim
 * interfaces (NailGun or HTTP) to the client the command can not return the
 * exception: If it would return an Exception org.eclim.command.Main.nailMain()
 * (line 133 - 139) would crash. --> The commands have the possibility to return
 * this {@code CommandException} which then will be returned as a JSON Object.
 *
 * @author Lukas Roth
 *
 */
public class CommandException
{
  /**
   * Describes if the error originates from the client (e.g. a project is not
   * here which the client passed in the argument) or if it is an internal error
   * (e.g. IO Error, we can not write to disk)
   */
  public enum ErrorType {
    SYSTEM_ERROR, CLIENT_ERROR
  }

  /*
   * NOSONAR: This fields will be used to create the JSON object.
   */
  private final String message; // NOSONAR
  private final String stackTrace; // NOSONAR
  private final ErrorType errorType; // NOSONAR

  public CommandException(Exception e, ErrorType errorType)
  {
    this.message = e.getMessage();
    StringWriter buf = new StringWriter();
    e.printStackTrace(new PrintWriter(buf));
    this.stackTrace = buf.toString();
    this.errorType = errorType;
  }

  public CommandException(String message, ErrorType errorType)
  {
    this.message = message;
    this.stackTrace = "";
    this.errorType = errorType;
  }

  public ErrorType getErrorType()
  {
    return errorType;
  }
}
