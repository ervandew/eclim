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

Automated Imports
=================

.. _\:JavaImport:

The automated import functionality is pretty straightforward.  Simply
place the cursor over the element to import and issue the command:

**:JavaImport**

and one of the following events will occur:

- If only one matching element is found, its import statement will be placed in
  the file.
- If multiple matching elements are found, you will be prompted to choose the
  element you wish to import from a list.
- If an element with the same name is already imported, the element is in
  java.lang, or the element is in the same package as the current src file, then
  a simple prompt will alert you that the element does not need to be imported.

.. _\:JavaImportMissing:

If you have numerous elements that you wish to import, you can also use:

**:JavaImportMissing**

which will look for all undefined types and attempt to find possible imports
for them.  If there is a single result then that result will be imported.  In
the event of multiple results for a given type, then you will be prompted to
choose the result to import, just like when using **:JavaImport**.

In addition to importing elements, this plugin provides two additional
commands:

.. _\:JavaImportSort:

- **:JavaImportSort** -
  Sorts the import statements in alphabetical order with java and javax
  imports first.

.. _\:JavaImportClean:

- **:JavaImportClean** -
  Removes any unused import statements.  If the current file is not in an
  Eclipse project, then a Vim only implementation is invoked, that behaves the
  same as the server side version, except it does not take into account object
  names that are commented out.


Configuration
-------------

Vim Variables

.. _g\:EclimJavaImportExclude:

- **g:EclimJavaImportExclude** -
  List of patterns to exclude from import results.

  Ex.

  .. code-block:: vim

    let g:EclimJavaImportExclude = [ "^com\.sun\..*", "^sun\..*", "^sunw\..*" ]

.. _g\:EclimJavaImportPackageSeparationLevel:

- **g:EclimJavaImportPackageSeparationLevel** (Default: -1) -
  Used to determine how imports are grouped together (or spaced apart).  The
  number represents how many segments of the package name to use to determine
  equality, where equal imports are grouped together and separated from other
  groups with a blank line.

  - -1: Use the entire package name. Only imports from the same full package
    are grouped together.
  - 0: Don't look at any package segments. All imports are grouped together
    with no spacing.
  - n: Look at the first `n` segments of the package name.

    Ex.

    .. code-block:: vim

      let g:EclimJavaImportPackageSeparationLevel = 2

Eclim Settings

.. _org.eclipse.jdt.ui.importorder:

- **org.eclipse.jdt.ui.importorder** (Default: java;javax;org;com) -
  Semicolon separated list of package names which specify the sorting order for
  import statements.  This settings is the same setting used by the eclipse gui
  in the "Organize Imports" preference dialog.
