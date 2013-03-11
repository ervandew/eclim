/**
 * Copyright (C) 2011  Eric Van Dewoestine
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
package org.eclim.plugin.sdt.command.src;

import java.io.File;

import java.util.List;
import java.util.Map;

import org.eclim.Eclim;

import org.eclim.plugin.sdt.Sdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for SrcUpdateCommand.
 *
 * @author Eric Van Dewoestine
 */
public class SrcUpdateCommandTest
{
  private static final String TEST_FILE=
    "src/eclim/test/src/TestSrc.scala";

  @Test
  @SuppressWarnings("unchecked")
  public void update()
  {
    assertTrue("Scala project doesn't exist.",
        Eclim.projectExists(Sdt.TEST_PROJECT));

    String file = Eclim.resolveFile(Sdt.TEST_PROJECT, TEST_FILE);
    new File(file).setLastModified(System.currentTimeMillis());

    List<Map<String,Object>> results = (List<Map<String,Object>>)
      Eclim.execute(new String[]{
        "scala_src_update", "-p", Sdt.TEST_PROJECT, "-f", TEST_FILE, "-v"
      });

    assertEquals("Wrong number of errors.", 2, results.size());

    Map<String,Object> error = results.get(0);
    assertEquals(error.get("filename"), file);
    assertEquals(error.get("warning"), false);
    assertTrue(((String)error.get("message"))
        .indexOf("value foo is not a member of eclim.test.TestScala") != -1);

    error = results.get(1);
    assertEquals(error.get("filename"), file);
    assertEquals(error.get("warning"), false);
    assertTrue(((String)error.get("message"))
        .indexOf("not found: type ArrayList") != -1);
  }
}
