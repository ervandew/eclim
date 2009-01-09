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

.. _vim/java/import:

Automated Imports
=================

.. _\:JavaImport:

The automated import functionality is pretty straightforward.  Simply
place the cursor over the element to import and issue the command\:

  **:JavaImport**

and one of the following events will occur\:


- If only one matching element is found, its import statement will be placed in
  the file.
- If multiple matching elements are found, you will be prompted to choose the
  element you wish to import from a list.
- If an element with the same name is already imported, the element is in
  java.lang, or the element is in the same package as the current src file, then
  a simple prompt will alert you that the element does not need to be imported.

In addition to importing elements, the plugin provides two additional
commands\:

.. _\:JavaImportSort:

- **:JavaImportSort** -
  Sorts the import statements in alphabetical order with java and javax
  imports first.

.. _\:JavaImportClean:

- **:JavaImportClean** -
  Removes any un-used import statements.  If the current file is not in an
  Eclipse project, then a Vim only implementation is invoked, that behaves the
  same as the server side version, except it does not take into account object
  names that are commented out.


Configuration
-------------

Vim Variables

.. _g\:JavaImportExclude:

- **g:JavaImportExclude** -
  List of patterns to exclude from import results.

  Ex.

  .. code-block:: vim

    let g:JavaImportExclude = [ "^com\.sun\..*", "^sun\..*", "^sunw\..*" ]
