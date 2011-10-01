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
package org.eclim.plugin.jdt.command.impl;

import java.util.List;
import java.util.Map;

import java.util.regex.Pattern;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for ImplCommand.
 *
 * @author Eric Van Dewoestine
 */
public class ImplCommandTest
{
  private static final String TEST_FILE =
    "src/org/eclim/test/impl/TestImpl.java";
  private static final String TEST_SUB_FILE =
    "src/org/eclim/test/impl/TestSubImpl.java";

  @Test
  @SuppressWarnings("unchecked")
  public void execute()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Map<String,Object> result = (Map<String,Object>)
      Eclim.execute(new String[]{
        "java_impl", "-p", Jdt.TEST_PROJECT,
        "-f", TEST_FILE,
        "-o", "83", "-e", "utf-8"
      });

    assertEquals("org.eclim.test.impl.TestImpl", result.get("type"));

    List<Map<String,Object>> superTypes =
      (List<Map<String,Object>>)result.get("superTypes");
    assertEquals(7, superTypes.size());

    assertEquals("java.util", superTypes.get(0).get("packageName"));
    assertEquals("public interface Comparator<String>",
        superTypes.get(0).get("signature"));
    assertEquals("public abstract int compare(String o1, String o2)",
        ((List<Map<String,Object>>)
         superTypes.get(0).get("methods")).get(0).get("signature"));
    assertEquals("public abstract boolean equals(Object obj)",
        ((List<Map<String,Object>>)
         superTypes.get(0).get("methods")).get(1).get("signature"));

    assertEquals("java.util", superTypes.get(1).get("packageName"));
    assertEquals("public interface Map<Integer,String>",
        superTypes.get(1).get("signature"));

    result = (Map<String,Object>)
      Eclim.execute(new String[]{
        "java_impl", "-p", Jdt.TEST_PROJECT,
        "-f", TEST_FILE,
        "-t", "org.eclim.test.impl.TestImpl",
        "-s", "java.util.Map%3CInteger,String%3E", "-m", "put(Integer,String)"
      });

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue("Method not found or invalid.",
        Pattern.compile("public String put\\(Integer key, String value\\)")
        .matcher(contents).find());

    superTypes = (List<Map<String,Object>>)result.get("superTypes");
    assertEquals("java.util", superTypes.get(1).get("packageName"));
    assertEquals("public interface Map<Integer,String>",
        superTypes.get(1).get("signature"));
    Map<String,Object> map = superTypes.get(1);
    Map<String,Object> put = null;
    for (Map<String,Object> m : (List<Map<String,Object>>)map.get("methods")){
      if (m.get("signature").equals("public abstract String put(Integer key, String value)")){
        put = m;
        break;
      }
    }
    assertNotNull(put);
    assertEquals(true, put.get("implemented"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void executeSub()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Map<String,Object> result = (Map<String,Object>)
      Eclim.execute(new String[]{
        "java_impl", "-p", Jdt.TEST_PROJECT,
        "-f", TEST_SUB_FILE,
        "-o", "83", "-e", "utf-8"
      });

    assertEquals("org.eclim.test.impl.TestSubImpl", result.get("type"));

    List<Map<String,Object>> superTypes =
      (List<Map<String,Object>>)result.get("superTypes");
    assertEquals("java.util", superTypes.get(0).get("packageName"));
    assertEquals("public interface Comparator<String>",
        superTypes.get(0).get("signature"));

    result = (Map<String,Object>)
      Eclim.execute(new String[]{
        "java_impl", "-p", Jdt.TEST_PROJECT,
        "-f", TEST_SUB_FILE,
        "-t", "org.eclim.test.impl.TestSubImpl",
        "-s", "java.util.Map%3CInteger,String%3E", "-m", "put(Integer,String)"
      });

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_SUB_FILE);
    assertTrue("Method not found or invalid.",
        Pattern.compile("public String put\\(Integer key, String value\\)")
        .matcher(contents).find());

    superTypes = (List<Map<String,Object>>)result.get("superTypes");
    assertEquals("java.util", superTypes.get(1).get("packageName"));
    assertEquals("public interface Map<Integer,String>",
        superTypes.get(1).get("signature"));
    Map<String,Object> map = superTypes.get(1);
    Map<String,Object> put = null;
    for (Map<String,Object> m : (List<Map<String,Object>>)map.get("methods")){
      if (m.get("signature").equals("public abstract String put(Integer key, String value)")){
        put = m;
        break;
      }
    }
    assertNotNull(put);
    assertEquals(true, put.get("implemented"));

    result = (Map<String,Object>)
      Eclim.execute(new String[]{
        "java_impl", "-p", Jdt.TEST_PROJECT,
        "-f", TEST_SUB_FILE,
        "-t", "org.eclim.test.impl.TestSubImpl",
        "-s", "java.util.Comparator%3CString%3E", "-m", "compare(String,String)"
      });

    contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_SUB_FILE);
    assertTrue("Method not found or invalid.",
        Pattern.compile("public int compare\\(String o1, String o2\\)")
        .matcher(contents).find());

    superTypes = (List<Map<String,Object>>)result.get("superTypes");
    assertEquals("java.util", superTypes.get(0).get("packageName"));
    assertEquals("public interface Comparator<String>",
        superTypes.get(0).get("signature"));
    assertEquals("public abstract int compare(String o1, String o2)",
        ((List<Map<String,Object>>)
         superTypes.get(0).get("methods")).get(0).get("signature"));
    assertEquals(true,
        ((List<Map<String,Object>>)
         superTypes.get(0).get("methods")).get(0).get("implemented"));
  }
}
