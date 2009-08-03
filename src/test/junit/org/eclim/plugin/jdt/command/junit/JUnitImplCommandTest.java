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
package org.eclim.plugin.jdt.command.junit;

import java.util.regex.Pattern;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for JUnitImplCommand.
 *
 * @author Eric Van Dewoestine
 */
public class JUnitImplCommandTest
{
  private static final String TEST_FILE =
    "src/org/eclim/test/junit/SomeClassTest.java";

  @Test
  public void execute()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "java_junit_impl", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE,
      "-b", "org.eclim.test.junit.SomeClass"
    });

    System.out.println(result);

    String valid =
      "org.eclim.test.junit.SomeClassTest\n" +
      "\n" +
      "package org.eclim.test.junit;\n" +
      "public class SomeClass {\n" +
      "\tpublic void aMethod()\n" +
      "\tpublic void aMethod(String name)\n" +
      "\tpublic void anotherMethod(int id)\n" +
      "}\n" +
      "\n" +
      "package java.util;\n" +
      "public interface Comparator<String> {\n" +
      "\tpublic abstract int compare(String o1, String o2)\n" +
      "\tpublic abstract boolean equals(Object obj)\n" +
      "}\n" +
      "\n" +
      "package java.lang;\n" +
      "public class Object {\n" +
      "\tpublic Object()\n" +
      "\tpublic int hashCode()\n" +
      "\tpublic boolean equals(Object obj)\n" +
      "\tprotected Object clone()\n" +
      "\t\tthrows CloneNotSupportedException\n" +
      "\tpublic String toString()\n" +
      "\tprotected void finalize()\n" +
      "\t\tthrows Throwable\n" +
      "}";

    assertEquals("Wrong results.", valid, result);

    result = Eclim.execute(new String[]{
      "java_junit_impl", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE,
      "-b", "org.eclim.test.junit.SomeClass",
      "-t", "org.eclim.test.junit.SomeClassTest",
      "-s", "org.eclim.test.junit.SomeClass",
      "-m", "aMethod(String)"
    });

    System.out.println(result);

    valid =
      "org.eclim.test.junit.SomeClassTest\n" +
      "\n" +
      "package org.eclim.test.junit;\n" +
      "public class SomeClass {\n" +
      "\t//public void aMethod()\n" +
      "\t//public void aMethod(String name)\n" +
      "\tpublic void anotherMethod(int id)\n" +
      "}\n" +
      "\n" +
      "package java.util;\n" +
      "public interface Comparator<String> {\n" +
      "\tpublic abstract int compare(String o1, String o2)\n" +
      "\tpublic abstract boolean equals(Object obj)\n" +
      "}\n" +
      "\n" +
      "package java.lang;\n" +
      "public class Object {\n" +
      "\tpublic Object()\n" +
      "\tpublic int hashCode()\n" +
      "\tpublic boolean equals(Object obj)\n" +
      "\tprotected Object clone()\n" +
      "\t\tthrows CloneNotSupportedException\n" +
      "\tpublic String toString()\n" +
      "\tprotected void finalize()\n" +
      "\t\tthrows Throwable\n" +
      "}";

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue("Method not found or invalid.",
        Pattern.compile("@Test\n\\s+public void aMethod\\(\\)")
        .matcher(contents).find());

    assertEquals("Wrong results.", valid, result);

    result = Eclim.execute(new String[]{
      "java_junit_impl", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE,
      "-b", "org.eclim.test.junit.SomeClass",
      "-t", "org.eclim.test.junit.SomeClassTest",
      "-s", "java.util.Comparator<String>",
      "-m", "compare(String,String)"
    });

    System.out.println(result);

    valid =
      "org.eclim.test.junit.SomeClassTest\n" +
      "\n" +
      "package org.eclim.test.junit;\n" +
      "public class SomeClass {\n" +
      "\t//public void aMethod()\n" +
      "\t//public void aMethod(String name)\n" +
      "\tpublic void anotherMethod(int id)\n" +
      "}\n" +
      "\n" +
      "package java.util;\n" +
      "public interface Comparator<String> {\n" +
      "\t//public abstract int compare(String o1, String o2)\n" +
      "\tpublic abstract boolean equals(Object obj)\n" +
      "}\n" +
      "\n" +
      "package java.lang;\n" +
      "public class Object {\n" +
      "\tpublic Object()\n" +
      "\tpublic int hashCode()\n" +
      "\tpublic boolean equals(Object obj)\n" +
      "\tprotected Object clone()\n" +
      "\t\tthrows CloneNotSupportedException\n" +
      "\tpublic String toString()\n" +
      "\tprotected void finalize()\n" +
      "\t\tthrows Throwable\n" +
      "}";

    contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue("Method not found or invalid.",
        Pattern.compile("@Test\n\\s+public void compare\\(\\)")
        .matcher(contents).find());

    assertEquals("Wrong results.", valid, result);
  }
}
