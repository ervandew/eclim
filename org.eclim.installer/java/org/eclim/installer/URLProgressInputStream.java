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
package org.eclim.installer;

import java.io.FilterInputStream;
import java.io.IOException;

import java.net.URLConnection;

import javax.swing.JProgressBar;

/**
 * Input stream which monitors the download progress from the supplied url
 * connection and updates the supplied progress bar accordingly.
 *
 * @author Eric Van Dewoestine
 */
public class URLProgressInputStream
  extends FilterInputStream
{
  private JProgressBar progressBar;

  public URLProgressInputStream(JProgressBar progressBar, URLConnection con)
    throws IOException
  {
    super(con.getInputStream());
    this.progressBar = progressBar;
    progressBar.setValue(0);
    progressBar.setMaximum(con.getContentLength());
    progressBar.setIndeterminate(false);
  }

  @Override
  public int read()
    throws IOException
  {
    int value = super.read();
    progressBar.setValue(progressBar.getValue() + 1);
    return value;
  }

  @Override
  public int read(byte[] b)
    throws IOException
  {
    return read(b, 0, b.length);
  }

  @Override
  public int read(byte[] b, int off, int len)
    throws IOException
  {
    int result = super.read(b, off, len);
    if (result < 0){ // no more bytes in the stream
      // setting to max value may not always be correct.
      progressBar.setValue(progressBar.getMaximum());
    }else{
      progressBar.setValue(progressBar.getValue() + result);
    }
    return result;
  }

  @Override
  public long skip(long n)
    throws IOException
  {
    long result = super.skip(n);
    progressBar.setValue(progressBar.getValue() + (int)result);
    return result;
  }
}
