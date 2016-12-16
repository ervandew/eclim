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

/**
 * Exception thrown by the {@codeHTTPServer}.
 *
 * @author Lukas Roth
 *
 */
public class HTTPServerException extends Exception
{
  private static final long serialVersionUID = -3627071763625417908L;
  private final int statusCode;

  public HTTPServerException(Exception e, int statusCode)
  {
    super(e);
    this.statusCode = statusCode;
  }

  public HTTPServerException(String message, int statusCode)
  {
    super(message);
    this.statusCode = statusCode;
  }

  public int getStatusCode()
  {
    return statusCode;
  }
}
