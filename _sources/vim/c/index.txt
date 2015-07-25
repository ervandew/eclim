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

C/C++
=====

Features
--------

.. toctree::
   :maxdepth: 1

   project
   complete
   validate
   search
   inspection

Suggested Mappings
------------------

Here are some mappings for the c/c++ funtionality provided by eclim.  To make
use of these mappings, simply create a ftplugin file for c/cpp and place your
mappings there (:help ftplugin-name).

- The following mapping allows you to simply hit <enter> on an element to
  perform a search to find it.

  .. code-block:: vim

    nnoremap <silent> <buffer> <cr> :CSearchContext<cr>
