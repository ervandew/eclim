/**
 * Copyright (c) 2005 - 2006
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
package org.eclim.logging.log4j;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Extension to log4j's ConsoleAppender that holds onto the configured stream
 * instead of continuously grabbing it, preventing calls to System.setOut() or
 * System.setErr() from resulting in undesired behavior.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
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

  /**
   * {@inheritDoc}
   */
  public synchronized void setWriter (Writer _writer)
  {
    if(!writerSet){
      super.setWriter(_writer);
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
     * @param _out The stream to wrap.
     */
    public SystemStream (OutputStream _out)
    {
      out = _out;
    }

    /**
     * {@inheritDoc}
     */
    public void close ()
    {
      // don't close System.out or System.err
    }

    /**
     * {@inheritDoc}
     */
    public void flush ()
      throws IOException
    {
      out.flush();
    }

    /**
     * {@inheritDoc}
     */
    public void write (final byte[] _b)
      throws IOException
    {
      out.write(_b);
    }

    /**
     * {@inheritDoc}
     */
    public void write (final byte[] _b, final int _off, final int _len)
      throws IOException
    {
      out.write(_b, _off, _len);
    }

    /**
     * {@inheritDoc}
     */
    public void write (final int _b)
      throws IOException
    {
      out.write(_b);
    }
  }
}
