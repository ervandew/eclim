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

Java Bean Functionality
=======================

Eclim currently provides the ability to generate java bean getters and setters
from one or more defined fields.


.. _\:JavaGetSet:

- **:JavaGetSet** -
  Generates both getters and setters for the field under the cursor or for
  all fields in the specified range.

.. _\:JavaGet:

- **:JavaGet** -
  Generates getters for the field under the cursor or for all fields in
  the specified range.

.. _\:JavaSet:

- **:JavaSet** -
  Generates setters for the field under the cursor or for all fields in
  the specified range.

Given the following file\:

.. code-block:: java

  public class Foo
  {
    private String name;
    private Bar[] bars;
  }

You can place the cursor on one of the fields and execute **:JavaGetSet** to
generate the getters and setters for the field.  All of the above commands
support ranges as well, so you can use a visual selection or a numbered range to
generate methods for a set of fields.

.. note::

  The insertion of these methods is done externally with Eclipse and with
  that comes a couple :doc:`caveats </vim/gotchas>`.


Configuration
-------------

Vim Variables

.. _g\:EclimJavaBeanInsertIndexed:

- **g:EclimJavaBeanInsertIndexed** (Default: 1) -
  When set to a value greater than 0, eclim will insert indexed getters and
  setters for array properties.
