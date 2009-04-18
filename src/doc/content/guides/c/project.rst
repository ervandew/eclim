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

.. _guides/c/project:

Creating A C/C++ Project
========================

The goal of this guide is to walk you through creating your first c or c++
project via eclim.


Creating a project
------------------

The first step is to open a Vim window and create the project by executing one
of\:

.. code-block:: vim

  :ProjectCreate /path/to/my_c_project -n c
  :ProjectCreate /path/to/my_cpp_project -n c++

The path supplied will be the path to the root of your project.  This path may
or may not exist.  If it does not exist it will be created for you.

Once you've created your project you can use the **:ProjectList** command to
list the available projects and you should see your newly created one in the
list.

::

  my_c_project - open   - /path/to/my_c_project


The **:ProjectList** result is in the form of ``projectName - (open|closed) -
/project/root/path``.  When you create projects, the last path element will be
used for the project name.  If that element contains any spaces, these will be
converted to underscores.

Once you have a project, you can then configure your project by setting the
location of your source files and add any necessary include paths. To do so,
start by opening the project configuration by executing:

.. code-block:: vim

  :CProjectConfigs my_c_project

This will open a temporary buffer which displays some of the core configuration
values supported by the eclipse cdt:

::

  Config: Linux GCC

    Sources: |add|
      dir:    /

    Tool: GCC C Compiler
      Includes: |add|
      Symbols:  |add|

    Tool: GCC Assembler
      Includes: |add|

Under the "Sources" section you should see a default source directory entry of
"/", representing your project root.  If you'd like to instead use a "src/"
directory, you can first create a "src" directory in your project root:

::

  $ mkdir src

Once the directory exists you can then move the cursor over the "\|add\|" link
on the "Sources" section and hit <enter>.  When prompted for a directory, enter
"src" and hit <enter>.  At this point it will prompt for any excludes, but for
the purposes of this guide, you can simply hit enter without supplying a value.

Now your sources should have both the project root and "src" listed.  You can
now delete the root entry by moving your cursor over that line and hitting D
(shift-d).

If you have any include paths you wish to add, you can follow a similar
procedure to add them by moving your cursor over the relevant "\|add\|" link
and supply the proper project relative or absolute path to the include path you
wish to add.
