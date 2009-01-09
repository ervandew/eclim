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

.. _vim/vim/find:

Vim Find
========

When working on Vim scripts, eclim provides a set of commands for finding user
defined commands, functions, and global variables.

.. _\:FindCommandDef:

- **:FindCommandDef [command]** -
  Finds the definition of the supplied command name or command name under the
  cursor.

.. _\:FindCommandRef:

- **:FindCommandRef [command]** -
  Finds references of the supplied command name or command name under the
  cursor.

.. _\:FindFunctionDef:

- **:FindFunctionDef [function]** -
  Finds the definition of the supplied function name or function name under the
  cursor.

.. _\:FindFunctionRef:

- **:FindFunctionRef [function]** -
  Finds references of the supplied function name or function name under the
  cursor.

.. _\:FindVariableDef:

- **:FindVariableDef [variable]** -
  Finds the definition of the supplied global variable name or variable name
  under the cursor.

.. _\:FindVariableRef:

- **:FindVariableRef [variable]** -
  Finds references of the supplied global variable name or variable name under
  the cursor.

.. _\:FindByContext:

- **:FindByContext** -
  Performs the appropriate search for the element under the cursor based on its
  context.  If the cursor is on a command, function, or variable definition,
  then this command will search for references of that command, function, or
  variable.  If the cursor is on a reference of a command, function, or
  variable, then this command will search for the definition of the command,
  function, or variable.

All of the above commands support ! which can be used to prevent jumping to the
first of mulitple results found.  In the case of a single result, you can set
the g:EclimVimFindSingleResult variable to determine the action to take, as
described below.


Configuration
-------------

Vim Variables

.. _g\:EclimVimFindSingleResult:

- **g:EclimVimFindSingleResult** (Default: 'split') -
  Determines what action to take when a only a single result is found.

  Possible values include\:

  - 'split' - open the result in a new window via "split".
  - 'edit' - open the result in the current window.
  - 'lopen' - open the location list to display the result.

.. _g\:EclimVimPaths:

- **g:EclimVimPaths** (Default: &runtimepath) -
  Comma seperated list of paths to recursively search.  Defaults to your systems
  'runtimepath' option which should be suitable for most users.
