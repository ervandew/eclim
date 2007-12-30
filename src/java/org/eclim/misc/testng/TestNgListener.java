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
package org.eclim.misc.testng;

import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

/**
 * The purpose of this listener is to print out failed test case class and
 * method names so that an editor output processor (like vim's errorformat for
 * its :make support) can obtain enough failure information to help populate an
 * error list and possibly locate the corresponding source files.
 *
 * @author Eric Van Dewoestine (ervandew@gmail.com)
 * @version $Revision$
 */
public class TestNgListener
  extends TestListenerAdapter
{
  /**
   * {@inheritDoc}
   * @see org.testng.ITestListener#onTestFailure(ITestResult)
   */
  public void onTestFailure (ITestResult _result)
  {
    System.out.println("eclim testng: " +
        _result.getTestClass().getName() + ':' +
        _result.getMethod().getMethodName());
  }
}
