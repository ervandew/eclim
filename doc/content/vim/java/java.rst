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

Java / Jps
==========

.. _\:Java:

Java
----

To run the configured main class for your project, you may use the **:Java**
command, which executes java and displays the results in a temporary buffer.

.. note::

  Please note that this command is not intended to be a full replacement for
  the more advance support provided by eclipse, ant, or maven.

The **:Java** will locate the main class to run using the following steps:

#. if the first argument is '%' (:Java %) then run the current class.
#. if the setting :ref:`org.eclim.java.run.mainclass
   <org.eclim.java.run.mainclass>` is set, then use the value as the fully
   qualified class name to run.
#. lastly, attempt to locate a class containing a static main method, if only
   one is found, use that class.

Configuration
^^^^^^^^^^^^^

.. _org.eclim.java.run.mainclass:

:doc:`Eclim Settings </vim/settings>`

- **org.eclim.java.run.mainclass** -
  Fully qualified name of the class containing the main method.
- **org.eclim.java.run.jvmargs** -
  Json formatted list of default jvm args.

.. _\:JavaClasspath:

Echo the classpath for the current project
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

When editing a java file, eclim provides the command **:JavaClasspath** which
will echo the project's resolved classpath entries separated by the system path
separator or a supplied
delimiter:

.. code-block:: vim

  :JavaClasspath
  :JavaClasspath -d \\n

If you would like to get the classpath from a script, you can also call eclim
directly:

.. code-block:: sh

  $ $ECLIPSE_HOME/eclim -command java_classpath -p <project_name>

.. _\:JavaListInstalls:

Viewing list of known JDKs/JREs installed
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

To view a list of all the JDKs/JREs that eclipse is aware of, eclim provides
the command **:JavaListInstalls**.

.. _\:Jps:

Jps (Process Status Tool)
-------------------------

As of Java 1.5 (Java 5.0), the sun jdk started shipping with some useful tools
for viewing information about running java processes.  To provide quick and easy
access to some of the information these commands provide, eclim exposes the
command **:Jps**.

.. note::

  For more information on the jdk tools you may view the `online
  documentation`_.

When invoked it will open a window containing information about the current
processes and some links for viewing additional info (depending upon
availability of required tools on your platform).

Example content:

.. image:: ../../images/screenshots/java/jps.png

- Line 1 consists of the process id followed by either the class name the
  process was started with or the path to the jar file.
- Lines 2 - 5 contains links that when you hit <enter> on, will open another
  window displaying the requested additional info.
- Lines 7 - 13 is a foldable block which contains a list of all the arguments
  passed to the main method of the process.
- Lines 15 - 21 is a foldable block which contains a list of all the arguments
  passed to the JVM.

.. _online documentation: http://docs.oracle.com/javase/6/docs/technotes/tools/#monitor
