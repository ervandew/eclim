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

.. _vim/java/javadoc:

Javadoc Support
===============

.. _\:JavaDocComment:

Commenting
----------

Eclim provides the **:JavaDocComment** command which will add or update the
javadoc comments for the element under the cursor.

.. note::

  The insertion of javadoc comments is done externally with Eclipse and with
  that comes a couple :ref:`caveats <vim/issues>`.


Configuration
-------------

Eclim Settings


- **org.eclim.user.name** - Used as the name portion of the author tag.
  Consult the :ref:`settings page <vim/settings>` for more info.
- **org.eclim.user.email** - Used as the email portion of the author tag.
  Consult the :ref:`settings page <vim/settings>` for more info.


.. _\:JavaDocSearch:

Searching
---------

Eclim supports searching of javadocs just like you would
:ref:`search the source code <vim/java/search>`.

The only difference is that you use **:JavaDocSearch** instead of
**:JavaSearch**.

The results will be displayed in a window and you can simply hit <enter> on an
entry to open it using the browser you configured via :ref:`g:EclimBrowser`.

The locations of the javadocs are determined via your Eclipse project's
.classpath file.  For each library entry you can define a javadoc attribute that
points to the base url of the javadoc (http, file, etc).

.. code-block:: xml

  <classpathentry kind="lib" path="lib/hibernate-3.0.jar">
    <attributes>
      <attribute value="http://hibernate.org/hib_docs/v3/api" name="javadoc_location"/>
    </attributes>
  </classpath>


Configuration
-------------

Vim Variables

.. _g\:EclimJavaDocSearchSingleResult:

- **g:EclimJavaDocSearchSingleResult** -
  Determines what action to take when only a singe result is found.

  Possible values include\:

  - 'open' - open the result in a browser.
  - 'lopen' - open the temp window to display the result.


.. _\:Javadoc:

Executing javadoc
-----------------

To run the javadoc utility on your project's source code, you may use the
**:Javadoc** command, which with no arguments will execute javadoc against all
your project's source code (as specified by any optional settings described
below).

If you wish to run javadoc only against one or more files, you can supply the
project relative paths as arguments to the **:Javadoc** command and only those
files will be used.

.. note::

  Please note that this command is not intended to be a full replacement for
  javadoc support provided by more comprehensive build tools like ant or maven.

Configuration
-------------

Eclim Settings

- **org.eclim.java.doc.dest** (Default: doc) -
  The project relative directory where the javadocs with be written to.
- **org.eclim.java.doc.packagenames** -
  Optional space separated list of package names to run javadoc against.
- **org.eclim.java.doc.sourcepath** -
  The project relative javadoc sourcepath to use.  This should be a space
  separated list of project relative source directories which you want javadoc
  to be executed against.  When unset, all your configured source directories
  will be used.
