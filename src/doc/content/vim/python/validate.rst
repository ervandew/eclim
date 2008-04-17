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

.. _vim/python/validate:

Python Validation
=================

When editing a python file eclim will default to validating the file when it is
written.  Any errors will be added to the current window's location list (:help
location-list) and their corresponding line number noted via Vim's sign
functionality.

Python validation currently uses both the python compiler to check for syntax
errors and pyflakes_ to perform some additional validation.  To make use of the
pyflakes portion of the validation you will first need to install pyflakes and
make sure it is in your path.

If you don't want python files validated when saving them, you can set the
g:EclimPythonValidate variable described in the configuration section below.

.. _Validate:

Regardless of whether you have validation enabled upon saving or not, the
command **:Validate** is available to manual validate the file.

.. _PyLint:

**:PyLint** -
Runs the `pylint tool <http://www.logilab.org/857>`_ on the current file,
populates the quickfix list with the results (:h quickfix), and marks all the
affected lines using vim's sign support.


Configuration
-------------

Vim Variables

.. _EclimPythonValidate:

- **g:EclimPythonValidate** (Default 1) -
  If set to 0, disables python validation when saving the file.

.. _pyflakes: http://www.divmod.org/trac/wiki/DivmodPyflakes
