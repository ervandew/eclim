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

.. _vim/mappings:

Suggested Vim Mappings
======================

Since each person has their own preference when it comes to key mappings,
eclim deliberately omits any convenience mappings for the provided
functionality.

So, instead this page provides some suggested mappings and examples that
may or may not suit your tastes, but will at least give you a template
that you can use to define your own mappings.

.. note::

  In the mappings below you will see <leader> used quite frequently.  In Vim the
  <leader> argument is mapped to a character of your choice.  Please see ":help
  mapleader" in Vim for more information.

**Ant Mappings**

Here are some mappings for the ant funtionality provided by eclim.  To make use
of these mappings, simply create a ftplugin file for ant and place your mappings
there (:help ftplugin-name).

- Lookup and open the documentation for the ant element under the cursor with
  <enter>.

  .. code-block:: vim

    noremap <silent> <buffer> <cr> :AntDoc<cr>

**Java Mappings**

Here are some mappings for the java funtionality provided by eclim.  To make use
of these mappings, simply create a ftplugin file for java and place your
mappings there (:help ftplugin-name).

- This mapping can be used to allow quick replacement of template variables when
  utilizing the :ref:`java abbreviations <vim/java/abbreviations>`.

  .. code-block:: vim

    nnoremap <silent> <buffer> <tab> :call eclim#util#FillTemplate("${", "}")<cr>

- Import the class under the cursor with <leader>i.

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

**Php Mappings**

Here are some mappings for the php funtionality provided by eclim.  To make use
of these mappings, simply create a ftplugin file for php and place your mappings
there (:help ftplugin-name).

- The following mapping allows you to simply hit <enter> on an element to
  perform a search to find it.

  .. code-block:: vim

    nnoremap <silent> <buffer> <cr> :PhpSearchContext<cr>

**Python Mappings**

Here are some mappings for the python funtionality provided by eclim.  To make
use of these mappings, simply create a ftplugin file for python and place your
mappings there (:help ftplugin-name).

- If you are doing django development you may want to use the following mapping
  which will execute **:DjangoViewOpen**, or **:DjangoTemplateOpen** depending
  on the context of the text under the cursor\:

  .. code-block:: vim

    nnoremap <silent> <buffer> <cr> :DjangoContextOpen<cr>

**Vim Script Mappings**

Here are some mappings for the Vim script funtionality provided by eclim.  To
make use of these mappings, simply create a ftplugin file for Vim and place your
mappings there (:help ftplugin-name).

- Perform a context sensitive search for the element under the cursor
  using <enter>.

  See :ref:`:FindByContext <findbycontext>` for more info.

  .. code-block:: vim

    " avoid overwriting <cr> mapping in 'command-line' buffer (:h cmdwin).
    if bufname('%') != 'command-line'
      nnoremap <silent> <buffer> <cr> :FindByContext<cr>
    endif
