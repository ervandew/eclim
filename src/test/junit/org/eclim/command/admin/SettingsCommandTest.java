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
package org.eclim.command.admin;

import java.io.ByteArrayInputStream;

import java.util.Properties;

import org.eclim.Eclim;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for SettingsCommand.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class SettingsCommandTest
{
  /**
   * Test the command.
   */
  @Test
  public void execute()
    throws Exception
  {
    String result = Eclim.execute(new String[]{"settings"});

    Properties properties = new Properties();
    properties.load(new ByteArrayInputStream(result.getBytes()));

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
    assertTrue("Missing org.eclim.java.validation.ignore.warnings",
        properties.containsKey("org.eclim.java.validation.ignore.warnings"));
    assertTrue("Missing org.eclipse.jdt.core.compiler.source",
        properties.containsKey("org.eclipse.jdt.core.compiler.source"));

    assertTrue("Missing org.eclim.java.doc.version",
        properties.containsKey("org.eclim.java.doc.version"));
  }
}
