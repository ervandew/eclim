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
package org.eclim.plugin.dltkruby.command.src;

import java.util.HashMap;
import java.util.List;

import org.eclim.Eclim;

import org.eclim.plugin.dltkruby.DltkRuby;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for SrcUpdateCommand.
 *
 * @author Eric Van Dewoestine
 */
public class SrcUpdateCommandTest
{
  private static final String TEST_FILE = "src/src/testValidate.rb";

  @Test
  @SuppressWarnings("unchecked")
  public void validate()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(DltkRuby.TEST_PROJECT));

    List<HashMap<String,Object>> results = (List<HashMap<String,Object>>)
      Eclim.execute(new String[]{
        "ruby_src_update", "-p", DltkRuby.TEST_PROJECT, "-f", TEST_FILE, "-v"
      });

    String file = Eclim.resolveFile(DltkRuby.TEST_PROJECT, TEST_FILE);
    HashMap<String,Object> error = results.get(0);
    assertEquals(error.get("filename"), file);
    assertEquals(error.get("message"), "syntax error, unexpected tRPAREN");
    assertEquals(error.get("line"), 2);
    assertEquals(error.get("column"), 11);
    assertEquals(error.get("warning"), 0);
  }
}
