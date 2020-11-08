/**
 * Copyright (C) 2005 - 2020  Eric Van Dewoestine
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
package org.eclim.plugin.core.command.admin;

import java.util.Map;

import org.eclim.Eclim;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for PingCommand.
 *
 * @author Eric Van Dewoestine
 */
public class PingCommandTest
{
  /**
   * Test the command.
   */
  @Test
  @SuppressWarnings("unchecked")
  public void execute()
  {
    Map<String, String> result = (Map<String, String>)
        Eclim.execute(new String[]{"ping"});
    assertEquals("Unexpected result",
        System.getProperty("eclim.version"), result.get("eclim"));
    assertTrue("Unexpected result", result.get("eclipse").startsWith("4."));
  }
}
