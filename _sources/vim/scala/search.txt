.. Copyright (C) 2012 - 2014  Eric Van Dewoestine

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

.. _\:ScalaSearch:

Scala Search
============

Eclim's scala searching currently supports searching for the definition of the
element under the cursor. Simply place the cursor on the element you wish to
search for and run **:ScalaSearch**. If the definition of the element is found,
the corresponding file will be opened and the cursor placed on the element's
definition. The command used to open the result can be defined usin the
:ref:`g:EclimScalaSearchSingleResult <g:EclimScalaSearchSingleResult>` variable
or on a per search basis by supplying the ``-a <action>`` arg:

.. code-block:: vim

  :ScalaSearch -a edit

Configuration
-------------

:doc:`Vim Settings </vim/settings>`

.. _g\:EclimScalaSearchSingleResult:

- **g:EclimScalaSearchSingleResult** (Default: 'split') -
  Determines what action to take when a only a single result is found.

  Possible values include\:

  - 'split' - open the result in a new window via "split".
  - 'edit' - open the result in the current window.
  - 'tabnew' - open the result in a new tab.

  This setting overrides the global default for all supported language types
  which can be set using the **g:EclimDefaultFileOpenAction** setting which
  accepts the same possible values.

.. include:: /vim/search.rst
   :start-after: begin-search-quickfix
   :end-before: end-search-quickfix
