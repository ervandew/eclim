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

.. _\:Validate_python:

Regardless of whether you have validation enabled upon saving or not, the
command **:Validate** is available to manual validate the file.

.. _\:PyLint:

**:PyLint** -
Runs the pylint_ tool on the current file, populates the quickfix list with the
results (:h quickfix), and marks all the affected lines using vim's sign
support.

.. note::

  When running **:PyLint**, determining additional directories to be include on
  the path for pylint_ is provided via eclim's integration with
  :ref:`rope <python-rope>`.

  Please see the :ref:`rope <python-rope>` docs for more information.


Configuration
-------------

Vim Variables

.. _g\:EclimPythonValidate:

- **g:EclimPythonValidate** (Default 1) -
  If set to 0, disables python validation when saving the file.

- **g:EclimValidateSortResults** (Default: 'occurrence') -
  If set to 'severity', the validation results will be sorted by severity
  (errors > warnings > info > etc.)

.. _pyflakes: http://www.divmod.org/trac/wiki/DivmodPyflakes
.. _pylint: http://www.logilab.org/857
