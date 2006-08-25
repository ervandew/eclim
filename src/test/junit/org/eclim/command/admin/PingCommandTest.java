/**
 * Copyright (c) 2005 - 2006
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

import junit.framework.JUnit4TestAdapter;

import org.eclim.Eclim;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for PingCommand.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class PingCommandTest
{
  @Test
  public void execute ()
  {
    String result = Eclim.execute(new String[]{"-command", "ping"});
    assertEquals("Unexpected result",
        "eclim " + System.getProperty("eclim.version"), result);
  }

  /**
   * Required for running tests in ant 1.6.5.
   */
  public static junit.framework.Test suite()
  {
    return new JUnit4TestAdapter(PingCommandTest.class);
  }
}
