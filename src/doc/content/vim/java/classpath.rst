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

.. _vim/java/classpath:

Eclipse Classpath Editing
=========================

Source code completion, searching, auto imports, all rely on a properly
configured classpath.  When you first create a project, a ``.classpath`` file is
created in the project's root directory.  If you created the project on an
existing code-base, eclim will attempt to setup the ``.classpath`` file with any
source code directories or libraries in the project.

Regardless of the state of your project you will at some point need to update
the classpath.  The primary method of doing so, is to directly edit the
``.classpath`` to add, update, or remove entries as needed. To help you do this,
eclim provides several commands to ease the creation of new classpath entries
and variables.

.. note::

  All of the commands described below are only available while editing the
  ``.classpath`` file in vim.

  When you write the ``.classpath`` file, Vim will issue a command to the eclim
  server to update the project's classpath, and will report any errors via vim's
  location list (:help location-list).

  In addition to directly editing the ``.classpath`` file, you may
  also use maven's support
  (:ref:`1.x <guides/java/maven/maven/classpath>` or
  :ref:`2.x <guides/java/maven/mvn/classpath>`) for
  maintaining the eclipse classpath.  For users who use ivy_, eclim also
  provides a means to
  :ref:`auto update the eclipse classpath <guides/java/ivy/classpath>` when
  saving changes to your ivy.xml.

.. _\:NewSrcEntry:

- **:NewSrcEntry** <dir> [<dir> ...] -
  Adds a new entry for one or more source code directories relative to the
  project's root directory.

  .. code-block:: xml

    <classpathentry kind="src" path="src/java"/>

  This command supports command completion of directories relative to the
  .classpath file.

.. _\:NewProjectEntry:

- **:NewProjectEntry** <project> [<project> ...] -
  Adds a new entry for one or more dependencies on other projects.

  .. code-block:: xml

    <classpathentry exported="true" kind="src" path="/a_project"/>

  This command supports command completion of project names.

.. _\:NewJarEntry:

- **:NewJarEntry** <file> [<file> ...] -
  Adds a new entry for one or more jar file dependencies.  If the jar file is
  not in a folder under the project root, you must use an absolute path
  (apparent limitation with Eclipse).  When adding the template, this command
  also adds the necessary (although commented out) elements and attributes to
  set the location of the jar's source code and javadocs.

  .. code-block:: xml

    <classpathentry exported="true" kind="lib" path="lib/hibernate-3.0.jar">
      <!--
        sourcepath="<path>">
      -->
      <!--
      <attributes>
        <attribute value="file:<javadoc>" name="javadoc_location"/>
      </attributes>
      -->
    </classpath>

.. _\:NewVarEntry:

- **:NewVarEntry** <VAR/file> [<VAR/file> ...] -
  Just like NewJarEntry except an Eclipse "var" entry is created.  When a jar
  entry references an absolute path, you should instead use a var entry.  The
  var entry allows you to define a base dir as a variable (ex. USER_HOME =
  /home/username), and then reference files relative to that variable.

  .. code-block:: xml

    <classpathentry exported="true" kind="var" path="USER_HOME/lib/hibernate-3.0.jar">
      <!--
        sourcepath="<path>">
      -->
      <!--
      <attributes>
        <attribute value="http://<javadoc>" name="javadoc_location"/>
      </attributes>
      -->
    </classpath>

  This allows you to share .classpath files with other developers without each
  having a local copy with environment specific paths.

  To add new base classpath variables, you can edit
  ``$ECLIPSE_HOME/plugins/org.eclim_version/classpath_variables.properties``

  By default, a USER_HOME variable is created that defaults to the java System
  property "user.home" and you can add more as needed.

  This command supports command completion of Eclipse variable names as well as
  the files and directories beneath the path the variable represents.

  To manage the classpath variables, eclim also provides the following
  commands.

  .. _\:VariableList:

  - **:VariableList** -
    Lists all the currently available classpath variables and their
    corresponding values.

  .. _\:VariableCreate:

  - **:VariableCreate** <name> <path> -
    Creates or updates the variable with the supplied name.

  .. _\:VariableDelete:

  - **:VariableDelete** <name> -
    Deletes the variable with the supplied name.

.. _ivy: http://jayasoft.org/ivy
