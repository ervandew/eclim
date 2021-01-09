.. Copyright (C) 2005 - 2020  Eric Van Dewoestine

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

.. _\:JavaImport:

Automated Imports
=================

The automated import functionality is pretty straightforward. Simply
place the cursor over the element to import and issue the command:

**:JavaImport**

and one of the following events will occur:

- If only one matching element is found, its import statement will be placed in
  the file.
- If multiple matching elements are found, you will be prompted to choose the
  element you wish to import from a list.
- If an element with the same name is already imported, the element is in
  java.lang, or the element is in the same package as the current src file, then
  no changes will occur.

.. _\:JavaImportOrganize:

In addition to adding imports one by one, you can also add them in bulk along
with the removal of unused imports and the sorting and formating of all the
file's import statements using the command:

**:JavaImportOrganize**

Configuration
-------------

:doc:`Eclim Settings </vim/settings>`

.. _org.eclipse.jdt.ui.importorder:

- **org.eclipse.jdt.ui.importorder** (Default: java;javax;org;com) -
  Semicolon separated list of package names which specify the sorting order for
  import statements.  This settings is the same setting used by the eclipse gui
  in the "Organize Imports" preference dialog.

.. _org.eclim.java.import.exclude:

- **org.eclim.java.import.exclude** (Default: ["^com\.sun\..*", "^sunw?\..*"]) -
  List of patterns to exclude from import results.

.. _org.eclim.java.import.package_separation_level:

- **org.eclim.java.import.package_separation_level** (Default: 1) -
  Used to determine how imports are grouped together (or spaced apart).  The
  number represents how many segments of the package name to use to determine
  equality, where equal imports are grouped together and separated from other
  groups with a blank line.

  - -1: Use the entire package name. Only imports from the same full package
    are grouped together.
  - 0: Don't look at any package segments. All imports are grouped together
    with no spacing.
  - n: Look at the first `n` segments of the package name.
