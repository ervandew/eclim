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
package org.eclim.plugin.jdt.command.include;

import java.util.ArrayList;
import java.util.List;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for ImportCommand.
 *
 * @author Eric Van Dewoestine
 */
public class ImportCommandTest
{
  @Test
  @SuppressWarnings("unchecked")
  public void execute()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    List<String> results = (List<String>)Eclim.execute(new String[]{
      "java_import", "-n", Jdt.TEST_PROJECT, "-p", "List"
    });

    // remove any com.sun entries
    List<String> imports = new ArrayList<String>();
    for (String imprt : results){
      if (imprt.matches("com\\.sun.*?")){
        continue;
      }
      imports.add(imprt);
    }

    assertEquals(2, imports.size());
    assertEquals("java.awt.List", imports.get(0));
    assertEquals("java.util.List", imports.get(1));
  }
}
