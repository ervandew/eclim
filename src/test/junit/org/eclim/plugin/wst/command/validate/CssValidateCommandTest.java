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
package org.eclim.plugin.wst.command.validate;

import org.apache.commons.lang.StringUtils;

import org.eclim.Eclim;

import org.eclim.plugin.wst.Wst;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for CssValidateCommand.
 *
 * @author Eric Van Dewoestine
 */
public class CssValidateCommandTest
{
  private static final String TEST_FILE = "css/validate.css";
  private static final String TEST_LEXICAL_FILE = "css/lexical.css";

  @Test
  public void validate()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Wst.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "css_validate", "-p", Wst.TEST_PROJECT, "-f", TEST_FILE
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');
    assertEquals("Wrong number of errors.", 2, results.length);

    String file = Eclim.resolveFile(Wst.TEST_PROJECT, TEST_FILE);
    for(int ii = 0; ii < results.length; ii++){
      assertTrue("Wrong filename [" + ii + "].", results[ii].startsWith(file));
      assertTrue("Wrong level [" + ii + "].", results[ii].endsWith("|e"));
    }

    assertTrue("Wrong error.",
        results[0].indexOf("bald is not a font-weight value") != -1);
    assertTrue("Wrong error.",
        results[1].indexOf("Property fnt-size doesn't exist") != -1);
  }

  @Test
  public void lexical()
  {
    assertTrue("Project doesn't exist.",
        Eclim.projectExists(Wst.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "css_validate", "-p", Wst.TEST_PROJECT, "-f", TEST_LEXICAL_FILE
    });

    System.out.println(result);

    String[] results = StringUtils.split(result, '\n');
    assertEquals("Wrong number of errors.", 1, results.length);

    String file = Eclim.resolveFile(Wst.TEST_PROJECT, TEST_LEXICAL_FILE);
    for(int ii = 0; ii < results.length; ii++){
      assertTrue("Wrong filename [" + ii + "].", results[ii].startsWith(file));
      assertTrue("Wrong level [" + ii + "].", results[ii].endsWith("|e"));
    }

    assertTrue("Wrong error.", results[0].indexOf("Lexical error") != -1);
  }
}
