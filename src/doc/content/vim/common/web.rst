.. Copyright (C) 2005 - 2009  Eric Van Dewoestine

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

.. _vim/common/web:

Web Lookup Commands
===================

Eclim provides a set of commands that allow you to open information in the web
browser or your choice.

.. _\:OpenUrl:

OpenUrl
-------

The first of these commands is **:OpenUrl** which opens a url in your web
browser, or optionally in Vim via netrw (:help netrw).

When executing the command you may supply the url to open, or if ommitted, it
will open the url under the cursor.  By default all urls will open in your web
browser, but you may optionally configure a list of url patterns to be opened
via the netrw plugin.  The following example is configured to open all dtd, xml,
xsd, and text files via netrw.

.. code-block:: vim

  let g:EclimOpenUrlInVimPatterns =
    \ [
      \ '\.\(dtd\|xml\|xsd\)$',
      \ '\.txt$',
    \ ]

For urls that match one of these patterns, you may also define how the file is
to be opened in Vim (split, edit, etc.).

.. code-block:: vim

  let g:EclimOpenUrlInVimAction = 'split'

If a url you want to open matches one
of these patterns, but you want to force it to be opened in your browser, you
can supply a bang (!) to force it to do so:

::

  :OpenUrl!


Configuration
-------------

Vim Variables

.. _g\:EclimOpenUrlInVimPatterns:

- **g:EclimOpenUrlInVimPatterns** (Default: []) -
  Defines a list of url patterns to open in Vim via netrw.

.. _g\:EclimOpenUrlInVimAction:

- **g:EclimOpenUrlInVimAction** (Default: 'split') -
  Defines the command used to open files matched by g:EclimOpenUrlInVimPatterns.


Web Search Commands
-------------------

In addition to simply opening a url, eclim also provides a couple helper
functions which allow you to create simple commands or mappings to search the
web using your favorite search engine or to lookup a word via an online
reference.

.. _eclim#web#SearchEngine:

- **eclim#web#SearchEngine**
  This function provides the functionality needed to create search engine
  commands or mappings.

  .. code-block:: vim

    command -range -nargs=* Google call eclim#web#SearchEngine(
      \ 'http://www.google.com/search?q=<query>', <q-args>, <line1>, <line2>)

  Adding the above command to your vimrc or similar provides you with a new
  :Google command allowing you to start a search on google.com_ in your browser
  from vim.  This command can be invoked in a few ways.

  #. First by supplying the word or words to search for as arguments to
     the command.

      .. code-block:: vim

        :Google "vim eclim"
        :Google vim eclim
        :Google +vim -eclim

    Note that you can supply the arguments to the command just as you would when
    using the search input via google's homepage, allowing you to utilize the
    full querying capabilities of google.

  #. The second method is to issue the command with no arguments. The
     command will then query google with the word under the cursor.

  #. The last method is to visually select the text you want to search for and
     then execute the command.

.. _eclim#web#WordLookup:

- **eclim#web#WordLookup**
  This function can be used to create commands or mappings which lookup a word
  using an online reference like a dictionary or thesaurus.

  .. code-block:: vim

    command -nargs=? Dictionary call eclim#web#WordLookup(
      \ 'http://dictionary.reference.com/search?q=<query>', '<args>')

  Adding the above command to your vimrc or similar provides you with a new
  :Dictionary command which can be used to look up a word on
  dictionary.reference.com_.  You can either supply the word to lookup as an
  argument to the command or it will otherwise use the word under the cursor.

.. _google.com: http://google.com
.. _dictionary.reference.com: http://dictionary.reference.com
