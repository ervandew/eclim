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

.. _\:CProjectConfigs:

C/C++ Project Configuration
===========================

The eclipse cdt provides a large set of configuration support for your c/c++
projects.  Eclim exposes a subset of these to you using the
**:CProjectConfigs** command:

.. code-block:: vim

  :CProjectConfigs

  " or if you are outside of the project
  :CProjectConfigs my_c_project

This command will open a temporary buffer displaying some of the cdt
configuration values available to you.  In this buffer you can add or remove
source directory references, include path references, and symbols.

Here is a small example of what the contents may look like:

::

  Config: Linux GCC

    Sources: |add|
      dir:    src

    Tool: GCC C Compiler
      Includes: |add|
        path:    "${workspace_loc:/my_c_project/includes}"
      Symbols:  |add|

    Tool: GCC Assembler
      Includes: |add|

To add a source directory, include path, or symbol, simply move the cursor over
the relevant "\|add\|" link and hit <enter>.  You will then be prompted to
enter an appropriate value.  For your convenience, tab completion is provided
where possible.

.. note::

  Despite the odd looking value in the includes path section above, to add the
  entry you simply need to supply the project relative path, "includes/" in
  this case, when prompted by the add command.

If at any point you would like to remove a value, you can move the cursor over
the line of the value you would like to remove and hit D (shift-d) to delete
the entry.

.. _eclim user: http://groups.google.com/group/eclim-user
