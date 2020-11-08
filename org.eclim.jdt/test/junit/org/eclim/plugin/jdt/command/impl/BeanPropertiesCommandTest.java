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

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for BeanPropertiesCommand.
 *
 * @author Eric Van Dewoestine
 */
public class BeanPropertiesCommandTest
{
  private static final String TEST_FILE =
    "src/org/eclim/test/impl/TestBean.java";

  @Test
  public void executeGet()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Eclim.execute(new String[]{
      "java_bean_properties", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE,
      "-o", "1", "-e", "utf-8", "-t", "getter", "-r", "name",
    });

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue("Getter not found.",
        Pattern.compile("public String getName\\(\\)").matcher(contents).find());
  }

  @Test
  public void executeSet()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Eclim.execute(new String[]{
      "java_bean_properties", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE,
      "-o", "1", "-e", "utf-8", "-t", "setter", "-r", "name",
    });

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue("Setter not found.",
        Pattern.compile("public void setName\\(String name\\)")
        .matcher(contents).find());
  }

  @Test
  public void executeGetSet()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Eclim.execute(new String[]{
      "java_bean_properties", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE,
      "-o", "1", "-e", "utf-8", "-t", "getter_setter", "-r", "valid",
    });

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue("Getter not found.",
        Pattern.compile("public boolean isValid\\(\\)").matcher(contents).find());
    assertTrue("Setter not found.",
        Pattern.compile("public void setValid\\(boolean valid\\)")
        .matcher(contents).find());
  }

  @Test
  public void executeGetSetIndex()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Eclim.execute(new String[]{
      "java_bean_properties", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE,
      "-o", "1", "-e", "utf-8", "-t", "getter_setter", "-r", "ids", "-i",
    });

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue("Getter not found.",
        Pattern.compile("public int\\[\\] getIds\\(\\)").matcher(contents).find());
    assertTrue("Getter index not found.",
        Pattern.compile("public int getIds\\(int index\\)")
          .matcher(contents)
          .find());
    assertTrue("Setter not found.",
        Pattern.compile("public void setIds\\(int\\[\\] ids\\)")
        .matcher(contents).find());
    assertTrue("Setter index not found.",
        Pattern.compile("public void setIds\\(int index, int ids\\)")
        .matcher(contents).find());
  }
}
