/**
 * Copyright (C) 2005 - 2011  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.refactoring;

import java.io.File;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for RenameCommand.
 *
 * @author Eric Van Dewoestine
 */
public class RenameCommandTest
{
  @Test
  public void executeRenameField()
    throws Exception
  {
    String p = Eclim.resolveFile(Jdt.TEST_PROJECT, "");

    String tn1Contents = Eclim.fileToString(
        Jdt.TEST_PROJECT,
        "src/org/eclim/test/refactoring/rename/n1/TestN1.java");
    assertTrue("field 'FOO' not found", tn1Contents.indexOf(
          "public static final String FOO = \"value\";") > 0);

    String tn2Contents = Eclim.fileToString(
        Jdt.TEST_PROJECT,
        "src/org/eclim/test/refactoring/rename/n1/n2/TestN2.java");
    assertTrue("import of 'FOO' not found", tn2Contents.indexOf(
          "import static org.eclim.test.refactoring.rename.n1.TestN1.FOO;"
          ) > 0);
    assertTrue("print of 'FOO' not found", tn2Contents.indexOf(
          "System.out.println(FOO);") > 0);

    // preview
    String result = (String)Eclim.execute(new String[]{
      "java_refactor_rename", "-p", Jdt.TEST_PROJECT,
      "-f", "src/org/eclim/test/refactoring/rename/n1/TestN1.java",
      "-n", "BAR", "-o", "98", "-l", "3", "-e", "utf-8", "-v",
    });
    assertEquals("Wrong preview results", result,
        "-pretty,-command,java_refactor_rename,-p,eclim_unit_test_java,-f,src/org/eclim/test/refactoring/rename/n1/TestN1.java,-n,BAR,-o,98,-l,3,-e,utf-8,-v\n" +
        "diff: " + p + "src/org/eclim/test/refactoring/rename/n1/TestN1.java\n" +
        "diff: " + p + "src/org/eclim/test/refactoring/rename/n1/n2/TestN2.java\n");

    // preview diff
    result = (String)Eclim.execute(new String[]{
      "java_refactor_rename", "-p", Jdt.TEST_PROJECT,
      "-f", "src/org/eclim/test/refactoring/rename/n1/TestN1.java",
      "-n", "BAR", "-o", "98", "-l", "3", "-e", "utf-8", "-v",
      "-d", Eclim.resolveFile(Jdt.TEST_PROJECT,
      "src/org/eclim/test/refactoring/rename/n1/n2/TestN2.java"),
    });
    assertEquals("Wrong diff results",
        tn2Contents.replaceAll("FOO", "BAR"), result);

    // rename
    result = (String)Eclim.execute(new String[]{
      "java_refactor_rename", "-p", Jdt.TEST_PROJECT,
      "-f", "src/org/eclim/test/refactoring/rename/n1/TestN1.java",
      "-n", "BAR", "-o", "98", "-l", "3", "-e", "utf-8",
    });

    assertEquals("Wrong rename results",
        "files:\n" +
        p + "src\n" +
        p + "src/org\n" +
        p + "src/org/eclim\n" +
        p + "src/org/eclim/test\n" +
        p + "src/org/eclim/test/refactoring\n" +
        p + "src/org/eclim/test/refactoring/rename\n" +
        p + "src/org/eclim/test/refactoring/rename/n1\n" +
        p + "src/org/eclim/test/refactoring/rename/n1/TestN1.java\n" +
        p + "src/org/eclim/test/refactoring/rename/n1/n2\n" +
        p + "src/org/eclim/test/refactoring/rename/n1/n2/TestN2.java",
        result
        );

    String tn1NewContents = Eclim.fileToString(
        Jdt.TEST_PROJECT,
        "src/org/eclim/test/refactoring/rename/n1/TestN1.java");
    assertEquals("Wrong new contents",
        tn1Contents.replaceAll("FOO", "BAR"), tn1NewContents);

    String tn2NewContents = Eclim.fileToString(
        Jdt.TEST_PROJECT,
        "src/org/eclim/test/refactoring/rename/n1/n2/TestN2.java");
    assertEquals("Wrong new contents",
        tn2Contents.replaceAll("FOO", "BAR"), tn2NewContents);
  }

  @Test
  public void executeRenameMethod()
    throws Exception
  {
    String p = Eclim.resolveFile(Jdt.TEST_PROJECT, "");

    String tn1Contents = Eclim.fileToString(
        Jdt.TEST_PROJECT,
        "src/org/eclim/test/refactoring/rename/n1/TestN1.java");
    assertTrue("method 'testMethod' not found", tn1Contents.indexOf(
          "public void testMethod()") > 0);

    String tn2Contents = Eclim.fileToString(
        Jdt.TEST_PROJECT,
        "src/org/eclim/test/refactoring/rename/n1/n2/TestN2.java");
    assertTrue("method 'testMethod' not found", tn2Contents.indexOf(
          "test.testMethod()") > 0);

    // preview
    String result = (String)Eclim.execute(new String[]{
      "java_refactor_rename", "-p", Jdt.TEST_PROJECT,
      "-f", "src/org/eclim/test/refactoring/rename/n1/n2/TestN2.java",
      "-n", "testRename", "-o", "310", "-l", "10", "-e", "utf-8", "-v",
    });
    assertEquals("Wrong preview results", result,
        "-pretty,-command,java_refactor_rename,-p,eclim_unit_test_java,-f,src/org/eclim/test/refactoring/rename/n1/n2/TestN2.java,-n,testRename,-o,310,-l,10,-e,utf-8,-v\n" +
        "diff: " + p + "src/org/eclim/test/refactoring/rename/n1/TestN1.java\n" +
        "diff: " + p + "src/org/eclim/test/refactoring/rename/n1/n2/TestN2.java\n");

    // preview diff
    result = (String)Eclim.execute(new String[]{
      "java_refactor_rename", "-p", Jdt.TEST_PROJECT,
      "-f", "src/org/eclim/test/refactoring/rename/n1/n2/TestN2.java",
      "-n", "testRename", "-o", "310", "-l", "10", "-e", "utf-8", "-v",
      "-d", Eclim.resolveFile(Jdt.TEST_PROJECT,
      "src/org/eclim/test/refactoring/rename/n1/TestN1.java"),
    });
    assertEquals("Wrong diff results",
        tn1Contents.replaceAll("testMethod", "testRename"), result);

    // rename
    result = (String)Eclim.execute(new String[]{
      "java_refactor_rename", "-p", Jdt.TEST_PROJECT,
      "-f", "src/org/eclim/test/refactoring/rename/n1/n2/TestN2.java",
      "-n", "testRename", "-o", "310", "-l", "10", "-e", "utf-8",
    });

    assertEquals("Wrong rename results",
        "files:\n" +
        p + "src\n" +
        p + "src/org\n" +
        p + "src/org/eclim\n" +
        p + "src/org/eclim/test\n" +
        p + "src/org/eclim/test/refactoring\n" +
        p + "src/org/eclim/test/refactoring/rename\n" +
        p + "src/org/eclim/test/refactoring/rename/n1\n" +
        p + "src/org/eclim/test/refactoring/rename/n1/TestN1.java\n" +
        p + "src/org/eclim/test/refactoring/rename/n1/n2\n" +
        p + "src/org/eclim/test/refactoring/rename/n1/n2/TestN2.java",
        result
        );

    String tn1NewContents = Eclim.fileToString(
        Jdt.TEST_PROJECT,
        "src/org/eclim/test/refactoring/rename/n1/TestN1.java");
    assertEquals("Wrong new contents",
        tn1Contents.replaceAll("testMethod", "testRename"), tn1NewContents);

    String tn2NewContents = Eclim.fileToString(
        Jdt.TEST_PROJECT,
        "src/org/eclim/test/refactoring/rename/n1/n2/TestN2.java");
    assertEquals("Wrong new contents",
        tn2Contents.replaceAll("testMethod", "testRename"), tn2NewContents);
  }

  @Test
  public void executeRenameType()
    throws Exception
  {
    String p = Eclim.resolveFile(Jdt.TEST_PROJECT, "");

    String tn1Contents = Eclim.fileToString(
        Jdt.TEST_PROJECT,
        "src/org/eclim/test/refactoring/rename/n1/TestN1.java");
    assertTrue("class 'TestN1' not found", tn1Contents.indexOf(
          "public class TestN1") > 0);

    String tn2Contents = Eclim.fileToString(
        Jdt.TEST_PROJECT,
        "src/org/eclim/test/refactoring/rename/n1/n2/TestN2.java");
    assertTrue("import of 'TestN1' not found", tn2Contents.indexOf(
          "import org.eclim.test.refactoring.rename.n1.TestN1;") > 0);

    // preview
    String result = (String)Eclim.execute(new String[]{
      "java_refactor_rename", "-p", Jdt.TEST_PROJECT,
      "-f", "src/org/eclim/test/refactoring/rename/n1/TestN1.java",
      "-n", "TestR1", "-o", "60", "-l", "6", "-e", "utf-8", "-v",
    });
    assertEquals("Wrong preview results", result,
        "-pretty,-command,java_refactor_rename,-p,eclim_unit_test_java,-f,src/org/eclim/test/refactoring/rename/n1/TestN1.java,-n,TestR1,-o,60,-l,6,-e,utf-8,-v\n" +
        "diff: " + p + "src/org/eclim/test/refactoring/rename/n1/n2/TestN2.java\n" +
        "other: Rename compilation unit 'TestN1.java' to 'TestR1.java'\n");

    // preview diff
    result = (String)Eclim.execute(new String[]{
      "java_refactor_rename", "-p", Jdt.TEST_PROJECT,
      "-f", "src/org/eclim/test/refactoring/rename/n1/TestN1.java",
      "-n", "TestR1", "-o", "60", "-l", "6", "-e", "utf-8", "-v",
      "-d", Eclim.resolveFile(Jdt.TEST_PROJECT,
      "src/org/eclim/test/refactoring/rename/n1/n2/TestN2.java"),
    });
    assertEquals("Wrong diff results",
        tn2Contents.replaceAll("TestN1", "TestR1"), result);

    // rename
    result = (String)Eclim.execute(new String[]{
      "java_refactor_rename", "-p", Jdt.TEST_PROJECT,
      "-f", "src/org/eclim/test/refactoring/rename/n1/TestN1.java",
      "-n", "TestR1", "-o", "60", "-l", "6", "-e", "utf-8",
    });

    assertEquals("Wrong rename results",
        "files:\n" +
        p + "src\n" +
        p + "src/org\n" +
        p + "src/org/eclim\n" +
        p + "src/org/eclim/test\n" +
        p + "src/org/eclim/test/refactoring\n" +
        p + "src/org/eclim/test/refactoring/rename\n" +
        p + "src/org/eclim/test/refactoring/rename/n1\n" +
        p + "src/org/eclim/test/refactoring/rename/n1/TestN1.java -> " + p + "src/org/eclim/test/refactoring/rename/n1/TestR1.java\n" +
        p + "src/org/eclim/test/refactoring/rename/n1/n2\n" +
        p + "src/org/eclim/test/refactoring/rename/n1/n2/TestN2.java",
        result
        );

    assertFalse(new File(Eclim.resolveFile(
            Jdt.TEST_PROJECT,
            "src/org/eclim/test/refactoring/rename/n1/TestN1.java")).exists());

    String tn1NewContents = Eclim.fileToString(
        Jdt.TEST_PROJECT,
        "src/org/eclim/test/refactoring/rename/n1/TestR1.java");
    assertEquals("Wrong new contents",
        tn1Contents.replaceAll("TestN1", "TestR1"), tn1NewContents);

    String tn2NewContents = Eclim.fileToString(
        Jdt.TEST_PROJECT,
        "src/org/eclim/test/refactoring/rename/n1/n2/TestN2.java");
    assertEquals("Wrong diff results",
        tn2Contents.replaceAll("TestN1", "TestR1"), tn2NewContents);
  }

  @Test
  public void executeRenamePackage()
    throws Exception
  {
    String p = Eclim.resolveFile(Jdt.TEST_PROJECT, "");

    String tr1Contents = Eclim.fileToString(
        Jdt.TEST_PROJECT,
        "src/org/eclim/test/refactoring/rename/n1/TestR1.java");
    assertTrue("package 'n1' not found", tr1Contents.indexOf(
          "package org.eclim.test.refactoring.rename.n1;") != -1);

    String tn2Contents = Eclim.fileToString(
        Jdt.TEST_PROJECT,
        "src/org/eclim/test/refactoring/rename/n1/n2/TestN2.java");
    assertTrue("package 'n1.n2' not found", tn2Contents.indexOf(
          "package org.eclim.test.refactoring.rename.n1.n2;") != -1);

    // preview
    String result = (String)Eclim.execute(new String[]{
      "java_refactor_rename", "-p", Jdt.TEST_PROJECT,
      "-f", "src/org/eclim/test/refactoring/rename/n1/n2/TestN2.java",
      "-n", "org.eclim.test.refactoring.rename.r1", "-o", "42", "-l", "2", "-e", "utf-8", "-v",
    });
    assertEquals("Wrong preview results", result,
        "-pretty,-command,java_refactor_rename,-p,eclim_unit_test_java,-f,src/org/eclim/test/refactoring/rename/n1/n2/TestN2.java,-n,org.eclim.test.refactoring.rename.r1,-o,42,-l,2,-e,utf-8,-v\n" +
        "diff: " + p + "src/org/eclim/test/refactoring/rename/n1/n2/TestN2.java\n" +
        "other: Rename package 'org.eclim.test.refactoring.rename.n1' and subpackages to 'org.eclim.test.refactoring.rename.r1'\n");

    // preview diff
    result = (String)Eclim.execute(new String[]{
      "java_refactor_rename", "-p", Jdt.TEST_PROJECT,
      "-f", "src/org/eclim/test/refactoring/rename/n1/n2/TestN2.java",
      "-n", "org.eclim.test.refactoring.rename.r1", "-o", "42", "-l", "2", "-e", "utf-8", "-v",
      "-d", Eclim.resolveFile(Jdt.TEST_PROJECT,
      "src/org/eclim/test/refactoring/rename/n1/n2/TestN2.java"),
    });
    assertEquals("Wrong diff results",
        tn2Contents.replaceAll("n1\\.TestR1", "r1.TestR1"), result);

    // rename
    result = (String)Eclim.execute(new String[]{
      "java_refactor_rename", "-p", Jdt.TEST_PROJECT,
      "-f", "src/org/eclim/test/refactoring/rename/n1/n2/TestN2.java",
      "-n", "org.eclim.test.refactoring.rename.r1",
      "-o", "42", "-l", "2", "-e", "utf-8",
    });

    assertEquals("Wrong rename results",
        "files:\n" +
        p + "src\n" +
        p + "src/org\n" +
        p + "src/org/eclim\n" +
        p + "src/org/eclim/test\n" +
        p + "src/org/eclim/test/refactoring\n" +
        p + "src/org/eclim/test/refactoring/rename\n" +
        p + "src/org/eclim/test/refactoring/rename/n1\n" +
        p + "src/org/eclim/test/refactoring/rename/r1\n" +
        p + "src/org/eclim/test/refactoring/rename/n1/TestR1.java -> " + p + "src/org/eclim/test/refactoring/rename/r1/TestR1.java\n" +
        p + "src/org/eclim/test/refactoring/rename/n1/n2 -> " + p + "src/org/eclim/test/refactoring/rename/r1/n2\n" +
        p + "src/org/eclim/test/refactoring/rename/n1/n2/TestN2.java -> " + p + "src/org/eclim/test/refactoring/rename/r1/n2/TestN2.java",
        result
        );

    assertFalse(new File(Eclim.resolveFile(
            Jdt.TEST_PROJECT,
            "src/org/eclim/test/refactoring/rename/n1/TestR1.java")).exists());
    String tn1NewContents = Eclim.fileToString(
        Jdt.TEST_PROJECT,
        "src/org/eclim/test/refactoring/rename/r1/TestR1.java");
    assertEquals("Wrong new contents",
        tr1Contents.replaceAll("n1", "r1"), tn1NewContents);

    assertFalse(new File(Eclim.resolveFile(
            Jdt.TEST_PROJECT,
            "src/org/eclim/test/refactoring/rename/n1/n2/TestN1.java")).exists());
    String tn2NewContents = Eclim.fileToString(
        Jdt.TEST_PROJECT,
        "src/org/eclim/test/refactoring/rename/r1/n2/TestN2.java");
    assertEquals("Wrong new contents",
        tn2Contents.replaceAll("n1", "r1"), tn2NewContents);
  }
}
