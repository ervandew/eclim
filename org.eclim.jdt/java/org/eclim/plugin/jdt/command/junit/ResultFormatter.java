/**
 * Copyright (C) 2012 - 2020  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.junit;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.tools.ant.BuildException;

import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.taskdefs.optional.junit.PlainJUnitResultFormatter;

/**
 * Custom junit formatter intended to be be easier to read than the default
 * 'plain' formatter when running multiple tests.
 *
 * @author Eric Van Dewoestine
 */
public class ResultFormatter
  extends PlainJUnitResultFormatter
{
  private OutputStream out;

  @Override
  public void setOutput(OutputStream out)
  {
    super.setOutput(out);
    this.out = out;
  }

  @Override
  public void startTestSuite(JUnitTest suite)
    throws BuildException
  {
    if (out == null) {
      return;
    }

    String name = suite.getName();
    StringBuffer nameDelim = new StringBuffer();
    for (int i = 0; i < name.length(); i++){
      nameDelim.append('-');
    }
    StringBuffer sb = new StringBuffer();
    String newline = System.lineSeparator();
    sb.append(newline);
    sb.append("-----------").append(nameDelim).append(newline);
    sb.append("Testsuite: ");
    sb.append(name);
    sb.append(newline);
    sb.append("-----------").append(nameDelim).append(newline);
    try {
      out.write(sb.toString().getBytes());
      out.flush();
    } catch (IOException ex) {
      throw new BuildException("Unable to write output", ex);
    }
  }
}
