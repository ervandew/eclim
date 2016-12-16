/**
 * Copyright (C) 2014  Eric Van Dewoestine
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
package org.eclim;

import java.io.FileWriter;

import java.util.HashMap;
import java.util.Map;

import org.eclim.util.IOUtils;
import org.eclim.util.StringUtils;

import org.junit.After;

/**
 * Optional super class for eclim test cases which provides some utilities/hooks
 * for dealing with test setup/cleanup.
 *
 * @author Eric Van Dewoestine
 */
public class EclimTestCase
{
  private HashMap<String, String> modified = new HashMap<String, String>();

  @After
  public void resetModified()
      throws Exception
  {
    for (Map.Entry<String, String> entry : modified.entrySet()) {
      String[] parts = StringUtils.split(entry.getKey(), '|');
      String project = parts[0];
      String file = parts[1];
      System.out.println("Restoring file. project: " + project + " file: " + file);
      String path = Eclim.resolveFile(project, file);
      FileWriter writer = new FileWriter(path);
      writer.write(entry.getValue());
      IOUtils.closeQuietly(writer);
    }
  }

  /**
   * Indicates that the test is going to modify the specified file from the
   * supplied project. After the test runs, that file will be restored to it's
   * original contents.
   *
   * @param project
   *          The name of the project the file resides in.
   * @param file
   *          The project relative path of the file.
   */
  protected void modifies(String project, String file)
  {
    String key = project + '|' + file;
    if (!modified.containsKey(key)) {
      String contents = Eclim.fileToString(project, file);
      modified.put(key, contents);
    }
  }
}
