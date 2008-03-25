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
package org.eclim.command.xml.validate;

import org.apache.commons.lang.StringUtils;

import org.eclim.Eclim;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test ValidateCommand.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class ValidateCommandTest
{
  private static final String TEST_FILE = "xml/test_dtd.xml";

  @Test
  public void execute ()
  {
    String file = TEST_FILE;
    String project = Eclim.TEST_PROJECT;
    String result = Eclim.execute(
        new String[]{"xml_validate", "-p", project, "-f", file });
    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');

    assertEquals("Wrong number of errors.", 2, results.length);

    assertTrue("Wrong filename.", results[0].indexOf(file) != -1);
    assertTrue("Wrong filename.", results[1].indexOf(file) != -1);

    assertTrue("Wrong error.",
        results[0].indexOf("The content of element type \"bar\" is incomplete") != -1);
    assertTrue("Wrong error.",
        results[1].indexOf("The content of element type \"foo\" must match") != -1);
  }
}
