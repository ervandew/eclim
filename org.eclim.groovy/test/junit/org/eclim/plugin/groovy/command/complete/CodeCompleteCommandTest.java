/**
 * Copyright (C) 2014  Eric Van Dewoestine
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
package org.eclim.plugin.groovy.command.complete;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.eclim.Eclim;

import org.eclim.plugin.groovy.Groovy;

import org.junit.Test;

/**
 * Test case for CodeCompleteCommand.
 *
 * @author Eric Van Dewoestine
 */
public class CodeCompleteCommandTest
{
  private static final String TEST_FILE =
      "src/org/eclim/test/complete/TestCompletion.groovy";
  private static final String TEST_JAVA_DOC_FILE =
      "src/org/eclim/test/complete/TestCompletionJavaDoc.groovy";

  @Test
  @SuppressWarnings("unchecked")
  public void completionByPrefix()
  {
    assertTrue("Groovy project doesn't exist.",
        Eclim.projectExists(Groovy.TEST_PROJECT));

    List<Map<String,String>> results = (List<Map<String,String>>)
      Eclim.execute(new String[]{
        "groovy_complete", "-p", Groovy.TEST_PROJECT,
        "-f", TEST_FILE, "-l", "compact", "-o", "106", "-e", "utf-8",
      });

    assertTrue(results.size() >= 10);

    Map<String,String> result = results.get(0);
    assertEquals(result.get("completion"), "add(");

    result = results.get(1);
    assertEquals(result.get("completion"), "addAll(");
  }

  @Test
  public void javaDoc(){
    assertTrue("Groovy project doesn't exist.",
        Eclim.projectExists(Groovy.TEST_PROJECT));
    int count = getJavaDocCount("anything");
    assertJavaDocPresent(count);
  }

  @Test
  public void JavaDocEmpty(){
    assertTrue("Groovy project doesn't exist.",
        Eclim.projectExists(Groovy.TEST_PROJECT));
    int count = getJavaDocCount("");
    assertJavaDocPresent(count);
  }

  private void assertJavaDocPresent(int count)
  {
    // We do not compare with the actual number to make
    // the test more robust ==> if something in eclipse
    // changes and some elements do not exist anymore /
    // do not have a javaDoc element the test still
    // succeeds.
    int minimalCount = 50;
    assertTrue("We should receive some JavaDoc links", count > minimalCount);
  }

  @Test
  public void noJavaDoc(){
    assertTrue("Groovy project doesn't exist.",
        Eclim.projectExists(Groovy.TEST_PROJECT));
    int count = getJavaDocCountNoJavaDocFlag();
    assertEquals("We should receive no JavaDoc links, since the java doc flag is not set", 0, count);
  }

  @SuppressWarnings("unchecked")
  private int getJavaDocCount(String javaDocArg){
    assertTrue("Groovy project doesn't exist.",
        Eclim.projectExists(Groovy.TEST_PROJECT));

    List<Map<String, String>> results = (List<Map<String, String>>) Eclim
        .execute(new String[] { "groovy_complete", "-p", Groovy.TEST_PROJECT, "-f",
            TEST_JAVA_DOC_FILE, "-l", "compact", "-o", "99", "-e", "utf-8", "-j", javaDocArg });

    return countJavaDocOccurence(results);
  }

  @SuppressWarnings("unchecked")
  private int getJavaDocCountNoJavaDocFlag(){
    assertTrue("Groovy project doesn't exist.",
        Eclim.projectExists(Groovy.TEST_PROJECT));

    List<Map<String, String>> results = (List<Map<String, String>>) Eclim
        .execute(new String[] { "groovy_complete", "-p", Groovy.TEST_PROJECT, "-f",
            TEST_JAVA_DOC_FILE, "-l", "compact", "-o", "99", "-e", "utf-8",});

    return countJavaDocOccurence(results);
  }

  private int countJavaDocOccurence(List<Map<String, String>> results)
  {
    int count = 0;
    for(Map<String, String> result : results){
      String javaDoc = result.get("javaDocURI");
      if(javaDoc != null && !javaDoc.equals("")){
        count++;
      }
    }
    return count;
  }
}
