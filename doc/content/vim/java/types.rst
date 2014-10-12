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

Type Creation
=============

.. _\:JavaNew:

Creating a new Class, Interface, etc.
-------------------------------------

**:JavaNew** is a command that allows you to create new classes, interfaces,
enums, or annotations by specifying which of those you'd like to create,
followed by the fully qualified path of the new type.

The available types you can create include:

- **class** - a new class
- **interface** - a new interface
- **abstract** - a new abstract class
- **enum** - a new enum
- **@interface** - a new annotation

Here are some examples:

.. code-block:: vim

  :JavaNew class org.test.MyNewClass
  :JavaNew interface org.test.MyNewInterface

If you ommit the package name, the new type will be created in the same package
as the file you are currently editing:

.. code-block:: vim

  :JavaNew class MyNewClass

.. note::

  This command supports command completion of the available types (class,
  interface, etc) as well as completion of existing package names.

In the case where you are creating a new type in a package that does not yet
exist, eclim will do its best to create that new package in the correct source
file directory. If you have multiple source directories with packages that
partially match the new package, eclim will currently choose the one which
occurs first in your ``.classpath`` file.
