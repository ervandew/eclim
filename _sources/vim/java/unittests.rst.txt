.. Copyright (C) 2005 - 2016  Eric Van Dewoestine

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

.. _\:JUnit:

JUnit
-----

Executing tests.
^^^^^^^^^^^^^^^^^^^^^

Eclim's **:JUnit** command allows you to execute individual test or individual
methods from your tests.

If you'd like to run a particular test you can do so by supplying the fully
qualified class name of the test to run (you can use vim's tab completion here
to alleviate having to type the full name):

.. code-block:: vim

  :JUnit org.test.MyTest

Another way is to simply run **:JUnit** with no arguments and let it decide
what to run based on the current context of the cursor:

* If you have a junit test file open and the cursor is not inside one of the
  test methods, then all of the current file's test methods will be executed.
* If the cursor is on or inside of a test method, then just that method will be
  run.
* If you have a regular class open and run **:JUnit**, eclim will attempt to
  locate the corresponding test and run it.
* If the cursor is on or inside of a method in a regular class, eclim will
  attempt to locate the test and then locate the corresponding test method for
  the current method in that test and run just that test method.

If you'd like to run all tests for the current file, regardless of whether the
cursor is on a method or not, you can do so by running **:JUnit** with the '%'
argument:

.. code-block:: vim

  :JUnit %

For cases where you'd like to run all your unit tests you can run **:JUnit**
with the '*' argument and eclim will locate all your test files and run them:

.. code-block:: vim

  :JUnit *

You can also pass in an ant compatible `pattern
<http://ant.apache.org/manual/dirtasks.html#patterns>`_ to match the tests
you'd like to run:

.. code-block:: vim

  :JUnit **/tests/*Test

.. _\:JUnitFindTest:

Find the test for the current source file.
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

When editing a java file, if you would like to open the corresponding test, you
can issue the command **:JUnitFindTest**. When the cursor is on a method in
your source file this command will also try to find the corresponding test
method within the test file.

If you run **:JUnitFindTest** from a test class, eclim will attempt to find the
corresponding class that is being tested.

.. _\:JUnitResult:

Opening test results run from you build tool.
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

If you are running your unit tests from a build tool like ant or maven, then
you most likely are writing those results to a directory in your project. If so
then you can set the :ref:`org.eclim.java.junit.output_dir
<org.eclim.java.junit.output_dir>` setting to that location which then allows
you to use the command **:JUnitResult** to locate and opening the result file
for the currently open test or the test supplied as an argument.

.. _\:JUnitImpl:

Generating test method stubs.
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

While editing junit files, eclim provides functionality to generate test method
stubs similar to the :ref:`method override / impl <:JavaImpl>`
functionality provided for non-test-case classes.  The only difference is that
instead of **:JavaImpl**, you use **:JUnitImpl** to open the window of possible
methods to implement.

To determine what class the current test is for, eclim expects that the
standard naming convention for tests is followed, where the test has the same
fully qualified class name as the target class with a 'Test' suffix.

So for the test ``org.foo.bar.BazTest``, the exepected class being tested would
be ``org.foo.bar.Baz``.

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

.. _org.eclim.java.junit.output_dir:

- **org.eclim.java.junit.output_dir** -
  Defines the project relative location of the junit test results.

  Ex.

  .. code-block:: cfg

    org.eclim.java.junit.output_dir=build/test/results

.. _org.eclim.java.junit.jvmargs:

- **org.eclim.java.junit.jvmargs** -
  Json formatted list of strings to supply as args to the jvm when forking to
  run unit tests.

  Ex.

  .. code-block:: cfg

    org.eclim.java.junit.jvmargs=["-Xmx512m"]

.. _org.eclim.java.junit.sysprops:

- **org.eclim.java.junit.sysprops** -
  Json formatted list of strings to supply as system properties to the jvm when
  forking to run unit tests.

  Ex.

  .. code-block:: cfg

    org.eclim.java.junit.sysprops=["file.encoding=UTF8", "foo.bar=baz"]

.. _org.eclim.java.junit.envvars:

- **org.eclim.java.junit.envvars** -
  Json formatted list of strings to supply as environment variables to the jvm
  when forking to run unit tests.

  Ex.

  .. code-block:: cfg

    org.eclim.java.junit.envvars=["FOO=bar"]

TestNG
------

Currently eclim's support for TestNG_ is limited to supporting Vim's :make in
conjunction with ant to populate vim's quickfix results with failed tests.

By default TestNG's output to the console is very terse.  So in order to support
monitoring of failed tests via vim's error format, eclim provides a custom
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
target from vim and (as long as eclim is running) all failed tests will be
added to your vim quickfix results.

Ex. Assuming your ant task is named 'test':

.. code-block:: vim

  :Ant test

.. _testng: http://testng.org/doc
.. _testng ant task docs: http://testng.org/doc/ant.html
