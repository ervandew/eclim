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
package org.eclim.plugin.pdt.command.search;

import org.eclim.Eclim;

import org.eclim.plugin.pdt.Pdt;

import org.junit.Test;

import static org.junit.Assert.*;

public class SearchCommandTest
{
  private static final String TEST_FILE = "php/search/find.php";

  @Test
  public void searchElementClass()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Pdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "php_search", "-n", Pdt.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "45", "-l", "5", "-e", "utf-8"
    });

    System.out.println(result);

    String file = Eclim.resolveFile(Pdt.TEST_PROJECT, "php/models.php");
    assertEquals("Wrong Result", file + "|6 col 7|class TestA", result);
  }

  @Test
  public void searchElementMethod()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Pdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "php_search", "-n", Pdt.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "62", "-l", "8", "-e", "utf-8"
    });

    System.out.println(result);

    String file = Eclim.resolveFile(Pdt.TEST_PROJECT, "php/models.php");
    assertEquals("Wrong Result",
        file + "|13 col 19|class TestA -> method methodA2", result);
  }

  @Test
  public void searchElementVariable()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Pdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "php_search", "-n", Pdt.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "82", "-l", "9", "-e", "utf-8"
    });

    System.out.println(result);

    String file = Eclim.resolveFile(Pdt.TEST_PROJECT, "php/models.php");
    assertEquals("Wrong Result",
        file + "|8 col 7|class TestA -> field $variable1", result);
  }

  @Test
  public void searchElementConstant()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Pdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "php_search", "-n", Pdt.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "111", "-l", "9", "-e", "utf-8"
    });

    System.out.println(result);

    String file = Eclim.resolveFile(Pdt.TEST_PROJECT, "php/models.php");
    assertEquals("Wrong Result", file + "|29 col 1|field CONSTANT1", result);
  }

  @Test
  public void searchElementFunction()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Pdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "php_search", "-n", Pdt.TEST_PROJECT, "-f", TEST_FILE,
      "-o", "94", "-l", "9", "-e", "utf-8"
    });

    System.out.println(result);

    String file = Eclim.resolveFile(Pdt.TEST_PROJECT, "php/models.php");
    assertEquals("Wrong Result", file + "|3 col 10|function functionA", result);
  }

  @Test
  public void searchClass()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Pdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "php_search", "-n", Pdt.TEST_PROJECT, "-p", "TestA", "-t", "class"
    });

    System.out.println(result);

    String file = Eclim.resolveFile(Pdt.TEST_PROJECT, "php/models.php");
    assertEquals("Wrong Result", file + "|6 col 7|class TestA", result);
  }

  @Test
  public void searchMethod()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Pdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "php_search", "-n", Pdt.TEST_PROJECT, "-p", "methodA1", "-t", "function"
    });

    System.out.println(result);

    String file = Eclim.resolveFile(Pdt.TEST_PROJECT, "php/models.php");
    assertEquals("Wrong Result",
        file + "|9 col 19|class TestA -> method methodA1", result);
  }

  @Test
  public void searchConstant()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Pdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "php_search", "-n", Pdt.TEST_PROJECT, "-p", "CONSTANT1", "-t", "field"
    });

    System.out.println(result);

    String file = Eclim.resolveFile(Pdt.TEST_PROJECT, "php/models.php");
    assertEquals("Wrong Result", file + "|29 col 1|field CONSTANT1", result);
  }

  @Test
  public void searchFunction()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Pdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "php_search", "-n", Pdt.TEST_PROJECT, "-p", "functionA", "-t", "function"
    });

    System.out.println(result);

    String file = Eclim.resolveFile(Pdt.TEST_PROJECT, "php/models.php");
    assertEquals("Wrong Result", file + "|3 col 10|function functionA", result);
  }
}
