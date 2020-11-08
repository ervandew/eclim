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
 * @author Eric Van Dewoestine
 */
public class ClassPrototypeCommandTest
{
  @Test
  public void execute()
    throws Exception
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    String result = (String)Eclim.execute(new String[]{
      "java_class_prototype",
      "-p", Jdt.TEST_PROJECT,
      "-c", "java.util.List",
    });

    File file = new File(result);

    assertTrue("Prototype file does not exist.", file.exists());

    FileInputStream fin = null;
    try{
      fin = new FileInputStream(file);
      String contents = IOUtils.toString(fin);
      System.out.println("Contents: \n" + contents);

      assertTrue("Class declaration doesn't match.",
          contents.indexOf("public abstract interface java.util.List") != -1);

      int index = contents.indexOf(
          "public abstract boolean add(java.lang.Object arg0);");
      assertTrue("add method declaration doesn't match.", index != -1);
    }finally{
      IOUtils.closeQuietly(fin);
    }
  }
}
