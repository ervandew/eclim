/**
 * Copyright (C) 2005 - 2008  Eric Van Dewoestine
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
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class ImplCommandTest
{
  private static final String TEST_FILE =
    "src/org/eclim/test/impl/TestImpl.java";

  @Test
  public void execute ()
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
        result.indexOf("package java.util;\npublic interface List {") != -1);
    assertTrue("Method not in results.",
        result.indexOf("\tpublic abstract boolean remove (Object o)") != -1);

    result = Eclim.execute(new String[]{
      "java_impl", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE,
      "-t", "org.eclim.test.impl.TestImpl",
      "-s", "java.util.List", "-m", "remove(Object)"
    });

    System.out.println(result);

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue("Method not found or invalid.",
        Pattern.compile("public boolean remove \\(Object o\\)")
        .matcher(contents).find());

    assertTrue("Method not commented out in results.",
        result.indexOf("//public abstract boolean remove (Object o)") != -1);
  }
}
