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
package org.eclim.plugin.jdt.command.junit;

import java.util.regex.Pattern;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for JUnitImplCommand.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class JUnitImplCommandTest
{
  private static final String TEST_FILE =
    "src/org/eclim/test/junit/SomeClassTest.java";

  @Test
  public void execute ()
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
      "\tpublic void aMethod ()\n" +
      "\tpublic void aMethod (String name)\n" +
      "\tpublic void anotherMethod (int id)\n" +
      "}\n" +
      "\n" +
      "package java.lang;\n" +
      "public class Object {\n" +
      "\tpublic Object ()\n" +
      "\tpublic int hashCode ()\n" +
      "\tpublic boolean equals (Object obj)\n" +
      "\tprotected Object clone ()\n" +
      "\t\tthrows CloneNotSupportedException\n" +
      "\tpublic String toString ()\n" +
      "\tprotected void finalize ()\n" +
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

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue("Method not found or invalid.",
        Pattern.compile("public void testAMethod \\(\\)")
        .matcher(contents).find());

    valid =
      "org.eclim.test.junit.SomeClassTest\n" +
      "\n" +
      "package org.eclim.test.junit;\n" +
      "public class SomeClass {\n" +
      "\t//public void aMethod ()\n" +
      "\t//public void aMethod (String name)\n" +
      "\tpublic void anotherMethod (int id)\n" +
      "}\n" +
      "\n" +
      "package java.lang;\n" +
      "public class Object {\n" +
      "\tpublic Object ()\n" +
      "\tpublic int hashCode ()\n" +
      "\tpublic boolean equals (Object obj)\n" +
      "\tprotected Object clone ()\n" +
      "\t\tthrows CloneNotSupportedException\n" +
      "\tpublic String toString ()\n" +
      "\tprotected void finalize ()\n" +
      "\t\tthrows Throwable\n" +
      "}";

    assertEquals("Wrong results.", valid, result);
  }
}
