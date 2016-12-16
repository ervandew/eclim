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
package org.eclim.plugin.jdt.command.dependency;

/**
 * Exception thrown by the {@code ClasspathFileManipulator}.
 *
 * @author Lukas Roth
 *
 */
public class ClasspathFileManipulatorException extends Exception
{
  private static final long serialVersionUID = -1988234200200654186L;

  public ClasspathFileManipulatorException(String message)
  {
    super(message);
  }

  public ClasspathFileManipulatorException(String message, Exception exception)
  {
    super(message, exception);
  }
}
