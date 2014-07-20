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
package org.eclim.plugin.pydev.command.project;

import java.io.File;

import org.eclim.Eclim;

import org.eclim.plugin.pydev.Pydev;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for InterpreterCommand.
 *
 * Note: this test expects that a 2.7 interpreter has already been added.
 *
 * @author Eric Van Dewoestine
 */
public class InterpreterCommandTest
{
  @Test
  public void interpreter()
  {
    assertTrue("Python project doesn't exist.",
        Eclim.projectExists(Pydev.TEST_PROJECT));

    String result = (String)
      Eclim.execute(new String[]{
        "python_interpreter", "-p", Pydev.TEST_PROJECT,
      });
    assertTrue(new File(result).exists());
  }
}
