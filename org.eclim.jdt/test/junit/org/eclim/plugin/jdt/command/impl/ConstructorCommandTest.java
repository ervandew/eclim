/**
 * Copyright (C) 2005 - 2020  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.impl;

import java.util.regex.Pattern;

import org.eclim.Eclim;
import org.eclim.EclimTestCase;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for ConstructorCommand.
 *
 * @author Eric Van Dewoestine
 */
public class ConstructorCommandTest
  extends EclimTestCase
{
  private static final String TEST_FILE =
    "src/org/eclim/test/impl/TestConstructor.java";
  private static final String TEST_ENUM_FILE =
    "src/org/eclim/test/impl/TestConstructorEnum.java";
  private static final String TEST_CHILD_FILE =
    "src/org/eclim/test/impl/TestConstructorChild.java";

  @Test
  public void emptyConstructor()
  {
    modifies(Jdt.TEST_PROJECT, TEST_ENUM_FILE);

    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Eclim.execute(new String[]{
      "java_constructor", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_ENUM_FILE, "-o", "1", "-e", "utf-8",
    });

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_ENUM_FILE);
    assertTrue("Constructor not found.",
        Pattern.compile("^\\s+TestConstructorEnum\\(\\)", Pattern.MULTILINE)
        .matcher(contents).find());
  }

  @Test
  public void argConstructor()
  {
    modifies(Jdt.TEST_PROJECT, TEST_FILE);

    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Eclim.execute(new String[]{
      "java_constructor", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE,
      "-o", "1", "-e", "utf-8", "-r", "[\"id\", \"name\"]",
    });

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue("Constructor not found.",
        Pattern.compile("public TestConstructor\\(int id, String name\\)")
        .matcher(contents).find());
  }

  @Test
  public void nestedConstructor()
  {
    modifies(Jdt.TEST_PROJECT, TEST_FILE);

    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Eclim.execute(new String[]{
      "java_constructor", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE, "-o", "207", "-e", "utf-8",
    });

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue("Constructor not found.",
        Pattern.compile("public TestInner\\(\\)\\s*\\{\\s*\\}")
        .matcher(contents).find());
  }

  @Test
  public void nestedArgConstructor()
  {
    modifies(Jdt.TEST_PROJECT, TEST_FILE);

    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Eclim.execute(new String[]{
      "java_constructor", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE, "-o", "207", "-e", "utf-8", "-r", "[\"subName\"]",
    });

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue("Constructor not found.",
        Pattern.compile(
          "public TestInner\\(String subName\\)\\s*" +
          "\\{\\s*this\\.subName = subName;\\s*\\}")
        .matcher(contents).find());
  }

  @Test
  public void enumConstructor()
  {
    modifies(Jdt.TEST_PROJECT, TEST_ENUM_FILE);

    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Eclim.execute(new String[]{
      "java_constructor", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_ENUM_FILE, "-o", "1", "-e", "utf-8", "-r", "[\"number\"]",
    });

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_ENUM_FILE);
    assertTrue("Constructor not found.",
        Pattern.compile(
          "^\\s+TestConstructorEnum\\(int number\\)",
          Pattern.MULTILINE
        ).matcher(contents).find());
  }

  @Test
  public void anonymousConstructor()
  {
    modifies(Jdt.TEST_PROJECT, TEST_FILE);

    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = (String)Eclim.execute(new String[]{
      "java_constructor", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE, "-o", "198", "-e", "utf-8", "-r", "[\"number\"]",
    });

    assertEquals(
        "Anonymous classes cannot contain explicitly declared constructors.",
        result);
  }

  @Test
  public void superConstructor()
  {
    modifies(Jdt.TEST_PROJECT, TEST_CHILD_FILE);

    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    // now test the subclass constructors
    Eclim.execute(new String[]{
      "java_constructor", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_CHILD_FILE, "-o", "1", "-e", "utf-8", "-s",
    });

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_CHILD_FILE);
    assertTrue("Constructor not found.",
        Pattern.compile(
          "public TestConstructorChild\\(int id, String name\\)\\s*" +
          "\\{\\s*super\\(id, name\\);\\s*\\}")
        .matcher(contents).find());

    Eclim.execute(new String[]{
      "java_constructor", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_CHILD_FILE, "-o", "1", "-e", "utf-8", "-s", "-r", "[\"names\"]",
    });

    contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_CHILD_FILE);
    assertTrue("Constructor not found.",
        Pattern.compile(
          "public TestConstructorChild\\(Set<String> names\\)\\s*" +
          "\\{\\s*this\\.names = names;\\s*\\}")
        .matcher(contents).find());
  }
}
