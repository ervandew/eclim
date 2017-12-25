/**
 * Copyright (C) 2005 - 2017  Eric Van Dewoestine
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
package org.eclim.testng;

import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

/**
 * The purpose of this listener is to print out failed test case class and
 * method names so that an editor output processor (like vim's errorformat for
 * its :make support) can obtain enough failure information to help populate an
 * error list and possibly locate the corresponding source files.
 *
 * @author Eric Van Dewoestine
 */
public class TestNgListener
  extends TestListenerAdapter
{
  @Override
  public void onTestFailure(ITestResult result)
  {
    System.out.println("eclim testng: " +
        result.getTestClass().getName() + ':' +
        result.getMethod().getMethodName());
  }
}
