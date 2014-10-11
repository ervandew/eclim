.. Copyright (C) 2005 - 2014  Eric Van Dewoestine

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

Python Search
=============

.. _\:PythonSearch:

Element Search
--------------

Element searching allows you to place the cursor over just about any element in
a source file (method call, class name, constant) and perform a search for that
element by issuing the command **:PythonSearch**. By default this command will
search for declarations, but you can specify that you want to search for
references using:

.. code-block:: vim

  :PythonSearch -x references

If only one result is found and that result is in the current source file, the
cursor will be moved to the element found. Otherwise, on single result matches,
the value of :ref:`g:EclimPythonSearchSingleResult` will be consulted for the
action to take. If there are multiple results, the location list will be opened
with the list of results. You can also force the action to use for the current
search by suppling the ``-a <action>`` arg:

.. code-block:: vim

  :PythonSearch -x references -a edit

.. note::

  **:PythonSearch** does not currently support searching for patterns.
  Attempting to supply a pattern to search for will result in an error.

.. _\:PythonSearchContext:

As a convenience eclim also provides the command **:PythonSearchContext**.
This command accepts only the optional ``-a`` argument described above, and will
perform the appropriate search depending on the context of the element under the
cursor.

- If the cursor is on the declaration of a class, function, or method then it
  will search for all occurrences.
- Otherwise, it will search for the declaration of the element.

Configuration
-------------

:doc:`Vim Settings </vim/settings>`

.. _g\:EclimPythonSearchSingleResult:

- **g:EclimPythonSearchSingleResult** (Default: 'split') -
  Determines what action to take when a only a single result is found.

  Possible values include\:

  - 'split' - open the result in a new window via "split".
  - 'edit' - open the result in the current window.
  - 'tabnew' - open the result in a new tab.

  This setting overrides the global default for all supported language types
  which can be set using the **g:EclimDefaultFileOpenAction** setting which
  accepts the same possible values.

- **g:EclimQuickfixHeight** (Default: 10) -
  Sets the height in lines of the quickfix window when eclim opens it to display
  search results.
