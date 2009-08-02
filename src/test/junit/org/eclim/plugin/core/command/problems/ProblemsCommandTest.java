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
package org.eclim.plugin.core.command.problems;

import java.util.ArrayList;

import org.eclim.Eclim;

import org.eclim.util.CollectionUtils;
import org.eclim.util.StringUtils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests the problems command.
 *
 * @author Eric Van Dewoestine
 */
public class ProblemsCommandTest
{
  private static final String TEST_PROJECT = "eclim_unit_test_java";

  @Test
  public void execute()
  {
    String result = Eclim.execute(new String[]{
      "problems", "-p", TEST_PROJECT
    });

    System.out.println(result);

    ArrayList<String> results = new ArrayList<String>();
    CollectionUtils.addAll(results, StringUtils.split(result, '\n'));
    assertTrue(results.contains(Eclim.getWorkspace() + "/" + TEST_PROJECT + "/src/org/eclim/test/checkstyle/TestCheckstyle.java|27 col 5|ArrayList cannot be resolved to a type|e"));
  }
}
