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
package org.eclim.logging.log4j;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Extension to log4j's ConsoleAppender that holds onto the configured stream
 * instead of continuously grabbing it, preventing calls to System.setOut() or
 * System.setErr() from resulting in undesired behavior.
 *
 * @author Eric Van Dewoestine
 */
public class ConsoleAppender
  extends org.apache.log4j.ConsoleAppender
{
  private boolean writerSet;

  /**
   * Prepares the appender for use.
   */
  public void activateOptions()
  {
    if(getTarget().equals(SYSTEM_ERR)) {
      setWriter(createWriter(new SystemStream(System.err)));
    } else {
      setWriter(createWriter(new SystemStream(System.out)));
    }

    super.activateOptions();
  }

  @Override
  public synchronized void setWriter(Writer writer)
  {
    if(!writerSet){
      super.setWriter(writer);
      writerSet = true;
    }
  }

  /**
   * An implementation of OutputStream that redirects to the
   * stream supplied at construction.
   */
  private static class SystemStream
    extends OutputStream
  {
    private OutputStream out;

    /**
     * Wraps the supplied stream, System.out or System.err.
     *
     * @param out The stream to wrap.
     */
    public SystemStream (OutputStream out)
    {
      this.out = out;
    }

    @Override
    public void close()
    {
      // don't close System.out or System.err
    }

    @Override
    public void flush()
      throws IOException
    {
      out.flush();
    }

    @Override
    public void write(final byte[] bytes)
      throws IOException
    {
      out.write(bytes);
    }

    @Override
    public void write(final byte[] bytes, final int offset, final int length)
      throws IOException
    {
      out.write(bytes, offset, length);
    }

    @Override
    public void write(final int bytes)
      throws IOException
    {
      out.write(bytes);
    }
  }
}
