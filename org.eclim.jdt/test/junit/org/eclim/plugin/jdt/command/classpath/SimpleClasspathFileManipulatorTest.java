/**
 * Copyright (C) 2005 - 2016  Eric Van Dewoestine
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
package org.eclim.plugin.jdt.command.classpath;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for the {@code SimpleClasspathFileManipulator} class.
 *
 * @author Lukas Roth
 *
 */
public class SimpleClasspathFileManipulatorTest
{
  private static final String TEST_FILES_BASE_FOLDER = "org.eclim.jdt/test/junit/org/eclim/plugin/jdt/command/classpath/testfiles/";
  private static List<String> cleanupList = new ArrayList<String>();
  private String dependencyLocation1 = "/example/folder/bla.jar";
  private String dependencyLocation2 = "/example/folder/bla2.jar";
  private String dependencyLocation3 = "/example/folder/bla3.jar";
  private String dependencyLocation4 = "/example/folder/bla4.jar";
  ClasspathFileManipulator classpathFileManipulator = new SimpleClasspathFileManipulator();

  @Test
  public void normalAddJarDependencyToClasspath()
      throws ClasspathFileManipulatorException, IOException
  {
    String filepath = getTempCopy(getBaseFolder() + "/add1.txt");
    classpathFileManipulator.addJarDependency(dependencyLocation1, filepath);
    String res = readFile(filepath);
    String expectedRes = readFile(getBaseFolder() + "/add1_expected.txt");
    Assert.assertEquals(expectedRes, res);
  }

  @Test
  public void normalRemoveJarDependencyToClasspath()
      throws ClasspathFileManipulatorException, IOException
  {
    String filepath = getTempCopy(getBaseFolder() + "/remove1.txt");
    classpathFileManipulator.removeJarDependency(dependencyLocation1, filepath);
    String res = readFile(filepath);
    String expectedRes = readFile(getBaseFolder() + "/remove1_expected.txt");
    Assert.assertEquals(expectedRes, res);
  }

  @Test
  public void normalAddRemoveJarDependencyToClasspath()
      throws ClasspathFileManipulatorException, IOException
  {
    String filepath = getTempCopy(getBaseFolder() + "/addRemove1.txt");
    classpathFileManipulator.addJarDependency(dependencyLocation1, filepath);
    classpathFileManipulator.removeJarDependency(dependencyLocation1, filepath);
    String res = readFile(filepath);
    String expectedRes = readFile(getBaseFolder() + "/addRemove1_expected.txt");
    Assert.assertEquals(expectedRes, res);
  }

  @Test
  public void addRemoveMultipleTimes()
      throws ClasspathFileManipulatorException, IOException
  {
    String filepath = getTempCopy(getBaseFolder() + "/addRemoveMultipleTimes1.txt");
    classpathFileManipulator.addJarDependency(dependencyLocation1, filepath);
    classpathFileManipulator.addJarDependency(dependencyLocation2, filepath);
    classpathFileManipulator.addJarDependency(dependencyLocation3, filepath);
    classpathFileManipulator.addJarDependency(dependencyLocation4, filepath);
    classpathFileManipulator.removeJarDependency(dependencyLocation1, filepath);
    classpathFileManipulator.removeJarDependency(dependencyLocation3, filepath);
    String res = readFile(filepath);
    String expectedRes = readFile(
        getBaseFolder() + "/addRemoveMultipleTimes1_expected.txt");
    Assert.assertEquals(expectedRes, res);
  }

  // we expect that it then does not re-add the classpath to the file
  @Test
  public void addTwoTimesSameClasspath()
      throws ClasspathFileManipulatorException, IOException
  {
    String filepath = getTempCopy(getBaseFolder() + "/addTwoTimesSame1.txt");
    classpathFileManipulator.addJarDependency(dependencyLocation1, filepath);
    classpathFileManipulator.addJarDependency(dependencyLocation1, filepath);
    String res = readFile(filepath);
    String expectedRes = readFile(
        getBaseFolder() + "/addTwoTimesSame1_expected.txt");
    Assert.assertEquals(expectedRes, res);
  }

  // we expect that it does not crash removing a non existent classpath
  @Test
  public void removeNotExisting()
      throws ClasspathFileManipulatorException, IOException
  {
    String filepath = getTempCopy(getBaseFolder() + "/removeNotExisting1.txt");
    classpathFileManipulator.removeJarDependency(dependencyLocation1, filepath);
    String res = readFile(filepath);
    String expectedRes = readFile(
        getBaseFolder() + "/removeNotExisting1_expected.txt");
    Assert.assertEquals(expectedRes, res);
  }

  static String readFile(String path)
      throws IOException
  {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, Charset.defaultCharset());
  }

  private static String getBaseFolder()
  {
    return TEST_FILES_BASE_FOLDER;
  }

  private static String getTempCopy(String string)
      throws IOException
  {
    String pathOfCopy = string + "_copy";
    Files.copy((new File(string)).toPath(), (new File(pathOfCopy)).toPath(),
        REPLACE_EXISTING);
    cleanupList.add(pathOfCopy);
    return pathOfCopy;
  }

  @After
  public void cleanUpCopies()
  {
    for (String file : cleanupList) {
      delete(file);
    }
  }

  private static void delete(String filepath)
  {
    File f = new File(filepath);
    f.delete();
  }
}
