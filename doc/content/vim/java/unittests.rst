.. Copyright (C) 2005 - 2012  Eric Van Dewoestine

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

Unit Tests
==========

JUnit
-----

.. _\:JUnitExecute:

.. _\:JUnitResult:

Executing test cases and viewing the results.
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

When editing java source files eclim exposes a couple commands which allow you
to easily execute junit test cases and view the results.  First, please note
that eclim does not attempt to provide a junit execution environment.  Instead
the goal is to allow you to easily interface with your favorite build tool (ant,
maven, etc.).

The first of the commands is **:JUnitExecute**.  This command is responsible for
executing the current test case or the test case supplied as an argument.

The second command is **:JUnitResult**.  This command is responsible for
locating and opening the result file for the current test case or the test case
supplied as an argument.

.. note::

  Both commands support command completion of their respective arguments
  as long as the settings defined below are defined properly.


.. _\:JUnitImpl:

Generating test method stubs.
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

While editing junit files, eclim provides functionality to generate test method
stubs similar to the :ref:`method override / impl <:JavaImpl>`
functionality provided for non-test-case classes.  The only difference is that
instead of **:JavaImpl**, you use **:JUnitImpl** to open the window of possible
methods to implement.

To determine what class the current test case is for, eclim expects that the
standard naming convention for test cases is followed, where the test case has
the same fully qualified class name as the target class with a 'Test' suffix.

So for the test case ``org.foo.bar.BazTest``, the exepected class being tested
would be ``org.foo.bar.Baz``.

.. note::

   Eclim also supports tests with a 'Test' prefix instead of a suffix and in
   the case of neither a 'Test' prefix or suffix, it will search for a class of
   the same name in a different package should you perhaps use a package
   convention for your tests rather than a class name convention.

When invoking **:JUnitImpl** from within ``org.foo.bar.BazTest``, eclim will
locate the class ``org.foo.bar.Baz`` and generate a list of methods to test
from it.

When you hit <enter> on the method to add, if that method belongs to a type in
the hierarchy for the class being tested, then the corresponding test method
stub will be inserted, otherwise a regular overriding stub will be generated.

Configuration
^^^^^^^^^^^^^

:doc:`Eclim Settings </vim/settings>`

.. _org.eclim.java.junit.src_dir:

- **org.eclim.java.junit.src_dir** -
  Defines the location of the junit test case source files.  Currently this is
  only utilized for command completion of test case names for **:JUnitExecute**.
  Supports "<project>" key to represent the root directory of the current
  project.

  Ex.

  .. code-block:: cfg

    org.eclim.java.junit.src_dir=<project>/src/test/junit

.. _org.eclim.java.junit.output_dir:

- **org.eclim.java.junit.output_dir** -
  Defines the location of the junit test case results.  Supports "<project>" key
  to represent the root directory of the current project.

  Ex.

  .. code-block:: cfg

    org.eclim.java.junit.output_dir=<project>/build/test/results


.. _org.eclim.java.junit.command:

- **org.eclim.java.junit.command** -
  Defines the command used to execute a test case.

  Supports the following keys:

  - <testcase>: key representing the requested test case to
    execute using path separators.

    Ex. org/test/SomeTest

    Useful for use with ant and maven 2.x.

  - <testcase_class>: key representing the fully qualified
    class name of the requested test case to execute.

    Ex.  org.test.SomeTest

    Useful for use with maven 1.x.

  Ex.

  .. code-block:: cfg

    # Ant, assuming you have a target 'test' supporting
    # property 'junit.include'.
    org.eclim.java.junit.command=Ant -Djunit.include=<testcase> test

    # Maven 2.x using built in surefire plugin.
    org.eclim.java.junit.command=Mvn -Dtest=<testcase> test

    # Maven 1.x using built in test plugin.
    org.eclim.java.junit.command=Maven -Dtestcase=<testcase_class> test:single

.. _org.eclim.java.junit.version:

- **org.eclim.java.junit.version** (Default: 4) -
  Specifies the primary junit version being used, which determines which junit
  test method template will be used to generated the test method stubs.

TestNG
------

Currently eclim's support for TestNG_ is limited to supporting Vim's :make in
conjunction with ant to populate vim's quickfix results with failed test cases.

By default TestNG's output to the console is very terse.  So in order to support
monitoring of failed test cases via vim's error format, eclim provides a custom
TestNG listener which must be installed into your build environment.

#.  The first step is to place the ``eclim-testng.jar`` file in your TestNG
    classpath you have configured for ant.  You can find this jar file in your
    $ECLIPSE_HOME/plugins/org.eclim.jdt_version/ directory.
#.  The second step is to add the ``listener`` attribute to your
    testng task which references the required eclim testng listener\:

    ::

        ...
      <testng ... listener="org.eclim.testng.TestNgListener">
        ...

    See the `testng ant task docs`_ for more information.

Once you have completed that setup, you should then be able to run your ant
target from vim and (as long as eclim is running) all failed test cases will be
added to your vim quickfix results.

Ex. Assuming your ant task is named 'test':

.. code-block:: vim

  :Ant test

.. _testng: http://testng.org/doc
.. _testng ant task docs: http://testng.org/doc/ant.html
