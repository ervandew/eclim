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
package org.eclim.plugin.jdt.command.bean;

import java.util.regex.Pattern;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for PropertiesCommand.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class PropertiesCommandTest
{
  private static final String TEST_FILE =
    "src/org/eclim/test/bean/TestBean.java";

  @Test
  public void executeGet ()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Eclim.execute(new String[]{
      "java_bean_properties", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE,
      "-o", "1", "-t", "getter", "-r", "name"
    });

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue("Getter not found.",
        Pattern.compile("public String getName \\(\\)").matcher(contents).find());
  }

  @Test
  public void executeSet ()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Eclim.execute(new String[]{
      "java_bean_properties", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE,
      "-o", "1", "-t", "setter", "-r", "name"
    });

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue("Setter not found.",
        Pattern.compile("public void setName \\(String name\\)")
        .matcher(contents).find());
  }

  @Test
  public void executeGetSet ()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Eclim.execute(new String[]{
      "java_bean_properties", "-p", Jdt.TEST_PROJECT,
      "-f", TEST_FILE,
      "-o", "1", "-t", "getter_setter", "-r", "valid"
    });

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue("Getter not found.",
        Pattern.compile("public boolean isValid \\(\\)").matcher(contents).find());
    assertTrue("Setter not found.",
        Pattern.compile("public void setValid \\(boolean valid\\)")
        .matcher(contents).find());
  }
}
