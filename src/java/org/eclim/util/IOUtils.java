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
package org.eclim.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Utility methods for io.
 *
 * @author Eric Van Dewoestine
 */
public class IOUtils
{
  private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

  private IOUtils ()
  {
  }

  /**
   * Closes the supplied InputStream ignoring null and any exceptions.
   *
   * @param stream The InputStream to close.
   */
  public static void closeQuietly(InputStream stream)
  {
    try{
      if(stream != null){
        stream.close();
      }
    }catch(Exception e){
      // ignore
    }
  }

  /**
   * Closes the supplied OutputStream ignoring null and any exceptions.
   *
   * @param stream The OutputStream to close.
   */
  public static void closeQuietly(OutputStream stream)
  {
    try{
      if(stream != null){
        stream.close();
      }
    }catch(Exception e){
      // ignore
    }
  }

  /**
   * Closes the supplied Reader ignoring null and any exceptions.
   *
   * @param stream The Reader to close.
   */
  public static void closeQuietly(Reader stream)
  {
    try{
      if(stream != null){
        stream.close();
      }
    }catch(Exception e){
      // ignore
    }
  }

  /**
   * Closes the supplied Writer ignoring null and any exceptions.
   *
   * @param stream The Writer to close.
   */
  public static void closeQuietly(Writer stream)
  {
    try{
      if(stream != null){
        stream.close();
      }
    }catch(Exception e){
      // ignore
    }
  }

  /**
   * Copy the contents of the supplied InputStream to the specified OutputStream.
   *
   * @param in The InputStream.
   * @param out The OutputStream.
   */
  public static void copy(InputStream in, OutputStream out)
    throws IOException
  {
    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
    int n = 0;
    while (-1 != (n = in.read(buffer))) {
      out.write(buffer, 0, n);
    }
  }

  /**
   * Copy the contents of the supplied Reader to the specified Writer.
   *
   * @param in The Reader.
   * @param out The Writer.
   */
  public static void copy(Reader in, Writer out)
    throws IOException
  {
    char[] buffer = new char[DEFAULT_BUFFER_SIZE];
    int n = 0;
    while (-1 != (n = in.read(buffer))) {
      out.write(buffer, 0, n);
    }
  }

  /**
   * Reads the contents from the supplied InputStream and returns those contents
   * as a String.
   *
   * @param in The InputStream to read.
   * @return The InputStream contents.
   */
  public static String toString(InputStream in)
    throws IOException
  {
    return toString(new InputStreamReader(in));
  }

  /**
   * Reads the contents from the supplied Reader and returns those contents
   * as a String.
   *
   * @param in The Reader to read.
   * @return The Reader contents.
   */
  public static String toString(Reader in)
    throws IOException
  {
    StringWriter out = new StringWriter();
    copy(in, out);
    return out.toString();
  }
}
