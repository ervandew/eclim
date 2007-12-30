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
package org.eclim.plugin.jdt.command.src;

import java.io.File;
import java.io.FileInputStream;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.eclim.util.IOUtils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for ClassPrototypeCommand.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class ClassPrototypeCommandTest
{
  @Test
  public void execute ()
    throws Exception
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = Eclim.execute(new String[]{
      "java_class_prototype", "-p", Jdt.TEST_PROJECT,
      "-c", "org.eclim.test.src.TestPrototype"
    });

    System.out.println("File: " + result);
    File file = new File(result);

    assertTrue("Prototype file does not exist.", file.exists());

    FileInputStream fin = null;
    try{
      fin = new FileInputStream(file);
      String contents = IOUtils.toString(fin);
      System.out.println("Contents: \n" + contents);

      assertTrue("Package declaration doesn't match.",
          contents.indexOf("package org.eclim.test.src;") != -1);

      assertTrue("Class declaration doesn't match.",
          contents.indexOf("public class TestPrototype") != -1);

      assertTrue("test method declaration doesn't match.",
          contents.indexOf("public final void test ();") != -1);

      assertTrue("testAnother method declaration doesn't match.",
          contents.indexOf("public String testAnother (String blah)\n\t\tthrows Exception;") != -1);
    }finally{
      IOUtils.closeQuietly(fin);
    }
  }
}
