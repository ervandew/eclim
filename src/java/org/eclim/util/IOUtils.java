/**
 * Copyright (c) 2005 - 2007
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
 * @version $Revision$
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
  public static void closeQuietly (InputStream stream)
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
  public static void closeQuietly (OutputStream stream)
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
  public static void closeQuietly (Reader stream)
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
  public static void closeQuietly (Writer stream)
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
  public static void copy (InputStream in, OutputStream out)
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
  public static void copy (Reader in, Writer out)
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
  public static String toString (InputStream in)
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
  public static String toString (Reader in)
    throws IOException
  {
    StringWriter out = new StringWriter();
    copy(in, out);
    return out.toString();
  }
}
