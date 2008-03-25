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
package org.eclim.command.xml.format;

import org.apache.commons.lang.StringUtils;

import org.eclim.Eclim;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test FormatCommand.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class FormatCommandTest
{
  private static final String TEST_FILE = "xml/format.xml";

  @Test
  public void execute ()
  {
    String result = Eclim.execute(new String[]{
      "xml_format",
      "-f", Eclim.resolveFile(TEST_FILE), "-w", "80", "-i", "2"
    });
    System.out.println(result);

    String[] lines = StringUtils.split(result, '\n');

    assertEquals("Wrong number of lines.", 5, lines.length);

    assertEquals("<blah attr1=\"one\" attr2=\"two\" attr3=\"three\" attr4=\"four\" attr5=\"five\" attr6=\"six\" attr7=\"seven\">", lines[1]);
    assertEquals("  <one>one</one>", lines[2]);
    assertEquals("  <two/>", lines[3]);
    assertEquals("</blah>", lines[4]);
  }
}
