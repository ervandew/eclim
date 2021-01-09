.. Copyright (C) 2014  Eric Van Dewoestine

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

Python Interpreter / Paths
==========================

Python code completion, searching, and other features require that you first
create a python (pydev) project:

.. code-block:: vim

  :ProjectCreate path/to/project -n python

If you haven't already configured a python interpreter, then you will be
prompted to do so when creating your first python project.
When creating your project a ``.pydevproject`` file will be also be created.
This file is used to define which interpreter to use for your project, the
location of your project's python source files, and the location of any third
party libraries your project requires (if not already on your project's
interpreter path).

.. note::

  When saving the .pydevproject file from within vim, eclim will update your
  project's configuration in memory or report any errors raised by pydev.

  Also note that although the .pydevproject file is xml, pydev doesn't handle
  stripping leading/trailing space or new lines from xml text values, so refrain
  from attempting to format this file and try to stick to using the commands
  below to configure your project.

Interpreter
-----------

Eclim provides commands to help you manage python interpreters available to
pydev projects as well as which interpreter to use for each of your projects.

.. _\:PythonInterpreterAdd:

- **:PythonInterpreterAdd** [-n <name>] </path/to/pythonX>
  Command to add a new interpreter to pydev which will then be available to your
  projects. If you supply only the path to the interpreter, then eclim will set
  the name of that interpreter to the basename of the path supplied.

  .. code-block:: vim

    :PythonInterpreterAdd /usr/bin/python3
    :PythonInterpreterAdd -n python3.3 /usr/bin/python3

.. _\:PythonInterpreterRemove:

- **:PythonInterpreterRemove** </path/of/pythonX>
  Command to remove an interpreter from pydev.

  .. code-block:: vim

    :PythonInterpreterRemove /usr/bin/python3

.. _\:PythonInterpreterList:

- **:PythonInterpreterList**
  Command to list all interpreters configured with pydev.

.. _\:PythonInterpreter:

- **:PythonInterpreter** [<interpreter_name or /path/to/interpreter>]
  When invoked with no arguments this command will print out the path to the
  python interpreter currently set for your project. This command can also be
  use to set your project's interpreter by supplying either the name of an
  interpreter already configured with pydev (via :ref:`:PythonInterpreterAdd
  <:PythonInterpreterAdd>`), or the absolute path to an interpreter on your
  system.

  .. code-block:: vim

    :PythonInterpreter python_2.7
    :PythonInterpreter /usr/bin/python3

  This command supports command completion of interpreter names or paths (if you
  start typing an absolute path).

Paths
-----

.. _\:NewSrcEntry_pydev:

- **:NewSrcEntry** <dir> -
  Add a new source entry which references a source directory in your project.

  .. code-block:: xml

    <path>/myproject/src</path>

  This command supports command completion of project relative directories.
