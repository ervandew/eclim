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
package org.eclim.plugin.maven.command.dependency;

import org.eclim.Eclim;

import org.eclim.plugin.maven.Maven;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for SearchCommand.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class SearchCommandTest
{
  private static final String TEST_FILE = "pom.xml";

  @Test
  public void execute ()
  {
    String result = Eclim.execute(new String[]{
      "maven_dependency_search", "-p", Maven.TEST_PROJECT,
      "-f", TEST_FILE,
      "-t", "mvn", "-s", "junit"
    });

    System.out.println(result);

    assertTrue("Ant section not found.", result.startsWith("ant\n"));
    assertTrue("JUnit section not found.", result.indexOf("\njunit\n") != -1);
    assertTrue("JUnit dependency not found.",
        result.indexOf("\n\tjunit.jar (3.8)\n") != -1);
    assertTrue("JUnit dependency not commented out.",
        result.indexOf("\n\t//junit.jar (3.8.1)\n") != -1);
  }

  /**
   * Required for running tests in ant 1.6.5.
   */
  public static junit.framework.Test suite()
  {
    return new junit.framework.JUnit4TestAdapter(SearchCommandTest.class);
  }
}
