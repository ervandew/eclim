.. Copyright (C) 2005 - 2015  Eric Van Dewoestine

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

Ruby Search
===========

.. _\:RubySearch:

Pattern Search
--------------

Pattern searching provides a means to widen a search beyond a single
element.  A pattern search can be executed using the command

**:RubySearch** -p <pattern> [-t <type> -s <scope> -i -a <action>]

When there is more than 1 result, those results will be placed into vim's
quickfix list (:help quickfix) so that you can easily navigate them using vim's
quickfix commands.

Vim command completion is supported through out the command with the exception
of the pattern to search for.

.. code-block:: vim

  :RubySearch <Tab>
  :RubySearch -p MyClass* <Tab>
  :RubySearch -p MyClass* -t <Tab>
  :RubySearch -p MyClass* -t class <Tab>
  :RubySearch -p MyClass* -t class -s <Tab>
  :RubySearch -p MyClass* -t class -s project

- -p <pattern>: The pattern to search for.

  Ex.

  ::

    MyClass
    myFunction
    my*

- -t <type> (Default: all): The type of element to search for where possible
  types include

  - class
  - method
  - field

- -x <context> (Default: declarations): The context of the search, where
  possible values include

  - all - All occurances.
  - declarations - Declarations matching the pattern or element.
  - references - References of the pattern or element.

- -s <scope> (Default: all): The scope of the search where possible values
  include

  - all - Search the whole workspace.
  - project - Search the current project, dependent projects, and libraries.

- -i: Ignore case when searching.

- -a: The vim command to use to open the result (edit, split, vsplit, etc).

Element Search
--------------

Element searching allows you to place the cursor over just about any element in
a source file (method call, class name, constant) and perform a search for that
element.  Performing an element search is the same as performing a pattern
search with the exception that you do not specify the -p option since the
element under the cursor will be searched for instead.

If only one result is found and that result is in the current source file, the
cursor will be moved to the element found. Otherwise, on single result
matches, the value of :ref:`g:EclimRubySearchSingleResult` will be consulted
for the action to take. If there are multiple results, the quickfix list will be
opened with the list of results.

.. _\:RubySearchContext:

As a convenience eclim also provides the command **:RubySearchContext**.  This
command accepts only the optional ``-a`` argument described above, and will
perform the appropriate search depending on the context of the element under the
cursor.

- If the cursor is on a the definition of a method, class, module, etc. then a
  search will be performed for all uses of that element.
- Otherwise, it will search for the declaration of the element.

Configuration
-------------

:doc:`Vim Settings </vim/settings>`

.. _g\:EclimRubySearchSingleResult:

- **g:EclimRubySearchSingleResult** (Default: 'split') -
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
