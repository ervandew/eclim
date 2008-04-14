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

.. _vim/xml/definition:

Xml Definition Lookup
=====================

When editing xml files, eclim provides a couple commands which allow you to
quickly and easily open the file's data definition and optionally jump to the
definition of a particular element.

.. note::

  When opening urls, these commands rely on netrw (:help netrw).

.. _DtdDefinition:

- **:DtdDefinition** [<element>] -
  When invoked, this command will attempt to locate the dtd declaration in the
  current xml file and open the dtd in a new split window.  If you supply an
  element name when invoking the command, it will attempt to locate and jump to
  the definition of that element within the dtd.  If no element name is
  supplied, but the cursor is located on an element name when invoke, that
  element name will be used.

.. _XsdDefinition:

- **:XsdDefinition** [<element>] -
  Behaves like **:DtdDefinition** except this command locates and opens the
  corresponding schema definition file.
