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

.. _vim/php/include:

PDT Include Path
================

Source code completion, searching, and other features make use of the pdt's
.projectOptions to locate external php resource.  When you first create a
project, a ``.projectOptions`` file is created in the project's root directory.
If your project depends on any php files located outside your project or in
another project, then you'll need to edit your .projectOptions accordingly.

To help you do this, eclim provides several commands to ease the creation of new
include path entries and variables, all of which are made available when you
edit your .projectOptions file in vim.  Also when you write the
``.projectOptions`` file, Vim will issue a command to the eclim server to update
the project's include path, and will report any errors via vim's location list
(:help location-list).

The following is a list of commands that eclim provides while editing your
.projectOptions.

.. _\:NewLibEntry:

- **:NewLibEntry** <dir> [<dir> ...] -
  Adds one or more new entries which reference external source
  directories.

  .. code-block:: xml

    <includepathentry kind="lib" path="/usr/local/php/cake_1.1.16.5421" createdReference="false" contentKind="source">
    </includepathentry>

  This command supports command completion of directories.

.. _\:NewProjectEntry:

- **:NewProjectEntry** <project> [<project> ...] -
  Adds one or more new entries which reference other projects.

  .. code-block:: xml

    <includepathentry kind="prj" path="/test_php" resource="test_php" createdReference="true" contentKind="source">
    </includepathentry>

  This command supports command completion of project names.

.. _\:NewVarEntry:

- **:NewVarEntry** <VAR/file> [<VAR/file> ...] -
  Just like NewLibEntry except an Eclipse "var" entry is created.  When adding
  references to external paths variables come in handy since other developers
  working with the project can specify where their version of the files are
  located.  This is especially useful if developers are working on different
  OSes where path locations will vary.

  The var entry allows you to define a base dir as a variable (ex.  USER_HOME =
  /home/username), and then reference files relative to that variable.

  .. code-block:: xml

    <includepathentry kind="var" path="CAKE" createdReference="false" contentKind="source">
    </includepathentry>

  To manage the include path variables, eclim provides the following
  commands.

  .. _\:VariableList:

  - **:VariableList** -
    Lists all the currently available include path variables and their
    corresponding values.

  .. _\:VariableCreate:

  - **:VariableCreate** <name> <path> -
    Creates or updates the variable with the supplied name.

  .. _\:VariableDelete:

  - **:VariableDelete** <name> -
    Deletes the variable with the supplied name.
