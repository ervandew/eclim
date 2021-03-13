/**
 * Copyright (C) 2005 - 2021  Eric Van Dewoestine
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclim.Eclim;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for SettingsCommand.
 *
 * @author Eric Van Dewoestine
 */
public class SettingsCommandTest
{
  /**
   * Test the command.
   */
  @Test
  @SuppressWarnings("unchecked")
  public void execute()
    throws Exception
  {
    List<Map<String, String>> results = (List<Map<String, String>>)
      Eclim.execute(new String[]{"settings"});

    HashMap<String, Object> properties = new HashMap<String, Object>();
    for (Map<String, String> result : results){
      properties.put(result.get("name"), result.get("value"));
    }

    assertTrue("Missing org.eclim.user.email",
        properties.containsKey("org.eclim.user.email"));
    assertTrue("Missing org.eclim.user.name",
        properties.containsKey("org.eclim.user.name"));

    assertTrue("Missing org.eclim.project.copyright",
        properties.containsKey("org.eclim.project.copyright"));
    assertTrue("Missing org.eclim.project.version",
        properties.containsKey("org.eclim.project.version"));

    assertTrue("Missing org.eclim.java.logging.impl",
        properties.containsKey("org.eclim.java.logging.impl"));
    assertTrue("Missing org.eclipse.jdt.core.compiler.source",
        properties.containsKey("org.eclipse.jdt.core.compiler.source"));

    assertTrue("Missing org.eclim.java.doc.version",
        properties.containsKey("org.eclim.java.doc.version"));
  }
}
