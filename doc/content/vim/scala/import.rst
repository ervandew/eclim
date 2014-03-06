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

.. _\:ScalaImport:

Automated Imports
=================

The automated import functionality is pretty straightforward. Simply
place the cursor over the element to import and issue the command:

**:ScalaImport**

and one of the following events will occur:

- If only one matching element is found, its import statement will be placed in
  the file.
- If multiple matching elements are found, you will be prompted to choose the
  element you wish to import from a list.
- If an element with the same name is already imported then no changes will
  occur.

.. note::

  Like the scala-ide (as of the time of this writting), imports are simply
  appended to the end of your file's import block. There is no attempt made to
  sort or group imports.

Configuration
-------------

:doc:`Eclim Settings </vim/settings>`

- :ref:`org.eclim.java.import.exclude` - Scala importing honors the java import
  exclussion setting.
