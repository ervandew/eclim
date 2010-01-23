.. Copyright (C) 2005 - 2010  Eric Van Dewoestine

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

.. _vim/java/java:

Java / Javac Support
====================

.. _\:Java:

Executing java
-----------------

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
-------------

.. _org.eclim.java.run.mainclass:

Eclim Settings

- **org.eclim.java.run.mainclass** -
  Fully qualified name of the class containing the main method.

.. _\:Javac:

Executing javac
-----------------

To compile your project's source code, you may use the **:Javac** command,
which will run javac on all source files found in your project's src
directories as configured by your .classpath file.  You may use the
:ref:`org.eclim.java.compile.sourcepath` setting described below, if you wish
to alter which directories are used.  The resulting class files will be written
to the output path as defined in your project's .classpath file.

.. note::

  Please note that this command is not intended to be a full replacement for
  javac support provided by more comprehensive build tools like ant or maven.

Configuration
-------------

Eclim Settings

.. _org.eclim.java.compile.sourcepath:

- **org.eclim.java.compile.sourcepath** -
  The project relative source paths to be compiled by javac.  This should be a
  space separated list of project relative source directories which you want
  javac to be executed against.  When unset, all your configured source
  directories will be used.


.. _\:JavaListInstalls:

Viewing list of known JDKs/JREs installed
-----------------------------------------

To view a list of all the JDKs/JREs that eclipse is aware of, eclim provides
the command **:JavaListInstalls**.
