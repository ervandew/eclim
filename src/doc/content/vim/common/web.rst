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

.. _vim/common/web:

Web Lookup Commands
===================

Eclim provides a set of commands that allow you to open information in the web
browser or your choice.

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

Configuration
-------------

Vim Variables

.. _EclimOpenUrlInVimPatterns:

- **g:EclimOpenUrlInVimPatterns** (Default: []) -
  Defines a list of url patterns to open in Vim via netrw.

.. _EclimOpenUrlInVimAction:

- **g:EclimOpenUrlInVimAction** (Default: 'split') -
  Defines the command used to open files matched by g:EclimOpenUrlInVimPatterns.


Web Search Commands
-------------------

The remaining web lookup commands provide a means for looking up words or
phrases using some great online tools.

.. _Google:

- **:Google** [word ...] -
  This command is used to look up a word or phrase on
  <a href="http://google.com">google.com</a>.  You can invoke it one of
  two ways.

  #.  First by supplying the word or words to search for as arguments to
      the command.

      .. code-block:: vim

        :Google "vim eclim"
        :Google vim eclim
        :Google +vim -eclim

    Note that you can supply the arguments to the command just as you would when
    using the search input via google's homepage, allowing you to utilize the
    full querying capabilities of google.

  #.  The second method is to issue the command with no arguments. The
      command will then query google with the word under the cursor.

.. _Clusty:

- **:Clusty** [word ...] -
  This command works just like the **:Google** command except it uses <a
  href="http://clusty.com">clusty.com</a> as the search engine.  For those of
  you who have not heard of clusty, it is an excellent alternative to google
  which groups results into clusters, allowing you to quickly filter search
  results down to the topic you are looking for.  It is a great place to go when
  google returns too many results that are difficult to sift through.

.. _Wikipedia:

- **:Wikipedia** [word ...] -
  Again this command behaves like the previous two, but instead looks up the
  desired word or phrase on <a href="http://en.wikipedia.com">wikipedia</a>, the
  great online encyclopedia.

.. _Dictionary:

- **:Dictionary** [word] -
  This command is used to look up a word on <a
  href="http://dictionary.reference.com">dictionary.reference.com</a>.  You can
  either supply the word to lookup as an argument to the command or it will
  otherwise use the word under the cursor.

.. _Thesaurus:

- **:Thesaurus** [word] -
  This command behaves the same as **:Dictionary** but looks up the word via <a
  href="http://thesaurus.reference.com">thesaurus.reference.com</a>.

While these commands can be quite useful, you may notice one thing is absent.
By default eclim does not provide any mappings that allow you to visually select
the text to lookup.  However the functions that the above commands rely upon
have been built to support this.  All you need to do is define mappings like the
following.

.. code-block:: vim

  vmap <leader>gn :call eclim#web#Google('', 0, 1)<cr>
  vmap <leader>gq :call eclim#web#Google('', 1, 1)<cr>

This example creates mappings to allow you to visually select some text and
submit that text as a query to google.  In this example <leader>gn will submit
the text unquoted, and <leader>gq will submit the text in quoted form.

See :help mapleader for more info on using <leader> for command mappings.

The above examples can be modified for clusty or wikipedia as well.  The
dictionary and thesaurus commands don't support this feature as they both
operate on single words.
