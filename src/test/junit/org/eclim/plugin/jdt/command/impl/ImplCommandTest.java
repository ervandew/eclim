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
package org.eclim.plugin.jdt.command.impl;

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
  public void execute()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "java_impl", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE,
      "-o", "83", "-e", "utf-8"
    });

    System.out.println(result);

    assertTrue("Wrong first line.",
        result.startsWith("org.eclim.test.impl.TestImpl"));
    assertTrue("Interface not in results.",
        result.indexOf(
          "package java.util;\npublic interface Comparator<String> {") != -1);
    assertTrue("Interface not in results.",
        result.indexOf(
          "package java.util;\npublic interface Map<Integer,String> {") != -1);
    assertTrue("Class not in results.",
        result.indexOf(
          "package java.util;\npublic class HashMap<Integer,String> {") != -1);
    assertTrue("Method not in results.",
        result.indexOf("\tpublic abstract int compare(String o1, String o2)") != -1);
    assertTrue("Method not in results.",
        result.indexOf("\tpublic abstract String put(Integer key, String value)") != -1);
    assertTrue("Method not in results.",
        result.indexOf("\tpublic abstract Set<Integer> keySet()") != -1);

    result = Eclim.execute(new String[]{
      "java_impl", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE,
      "-t", "org.eclim.test.impl.TestImpl",
      "-s", "java.util.HashMap<Integer,String>", "-m", "put(Integer,String)"
    });

    System.out.println(result);

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue("Method not found or invalid.",
        Pattern.compile("public String put\\(Integer key, String value\\)")
        .matcher(contents).find());

    assertTrue("Method not commented out in results.",
        result.indexOf(
          "//public abstract String put(Integer key, String value)") != -1);
  }

  @Test
  public void executeSub()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "java_impl", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_SUB_FILE,
      "-o", "83", "-e", "utf-8"
    });

    System.out.println(result);

    assertTrue("Wrong first line.",
        result.startsWith("org.eclim.test.impl.TestSubImpl"));
    assertTrue("Interface not in results.",
        result.indexOf(
          "package java.util;\npublic interface Comparator<String> {") != -1);
    assertTrue("Interface not in results.",
        result.indexOf(
          "package java.util;\npublic interface Map<Integer,String> {") != -1);
    assertTrue("Class not in results.",
        result.indexOf(
          "package java.util;\npublic class HashMap<Integer,String> {") != -1);
    assertTrue("Method not in results.",
        result.indexOf("\tpublic abstract int compare(String o1, String o2)") != -1);
    assertTrue("Method not in results.",
        result.indexOf("\tpublic abstract String put(Integer key, String value)") != -1);
    assertTrue("Method not in results.",
        result.indexOf("\tpublic abstract Set<Integer> keySet()") != -1);

    result = Eclim.execute(new String[]{
      "java_impl", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_SUB_FILE,
      "-t", "org.eclim.test.impl.TestSubImpl",
      "-s", "java.util.HashMap<Integer,String>", "-m", "put(Integer,String)"
    });

    System.out.println(result);

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_SUB_FILE);
    assertTrue("Method not found or invalid.",
        Pattern.compile("public String put\\(Integer key, String value\\)")
        .matcher(contents).find());

    assertTrue("Method not commented out in results.",
        result.indexOf(
          "//public abstract String put(Integer key, String value)") != -1);

    result = Eclim.execute(new String[]{
      "java_impl", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_SUB_FILE,
      "-t", "org.eclim.test.impl.TestSubImpl",
      "-s", "java.util.Comparator<String>", "-m", "compare(String,String)"
    });

    System.out.println(result);

    contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_SUB_FILE);
    assertTrue("Method not found or invalid.",
        Pattern.compile("public int compare\\(String o1, String o2\\)")
        .matcher(contents).find());

    assertTrue("Method not commented out in results.",
        result.indexOf(
          "//public abstract int compare(String o1, String o2)") != -1);
  }
}
