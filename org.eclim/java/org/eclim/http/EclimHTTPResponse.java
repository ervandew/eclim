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
 * Describes how a HTTP response of eclim looks like. This object is returned
 * (converted to JSON) in the response body.
 *
 * @author Lukas Roth
 *
 */
public class EclimHTTPResponse
{
  private final String result;
  private final String errStream;
  private final String outStream;
  private final int statusCode;

  public EclimHTTPResponse(String result, String outStream, String errStream,
      int statusCode)
  {
    this.result = result;
    this.outStream = outStream;
    this.errStream = errStream;
    this.statusCode = statusCode;
  }

  public String getResult()
  {
    return result;
  }

  public String getErrStream()
  {
    return errStream;
  }

  public String getOutStream()
  {
    return outStream;
  }

  public int getStatusCode()
  {
    return statusCode;
  }
}
