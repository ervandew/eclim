/**
 * Copyright (C) 2005 - 2016  Eric Van Dewoestine
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
package org.eclim.plugin.pdt.command.complete;

import java.io.FileWriter;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import org.eclim.Eclim;

import org.eclim.plugin.pdt.Pdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for CodeCompleteCommand.
 *
 * @author Eric Van Dewoestine
 */
public class CodeCompleteCommandTest
{
  private static final String TEST_FILE = "php/complete/test.php";
  private static final String TEST_FILE_ERRATIC = "test.php";

  @Test
  @SuppressWarnings("unchecked")
  public void completeAll()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Pdt.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "php_complete", "-p", Pdt.TEST_PROJECT, "-f", TEST_FILE,
        "-o", "258", "-e", "utf-8"
      });

    assertEquals("Wrong number of results", 3, results.size());

    Map<String,Object> result = results.get(0);
    assertEquals(result.get("completion"), "variable1");
    assertEquals(result.get("menu"), "$variable1");
    assertEquals(result.get("info"), "$variable1");

    result = results.get(1);
    assertEquals(result.get("completion"), "methodA1(");
    assertEquals(result.get("menu"), "methodA1($str) : void - TestA");
    assertEquals(result.get("info"), "TestA::methodA1($str) : void");

    result = results.get(2);
    assertEquals(result.get("completion"), "methodA2()");
    assertEquals(result.get("menu"), "methodA2() : void - TestA");
    assertEquals(result.get("info"), "TestA::methodA2() : void");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void completePrefix()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Pdt.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "php_complete", "-p", Pdt.TEST_PROJECT, "-f", TEST_FILE,
        "-o", "273", "-e", "utf-8"
      });

    assertEquals("Wrong number of results", 2, results.size());

    Map<String,Object> result = results.get(0);
    assertEquals(result.get("completion"), "methodA1(");
    assertEquals(result.get("menu"), "methodA1($str) : void - TestA");
    assertEquals(result.get("info"), "TestA::methodA1($str) : void");

    result = results.get(1);
    assertEquals(result.get("completion"), "methodA2()");
    assertEquals(result.get("menu"), "methodA2() : void - TestA");
    assertEquals(result.get("info"), "TestA::methodA2() : void");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void completeMagic()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Pdt.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "php_complete", "-p", Pdt.TEST_PROJECT, "-f", TEST_FILE,
        "-o", "339", "-e", "utf-8"
      });

    assertEquals("Wrong number of results", 1, results.size());

    Map<String,Object> result = results.get(0);
    assertEquals(result.get("completion"), "regular");
    assertEquals(result.get("menu"), "$regular : mixed");
    assertEquals(result.get("info"), "$regular regular read/write property<br/>Type:  mixed");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void completeNamespace()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Pdt.TEST_PROJECT));

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "php_complete", "-p", Pdt.TEST_PROJECT, "-f", TEST_FILE,
        "-o", "133", "-e", "utf-8"
      });

    assertEquals("Wrong number of results", 3, results.size());

    Map<String,Object> result = results.get(0);
    assertEquals(result.get("completion"), "Common");
    assertEquals(result.get("menu"), "Common - Eclim");
    assertEquals(result.get("info"), "Eclim\\Common");

    result = results.get(1);
    assertEquals(result.get("completion"), "Test\\");
    assertEquals(result.get("menu"), "Eclim\\Test");
    assertEquals(result.get("info"), "Eclim\\Test");

    result = results.get(2);
    assertEquals(result.get("completion"), "Test\\Nested\\");
    assertEquals(result.get("menu"), "Eclim\\Test\\Nested");
    assertEquals(result.get("info"), "Eclim\\Test\\Nested");

    results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "php_complete", "-p", Pdt.TEST_PROJECT, "-f", TEST_FILE,
        "-o", "349", "-e", "utf-8"
      });

    assertEquals("Wrong number of results", 1, results.size());

    result = results.get(0);
    assertEquals(result.get("completion"), "Lib\\");
    assertEquals(result.get("menu"), "App\\Lib");
    assertEquals(result.get("info"), "App\\Lib");

    results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "php_complete", "-p", Pdt.TEST_PROJECT, "-f", TEST_FILE,
        "-o", "363", "-e", "utf-8"
      });

    assertEquals("Wrong number of results", 3, results.size());

    result = results.get(0);
    assertEquals(result.get("completion"), "MyFunction()");
    assertEquals(result.get("menu"), "MyFunction() : mixed - App\\Lib");
    assertEquals(result.get("info"), "App\\Lib::MyFunction() : mixed");

    result = results.get(1);
    assertEquals(result.get("completion"), "MyClass");
    assertEquals(result.get("menu"), "MyClass - App\\Lib");
    assertEquals(result.get("info"), "App\\Lib\\MyClass");

    result = results.get(2);
    assertEquals(result.get("completion"), "MYCONST");
    assertEquals(result.get("menu"), "MYCONST");
    assertEquals(result.get("info"), "MYCONST = 'App\\Lib\\MYCONST'");
  }

  /**
   * Test the case where pdt will complete the first completion attempt, but
   * return no results for the second attempt on a new line (sometimes takes
   * several lines before the problem surfaces).
   */
  @Test
  @SuppressWarnings("unchecked")
  public void completeErratic()
    throws Exception
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Pdt.TEST_PROJECT));

    String[] contents = new String[]{
      "<?php",
      "",
      "class Test {",
      "  function getName() {",
      "  }",
      "  function getValue() {",
      "  }",
      "}",
      "",
      "$test = new Test();",
      "$test->;"
    };

    int index = 105;
    for (int ii = 0 ; ii < 10; ii++){
      FileWriter out = new FileWriter(Eclim.resolveFile(Pdt.TEST_PROJECT, TEST_FILE_ERRATIC));
      out.write(StringUtils.join(contents, "\n"));
      out.close();

      List<Map<String,Object>> results = (List<Map<String,Object>>)
        Eclim.execute(new String[]{
          "php_complete", "-p", Pdt.TEST_PROJECT, "-f", TEST_FILE_ERRATIC,
          "-o", String.valueOf(index), "-e", "utf-8"
        });

      assertEquals("Wrong number of results", 2, results.size());

      Map<String,Object> result = results.get(0);
      assertEquals(result.get("completion"), "getName()");
      assertEquals(result.get("menu"), "getName() : void - Test");
      assertEquals(result.get("info"), "Test::getName() : void");

      result = results.get(1);
      assertEquals(result.get("completion"), "getValue()");
      assertEquals(result.get("menu"), "getValue() : void - Test");
      assertEquals(result.get("info"), "Test::getValue() : void");

      String[] newContents = new String[contents.length + 1];
      System.arraycopy(contents, 0, newContents, 0, contents.length - 1);
      newContents[contents.length - 1] = "$test->getName();";
      newContents[contents.length] = "$test->;";
      contents = newContents;
      index += 18;
      // sleep a little bit since no human can type this fast and the pdt
      // appears to need a little time to update.
      Thread.sleep(1000);
    }
  }
}
