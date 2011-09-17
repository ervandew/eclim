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
package org.eclim.plugin.maven.command.dependency;

import org.eclim.Eclim;

import org.eclim.plugin.maven.Maven;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for SearchCommand.
 *
 * @author Eric Van Dewoestine
 */
public class SearchCommandTest
{
  private static final String TEST_FILE = "pom.xml";

  @Test
  public void execute()
  {
    String result = (String)Eclim.execute(new String[]{
      "maven_dependency_search", "-p", Maven.TEST_PROJECT,
      "-f", TEST_FILE,
      "-t", "mvn", "-s", "junit"
    });

    assertTrue("Ant section not found.", result.startsWith("ant\n"));
    assertTrue("JUnit section not found.", result.indexOf("\njunit\n") != -1);
    assertTrue("JUnit dependency not found.",
        result.indexOf("\n\tjunit (3.8)\n") != -1);
    assertTrue("JUnit dependency not commented out.",
        result.indexOf("\n\t//junit (3.8.1)\n") != -1);
  }
}
