.. Copyright (C) 2005 - 2008  Eric Van Dewoestine

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.

.. _vim/java/testng:

TestNG
======

Currently eclim's support for <a href="ext:testng">TestNG</a> is limited to
supporting Vim's :make in conjunction with ant to populate vim's quickfix
results with failed test cases.

By default TestNG's output to the console is very terse.  So in order to support
monitoring of failed test cases via vim's error format, eclim provides a custom
TestNG listener which must be installed into your build environment.

#.  The first step is to place the ``eclim-misc.jar`` file in your TestNG
    classpath you have configured for ant.  You can find this jar file in your
    $ECLIPSE_HOME/plugins/org.eclim_version/ directory.
#.  The second step is to add the ``listener`` attribute to your
    testng task which references the required eclim testng listener\:

    ::

        ...
      <testng ... listener="org.eclim.misc.testng.TestNgListener">
        ...

    See the `testng ant task docs`_ for more information.

Once you have completed that setup, you should then be able to run your ant
target from vim and (as long as eclim is running) all failed test cases will be
added to your vim quickfix results.

Ex. Assuming your ant task is named 'test':

.. code-block:: vim

  :Ant test

.. _testng ant task docs: http://testng.org/doc/ant.html
