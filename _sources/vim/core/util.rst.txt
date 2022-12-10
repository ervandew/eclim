.. Copyright (C) 2005 - 2019  Eric Van Dewoestine

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

Utility Commands
================

The following is a list of utility commands provided by eclim.  These are
general purpose commands that are useful in and outside the scope of eclim.

.. _\:Tcd:

- **:Tcd** dir -
  Mimics vim's :lcd command but sets the current working directory local to the
  current tab instead of just the current window.

.. _\:DiffLastSaved:

- **:DiffLastSaved** -
  Performs a diffsplit with the last saved version of the currently modifed
  file.

.. _\:SwapWords:

- **:SwapWords** -
  Swaps two words (with cursor placed on the first word).  Supports swapping
  around non-word characters like commas, periods, etc.

.. _\:Sign:

- **:Sign** -
  Toggles adding or removing a vim sign on the current line.

.. _\:Signs:

- **:Signs** -
  Opens a new window containing a list of signs for the current buffer.  Hitting
  <enter> on one of the signs in the list will take you to that sign in the
  corresponding buffer.

.. _\:SignClearUser:

- **:SignClearUser** -
  Removes all vim signs added via :Sign.

.. _\:SignClearAll:

- **:SignClearAll** -
  Removes all vim signs.

.. _\:QuickFixClear:

- **:QuickFixClear** -
  Removes all entries from the quick fix window.

.. _\:LocationListClear:

- **:LocationListClear** -
  Removes all entries from the location list window.

.. _\:Buffers:

- **:Buffers** -
  Opens a temporary window with a list of all the currently listed buffers in
  vim (like :buffers).  From this list you can open any of the files using one
  of the following shortcuts:

  - **E** (shift-e) - Open the file with 'edit'.
  - **S** (shift-s) - Open the file with 'split'.
  - **V** (shift-v) - Open the file with 'vsplit'.
  - **T** (shift-t) - Open the file with 'tabnew'.
  - **D** (shift-d) - Deletes the buffer and removes it from the list.
  - **O** (shift-o) - Delete all hidden buffers and remove them from the list
    (analogous to :only).
  - **\?** - View the help buffer.

  In addition to the above mappings you can also use <return> to execute the
  configured default action on the buffer under the cursor.

  To configure the default action you can set the following variable:

  **g:EclimBuffersDefaultAction** (defaults to 'split')

  Note that eclim will track the tab where buffers are opened and closed
  allowing **:Buffers** to filter the list to those whose primary tab is the
  current tab, or for buffers not open, show those that were last open on the
  current tab. If however you would like to still see all listed buffers, you
  can append '!' to the command: ``:Buffers!``

  By default entries will be sorted by path name, but you may change the
  sorting via these two variables:

  **Configuration**

  :doc:`Vim Settings </vim/settings>`

  .. _g\:EclimBuffersSort:

  - **g:EclimBuffersSort** (defaults to 'path')  Supports one
    of 'path', 'status' (active or hidden), 'bufnr'.

  .. _g\:EclimBuffersSortDirection:

  - **g:EclimBuffersSortDirection** (defaults to 'asc')
    Supports one of 'asc' or 'desc'.

  .. _g\:EclimBuffersTabTracking:

  - **g:EclimBuffersTabTracking** (defaults to 1)
    When set to a non-0 value, eclim will keep track of which tabs buffers are
    opened on allowing the ``:Buffers`` command to filter the list of buffers to
    those accessed by the current tab. As noted above, you can still view all
    buffers with this option enabled by using ``:Buffers!`` ('!' appended).

  .. _g\:EclimBuffersDeleteOnTabClose:

  - **g:EclimBuffersDeleteOnTabClose** (defaults to 0)
    When set to a non-0 value and ``g:EclimBuffersTabTracking`` is enabled, then
    eclim will delete any non-active buffers associated with the current tab
    when that tab is closed. The can be useful if you use a tab per project
    workflow and would like to close a project's tab and have any buffers for
    that project deleted as well.

.. _\:BuffersToggle:

- **:BuffersToggle** -
  A convenience command which opens the buffers window if not open, otherwise
  closes it.  Useful for creating a key mapping to quickly open/close the
  buffers window.

.. _\:Only:

- **:Only** -
  Alternative for vim's :only command.  The purpose of this command and the
  original vim version is to close all but the current window.  Unfortunately
  there is no way to tell the vim version to exclude some windows you may wish
  to keep open (taglist, quickfix, etc.).  The eclim version provides that
  ability via the **g:EclimOnlyExclude** and **g:EclimOnlyExcludeFixed**
  variables.

  **Configuration**

  :doc:`Vim Settings </vim/settings>`

  .. _g\:EclimOnlyExclude:

  - **g:EclimOnlyExclude** (defaults to '^NONE$') -
    Regex used to match buffer names for windows that should not be closed when
    issuing the :Only command.

  .. _g\:EclimOnlyExcludeFixed:

  - **g:EclimOnlyExcludeFixed** (defaults to 1)
    When non-0 all fixed windows (ones which have 'winfixwidth' or
    'winfixheight' set) will be preserved when issuing the :Only command.

.. _\:OpenUrl:

- **:OpenUrl** [url] -
  Opens a url in your web browser, or optionally in Vim via netrw (:help netrw).

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

  **Configuration**

  :doc:`Vim Settings </vim/settings>`

  .. _g\:EclimOpenUrlInVimPatterns:

  - **g:EclimOpenUrlInVimPatterns** (Default: []) -
    Defines a list of url patterns to open in Vim via netrw.

  .. _g\:EclimOpenUrlInVimAction:

  - **g:EclimOpenUrlInVimAction** (Default: 'split') -
    Defines the command used to open files matched by g:EclimOpenUrlInVimPatterns.

.. _eclim#web#SearchEngine:

- **eclim#web#SearchEngine**
  Helper function which provides the functionality needed to create search
  engine commands or mappings.

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

     Note that you can supply the arguments to the command just as you would
     when using the search input via google's homepage, allowing you to utilize
     the full querying capabilities of google.

  #. The second method is to issue the command with no arguments. The
     command will then query google with the word under the cursor.

  #. The last method is to visually select the text you want to search for and
     then execute the command.

.. _eclim#web#WordLookup:

- **eclim#web#WordLookup**
  Helper function which can be used to create commands or mappings which lookup
  a word using an online reference like a dictionary or thesaurus.

  .. code-block:: vim

    command -nargs=? Dictionary call eclim#web#WordLookup(
      \ 'http://dictionary.reference.com/search?q=<query>', '<args>')

  Adding the above command to your vimrc or similar provides you with a new
  :Dictionary command which can be used to look up a word on
  dictionary.reference.com_.  You can either supply the word to lookup as an
  argument to the command or it will otherwise use the word under the cursor.

.. _google.com: http://google.com
.. _dictionary.reference.com: http://dictionary.reference.com
