/**
 * Copyright (C) 2014 - 2016  Eric Van Dewoestine
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

import java.util.List;
import java.util.Map;

import org.eclim.Eclim;

import org.eclim.plugin.groovy.Groovy;

import org.junit.Test;

import static org.junit.Assert.*;

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
  @SuppressWarnings("unchecked")
  public void javaDoc(){
    assertTrue("Groovy project doesn't exist.",
        Eclim.projectExists(Groovy.TEST_PROJECT));
    assertTrue("Groovy project doesn't exist.",
        Eclim.projectExists(Groovy.TEST_PROJECT));

    List<Map<String, String>> results = (List<Map<String, String>>)
      Eclim.execute(new String[]{
        "groovy_complete", "-p", Groovy.TEST_PROJECT,
        "-f", TEST_JAVA_DOC_FILE, "-l", "compact", "-o", "99", "-e", "utf-8",
        "-j"
      });

    // just check that at least some results have a javaDocURI in the response
    // (number of results can vary by the user's environment).
    int count = countJavaDocOccurence(results);
    assertTrue(count > 0);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void noJavaDoc(){
    assertTrue("Groovy project doesn't exist.",
        Eclim.projectExists(Groovy.TEST_PROJECT));

    List<Map<String, String>> results = (List<Map<String, String>>)
      Eclim.execute(new String[]{
        "groovy_complete", "-p", Groovy.TEST_PROJECT,
        "-f", TEST_JAVA_DOC_FILE, "-l", "compact", "-o", "99", "-e", "utf-8"
      });

    int count = countJavaDocOccurence(results);
    assertEquals(count, 0);
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
