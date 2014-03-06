/**
 * Copyright (C) 2014  Eric Van Dewoestine
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
package org.eclim.plugin.sdt.command.include;

import java.util.List;
import java.util.Map;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclim.Eclim;

import org.eclim.plugin.sdt.Sdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for ImportCommand.
 *
 * @author Eric Van Dewoestine
 */
public class ImportCommandTest
{
  private static final String TEST_FILE=
    "src/eclim/test/include/TestImport.scala";

  @Test
  @SuppressWarnings("unchecked")
  public void execute()
  {
    assertTrue("Scala project doesn't exist.",
        Eclim.projectExists(Sdt.TEST_PROJECT));

    List<String> choices = (List<String>)
      Eclim.execute(new String[]{
        "scala_import", "-p", Sdt.TEST_PROJECT, "-f", TEST_FILE,
        "-o", "60", "-e", "utf-8"
      });

    assertEquals("Wrong number of choices.", 3, choices.size());

    assertEquals(choices.get(0), "java.awt.List");
    assertEquals(choices.get(1), "java.util.List");
    assertEquals(choices.get(2), "scala.collection.immutable.List");

    Map<String,Object> position = (Map<String,Object>)Eclim.execute(new String[]{
      "scala_import", "-p", Sdt.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "60", "-e", "utf-8", "-t", "java.util.List",
    });
    String file = Eclim.fileToString(Sdt.TEST_PROJECT, TEST_FILE);
    Pattern listImport = Pattern.compile("import java\\.util\\.List");
    Matcher listMatcher = listImport.matcher(file);
    assertTrue(listMatcher.find());
    assertEquals(28, listMatcher.start());
    assertEquals(83, position.get("offset"));
    assertEquals(6, position.get("line"));
    assertEquals(14, position.get("column"));

    String message = (String)Eclim.execute(new String[]{
      "scala_import", "-p", Sdt.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "83", "-e", "utf-8", "-t", "java.util.List",
    });
    assertEquals(message, "Import already exists: java.util.List");

    position = (Map<String,Object>)Eclim.execute(new String[]{
      "scala_import", "-p", Sdt.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "134", "-e", "utf-8",
    });
    file = Eclim.fileToString(Sdt.TEST_PROJECT, TEST_FILE);
    Pattern collatorImport = Pattern.compile("import java\\.text\\.Collator");
    Matcher collatorMatcher = collatorImport.matcher(file);
    assertTrue(collatorMatcher.find());
    assertEquals(50, collatorMatcher.start());
    assertEquals(160, position.get("offset"));
    assertEquals(10, position.get("line"));
    assertEquals(24, position.get("column"));

    position = (Map<String,Object>)Eclim.execute(new String[]{
      "scala_import", "-p", Sdt.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "198", "-e", "utf-8",
    });
    file = Eclim.fileToString(Sdt.TEST_PROJECT, TEST_FILE);
    Pattern listBufferImport = Pattern.compile(
        "import scala\\.collection\\.mutable\\.ListBuffer");
    Matcher listBufferMatcher = listBufferImport.matcher(file);
    assertTrue(listBufferMatcher.find());
    assertEquals(76, listBufferMatcher.start());
    assertEquals(241, position.get("offset"));
    assertEquals(12, position.get("line"));
    assertEquals(19, position.get("column"));

    position = (Map<String,Object>)Eclim.execute(new String[]{
      "scala_import", "-p", Sdt.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "248", "-e", "utf-8",
    });
    file = Eclim.fileToString(Sdt.TEST_PROJECT, TEST_FILE);
    Pattern testImport = Pattern.compile(
        "import eclim\\.test\\.TestScala");
    Matcher testMatcher = testImport.matcher(file);
    assertTrue(testMatcher.find());
    assertEquals(119, testMatcher.start());
    assertEquals(276, position.get("offset"));
    assertEquals(13, position.get("line"));
    assertEquals(26, position.get("column"));
  }
}
