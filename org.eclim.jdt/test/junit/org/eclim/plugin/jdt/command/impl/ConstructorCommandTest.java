/**
 * Copyright (C) 2005 - 2012  Eric Van Dewoestine
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

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for ConstructorCommand.
 *
 * @author Eric Van Dewoestine
 */
public class ConstructorCommandTest
{
  private static final String TEST_FILE =
    "src/org/eclim/test/impl/TestConstructor.java";
  private static final String TEST_ENUM_FILE =
    "src/org/eclim/test/impl/TestConstructorEnum.java";
  private static final String TEST_SUPER_FILE =
    "src/org/eclim/test/impl/TestConstructorSuper.java";

  @Test
  public void emptyConstructor()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Eclim.execute(new String[]{
      "java_constructor", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_ENUM_FILE, "-o", "1", "-e", "utf-8"
    });

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_ENUM_FILE);
    assertTrue("Constructor not found.",
        Pattern.compile("^\\s+TestConstructorEnum\\(\\)", Pattern.MULTILINE)
        .matcher(contents).find());
  }

  @Test
  public void argConstructor()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Eclim.execute(new String[]{
      "java_constructor", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_SUPER_FILE,
      "-o", "1", "-e", "utf-8", "-r", "[\"id\", \"name\"]"
    });

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_SUPER_FILE);
    assertTrue("Constructor not found.",
        Pattern.compile("public TestConstructorSuper\\(int id, String name\\)")
        .matcher(contents).find());
  }

  @Test
  public void nestedConstructor()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Eclim.execute(new String[]{
      "java_constructor", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE, "-o", "255", "-e", "utf-8"
    });

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue("Constructor not found.",
        Pattern.compile("public TestInner\\(\\)\\s*\\{\\s*\\}")
        .matcher(contents).find());
  }

  @Test
  public void nestedArgConstructor()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Eclim.execute(new String[]{
      "java_constructor", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE, "-o", "255", "-e", "utf-8", "-r", "[\"subName\"]"
    });

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue("Constructor not found.",
        Pattern.compile(
          "public TestInner\\(String subName\\)\\s*\\{\\s*this\\.subName = subName;\\s*\\}")
        .matcher(contents).find());
  }

  @Test
  public void enumConstructor()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Eclim.execute(new String[]{
      "java_constructor", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_ENUM_FILE, "-o", "1", "-e", "utf-8", "-r", "[\"number\"]"
    });

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_ENUM_FILE);
    assertTrue("Constructor not found.",
        Pattern.compile("^\\s+TestConstructorEnum\\(int number\\)", Pattern.MULTILINE)
        .matcher(contents).find());
  }

  @Test
  public void anonymousConstructor()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = (String)Eclim.execute(new String[]{
      "java_constructor", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE, "-o", "217", "-e", "utf-8", "-r", "[\"number\"]"
    });

    System.out.println(result);
    assertEquals(
        "Anonymous classes cannot contain explicitly declared constructors.", result);
  }

  @Test
  public void superConstructor()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Eclim.execute(new String[]{
      "java_constructor", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE, "-o", "1", "-e", "utf-8", "-s"
    });

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue("Constructor not found.",
        Pattern.compile(
          "public TestConstructor\\(int id, String name\\)\\s*\\{\\s*super\\(id, name\\);\\s*\\}")
        .matcher(contents).find());

    Eclim.execute(new String[]{
      "java_constructor", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE, "-o", "1", "-e", "utf-8", "-s", "-r", "[\"names\"]"
    });

    contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue("Constructor not found.",
        Pattern.compile(
          "public TestConstructor\\(Set<String> names\\)\\s*\\{\\s*this\\.names = names;\\s*\\}")
        .matcher(contents).find());
  }
}
