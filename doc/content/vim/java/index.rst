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

Java
======

Features
--------

.. toctree::
   :maxdepth: 1

   classpath
   validate
   complete
   search
   java
   debug
   javadoc
   format
   refactor
   inspection
   import
   methods
   unittests
   logging

.. toctree::
   :maxdepth: 1

   ant
   maven
   android
   webxml

Suggested Mappings
------------------

Here are some mappings for the java funtionality provided by eclim.  To make use
of these mappings, simply create a ftplugin file for java and place your
mappings there (:help ftplugin-name).

- Import the class under the cursor with <leader>i (:h mapleader):

  .. code-block:: vim

    nnoremap <silent> <buffer> <leader>i :JavaImport<cr>

- Search for the javadocs of the element under the cursor with
  <leader>d.

  .. code-block:: vim

    nnoremap <silent> <buffer> <leader>d :JavaDocSearch -x declarations<cr>

- Perform a context sensitive search of the element under the cursor with
  <enter>.

  .. code-block:: vim

    nnoremap <silent> <buffer> <cr> :JavaSearchContext<cr>
