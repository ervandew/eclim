/**
 * Copyright (c) 2005 - 2008
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
  public void execute ()
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
