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
package org.eclim.plugin.jdt.command.doc;

import java.util.List;
import java.util.Map;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for GetElementDocCommand.
 *
 * @author Eric Van Dewoestine
 */
public class GetElementDocCommandTest
{
  private static final String TEST_FILE =
    "src/org/eclim/test/doc/TestPreview.java";

  @Test
  @SuppressWarnings("unchecked")
  public void executeLocalElements()
  {
    // class reference: TestPreview
    Map<String, Object> result = (Map<String, Object>)
      Eclim.execute(new String[]{
        "java_element_doc", "-p", Jdt.TEST_PROJECT,
        "-f", TEST_FILE, "-o", "255", "-l", "11", "-e", "utf-8",
      });
    assertEquals(2, result.size());
    assertEquals(
        "|org[0]|.|eclim[1]|.|test[2]|.|doc[3]|.TestPreview\n\n" +
        "A test class for javadoc previews.\n" +
        "Author:\n\t Eric Van Dewoestine\n", result.get("text"));
    List<Map<String, String>> links =
      (List<Map<String, String>>)result.get("links");
    assertEquals(4, links.size());

    // constructor call: TestPreview(String[])
    result = (Map<String, Object>)Eclim.execute(new String[]{
      "java_element_doc", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE, "-o", "275", "-l", "11", "-e", "utf-8",
    });
    assertEquals(2, result.size());
    assertEquals(
        "|org[0]|." +
        "|eclim[1]|." +
        "|test[2]|." +
        "|doc[3]|." +
        "|TestPreview[4]|." +
        "TestPreview(|String[5]|[] args)\n\n" +
        "Constructs a new instance from the supplied arguments.\n" +
        "Parameters:\n\targs The arguments.\n", result.get("text"));
    links = (List<Map<String, String>>)result.get("links");
    assertEquals(6, links.size());
    assertEquals("TestPreview", links.get(4).get("text"));
    assertTrue(links.get(4).get("href").startsWith("eclipse-javadoc:"));
    assertEquals("String", links.get(5).get("text"));
    assertTrue(links.get(5).get("href").startsWith("eclipse-javadoc:"));

    // method call: test()
    result = (Map<String, Object>)Eclim.execute(new String[]{
      "java_element_doc", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE, "-o", "300", "-l", "4", "-e", "utf-8",
    });
    assertEquals(2, result.size());
    assertEquals(
        "|String[0]| " +
        "|org[1]|." +
        "|eclim[2]|." +
        "|test[3]|." +
        "|doc[4]|." +
        "|TestPreview[5]|" +
        ".test()\n\n" +
        "A test method.\nReturns:\n\t a test |String[6]|\n", result.get("text"));
    links = (List<Map<String, String>>)result.get("links");
    assertEquals(7, links.size());
    assertEquals("String", links.get(0).get("text"));
    assertTrue(links.get(0).get("href").startsWith("eclipse-javadoc:"));
    assertEquals("TestPreview", links.get(5).get("text"));
    assertTrue(links.get(5).get("href").startsWith("eclipse-javadoc:"));
    assertEquals("String", links.get(6).get("text"));
    assertTrue(links.get(6).get("href").startsWith("eclipse-javadoc:"));

    // follow url: String
    result = (Map<String, Object>)Eclim.execute(new String[]{
      "java_element_doc", "-u", links.get(0).get("href"),
    });
    assertEquals(2, result.size());
    String text = (String)result.get("text");
    assertTrue(text.startsWith("|java[0]|.|lang[1]|.String\n\nThe String class"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void executeJdkElements()
  {
    // ambiguous reference: List
    Map<String, Object> result = (Map<String, Object>)
      Eclim.execute(new String[]{
        "java_element_doc", "-p", Jdt.TEST_PROJECT,
        "-f", TEST_FILE, "-o", "169", "-l", "4", "-e", "utf-8",
      });
    assertEquals(2, result.size());
    String text = (String)result.get("text");
    // The first run (any in the first second or so) puts java.util.List last,
    // so try that first, then fallback to the order it'll be for all subsequent
    // executions.
    int index = 0;
    if (text.indexOf("java.util.List[2]") != -1){
      index = 2;
      assertEquals(
          "\n\t- |java.awt.List[0]|" +
          "\n\t- |com.sun.tools.javac.util.List[1]|" +
          "\n\t- |java.util.List[2]|",
          result.get("text"));
      List<Map<String, String>> links =
        (List<Map<String, String>>)result.get("links");
      assertEquals(3, links.size());
      assertEquals("java.awt.List", links.get(0).get("text"));
      assertTrue(links.get(0).get("href").startsWith("eclipse-javadoc:"));
      assertEquals("java.util.List", links.get(2).get("text"));
      assertTrue(links.get(2).get("href").startsWith("eclipse-javadoc:"));
    }else{
      index = 1;
      assertEquals(
          "\n\t- |java.awt.List[0]|" +
          "\n\t- |java.util.List[1]|" +
          "\n\t- |com.sun.tools.javac.util.List[2]|",
          result.get("text"));
      List<Map<String, String>> links =
        (List<Map<String, String>>)result.get("links");
      assertEquals(3, links.size());
      assertEquals("java.awt.List", links.get(0).get("text"));
      assertTrue(links.get(0).get("href").startsWith("eclipse-javadoc:"));
      assertEquals("java.util.List", links.get(1).get("text"));
      assertTrue(links.get(1).get("href").startsWith("eclipse-javadoc:"));
    }

    // follow url: java.util.List
    List<Map<String, String>> links =
      (List<Map<String, String>>)result.get("links");
    result = (Map<String, Object>)Eclim.execute(new String[]{
      "java_element_doc", "-u", links.get(index).get("href"),
    });
    assertEquals(2, result.size());
    text = (String)result.get("text");
    assertTrue(text.startsWith(
          "|java[0]|.|util[1]|.List<|E[2]|>\n\nAn ordered collection"));

    // method call: Map.put
    result = (Map<String, Object>)Eclim.execute(new String[]{
      "java_element_doc", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE, "-o", "574", "-l", "3", "-e", "utf-8",
    });
    assertEquals(2, result.size());
    text = (String)result.get("text");
    assertTrue(text.startsWith(
        "|Object[0]| " +
        "|java[1]|." +
        "|util[2]|." +
        "|Map[3]|" +
        ".put(|Object[4]| key, |Object[5]| value)\n\n" +
        "Associates the specified value with the specified key in this map"));
  }

  @Test
  public void htmlFlag()
  {
    String results = (String)
      Eclim.execute(new String[]{
        "java_element_doc", "-p", Jdt.TEST_PROJECT,
        "-f", TEST_FILE, "-o", "255", "-l", "11", "-e", "utf-8", "-h",
      });
    assertTrue("HTML Format expected", results.startsWith("<html><head>"));
  }
}
