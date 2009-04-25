.. Copyright (C) 2005 - 2009  Eric Van Dewoestine

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

Configuration
-------------

Eclim Settings

- **org.eclim.java.run.mainclass** -
  Fully qualified name of the class containing the main method.

.. _\:Javac:

Executing javac
-----------------

To run the javac command on your source code, you may use the **:Javac**
command, which executes javac against all your source code.

.. note::

  Please note that this command is not intended to be a full replacement for
  javac support provided by more comprehensive build tools like ant or maven.

Configuration
-------------

Eclim Settings

- **org.eclim.java.compile.sourcepath** -
  The project relative source paths to be compiled by javac.  This should be a
  space separated list of project relative source directories which you want
  javac to be executed against.  When unset, all your configured source
  directories will be used.
