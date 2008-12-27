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
package org.eclim.plugin.jdt.command.constructor;

import java.util.regex.Pattern;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for ConstructorCommand.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class ConstructorCommandTest
{
  private static final String TEST_FILE =
    "src/org/eclim/test/constructor/TestConstructor.java";

  @Test
  public void emptyConstructor()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Eclim.execute(new String[]{
      "java_constructor", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE, "-o", "1", "-e", "utf-8"
    });

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue("Constructor not found.",
        Pattern.compile("public TestConstructor\\(\\)")
        .matcher(contents).find());
  }

  @Test
  public void argConstructor()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Eclim.execute(new String[]{
      "java_constructor", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE,
      "-o", "1", "-e", "utf-8", "-r", "id,name"
    });

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue("Constructor not found.",
        Pattern.compile("public TestConstructor\\(int id, String name\\)")
        .matcher(contents).find());
  }
}
