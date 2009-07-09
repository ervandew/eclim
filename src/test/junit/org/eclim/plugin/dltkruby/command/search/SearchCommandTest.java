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
package org.eclim.plugin.dltkruby.command.search;

import org.eclim.Eclim;

import org.eclim.plugin.dltkruby.DltkRuby;

import org.junit.Test;

import static org.junit.Assert.*;

public class SearchCommandTest
{
  private static final String TEST_FILE = "src/search/testSearch.rb";

  @Test
  public void searchClass()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(DltkRuby.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "ruby_search", "-n", DltkRuby.TEST_PROJECT, "-p", "TestClass", "-t", "class"
    });

    System.out.println(result);

    String file = Eclim.resolveFile(DltkRuby.TEST_PROJECT, "src/test.rb");
    assertEquals("Wrong Result", file + "|11 col 7|type TestClass", result);

    result = Eclim.execute(new String[]{
      "ruby_search", "-n", DltkRuby.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "23", "-l", "9", "-e", "utf-8", "-x", "declarations"
    });

    System.out.println(result);

    file = Eclim.resolveFile(DltkRuby.TEST_PROJECT, "src/test.rb");
    assertEquals("Wrong Result", file + "|11 col 7|type TestClass", result);
  }

  @Test
  public void searchModule()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(DltkRuby.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "ruby_search", "-n", DltkRuby.TEST_PROJECT, "-p", "TestModule",
    });

    System.out.println(result);

    String file = Eclim.resolveFile(DltkRuby.TEST_PROJECT, "src/test.rb");
    assertEquals("Wrong Result", file + "|1 col 8|type TestModule", result);

    result = Eclim.execute(new String[]{
      "ruby_search", "-n", DltkRuby.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "69", "-l", "10", "-e", "utf-8", "-x", "declarations"
    });

    System.out.println(result);

    file = Eclim.resolveFile(DltkRuby.TEST_PROJECT, "src/test.rb");
    assertEquals("Wrong Result", file + "|1 col 8|type TestModule", result);
  }

  @Test
  public void searchMethod()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(DltkRuby.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "ruby_search", "-n", DltkRuby.TEST_PROJECT, "-p", "testA", "-t", "method"
    });

    System.out.println(result);

    String file = Eclim.resolveFile(DltkRuby.TEST_PROJECT, "src/test.rb");
    assertEquals("Wrong Result",
        file + "|13 col 7|type TestClass : method testA", result);

    result = Eclim.execute(new String[]{
      "ruby_search", "-n", DltkRuby.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "42", "-l", "5", "-e", "utf-8", "-x", "declarations"
    });

    System.out.println(result);

    file = Eclim.resolveFile(DltkRuby.TEST_PROJECT, "src/test.rb");
    assertEquals("Wrong Result",
        file + "|13 col 7|type TestClass : method testA", result);
  }

  @Test
  public void searchFunction()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(DltkRuby.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "ruby_search", "-n", DltkRuby.TEST_PROJECT, "-p", "testFunction", "-t", "function"
    });

    System.out.println(result);

    String file = Eclim.resolveFile(DltkRuby.TEST_PROJECT, "src/test.rb");
    assertEquals("Wrong Result",
        file + "|21 col 5|function testFunction", result);

    result = Eclim.execute(new String[]{
      "ruby_search", "-n", DltkRuby.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "104", "-l", "12", "-e", "utf-8", "-x", "declarations"
    });

    System.out.println(result);

    file = Eclim.resolveFile(DltkRuby.TEST_PROJECT, "src/test.rb");
    assertEquals("Wrong Result",
        file + "|21 col 5|function testFunction", result);
  }

  @Test
  public void searchField()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(DltkRuby.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "ruby_search", "-n", DltkRuby.TEST_PROJECT, "-p", "CONSTANT", "-t", "field"
    });

    System.out.println(result);

    String file = Eclim.resolveFile(DltkRuby.TEST_PROJECT, "src/test.rb");
    assertEquals("Wrong Result",
        file + "|12 col 3|type TestClass : field CONSTANT", result);

    result = Eclim.execute(new String[]{
      "ruby_search", "-n", DltkRuby.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "59", "-l", "8", "-e", "utf-8", "-x", "declarations"
    });

    System.out.println(result);

    file = Eclim.resolveFile(DltkRuby.TEST_PROJECT, "src/test.rb");
    assertEquals("Wrong Result",
        file + "|12 col 3|type TestClass : field CONSTANT", result);
  }
}
