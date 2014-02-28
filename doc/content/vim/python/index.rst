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

Python
======

Features
--------

.. toctree::
   :maxdepth: 1

   path
   validate
   complete
   search
   django

Suggested Mappings
------------------

Here are some mappings for the python funtionality provided by eclim.  To make
use of these mappings, simply create a ftplugin file for python and place your
mappings there (:help ftplugin-name).

- The following mapping allows you to simply hit <enter> on an element to
  perform a search to find its definition or occurrences depending on the
  context.

  .. code-block:: vim

    nnoremap <silent> <buffer> <cr> :PythonSearchContext<cr>

- If you are doing django development you may want to use the following mapping
  instead which also supports locating django templates when executed over a
  quoted template path in a view, or locating django views when executed on a
  quoted view name in a urls.py file.

  .. code-block:: vim

    nnoremap <silent> <buffer> <cr> :DjangoContextOpen<cr>
