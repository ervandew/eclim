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

.. _vim/common/util:

Utility Commands
================

The following is a list of utility commands provided by eclim.  These are
general purpose commands that are useful in and outside the scope of eclim.

.. _\:LocateFile:

- **:LocateFile** [file] -
  Attempts to locate the supplied file pattern or if no argument is supplied,
  opens a temporary window where the text you type is turned into a pattern and
  search results are presented as you type.

  .. image:: ../../images/screenshots/locate.png

  While in this completion mode the following key bindings are available:
    - <tab> - cycle forward through the results.
    - <shift><tab> - cycle backwards through the results.
    - <enter> - open the selected file using the
      :ref:`default action <g:EclimLocatFileDefaultAction>`.
    - <ctrl>e - open the selected file via :edit
    - <ctrl>s - open the selected file via :split
    - <ctrl>t - open the selected file via :tabnew
    - <esc> - close the search window without selecting a file.

  All searching by this command is limited to the current project and any
  projects listed as dependencies.

  .. note::

    For performance reasons, this functionality depends on eclipse being aware
    of all your project files.  For the most part this is handled automatically
    as you create and edit files within vim.  However, actions you take outside
    of vim or eclipse (moving/removing files, updates from a version control
    system, etc.) will not be visible until you force a project refresh via
    :ref:`:ProjectRefresh`.

.. _g\:EclimLocatFileDefaultAction:

  **g:EclimLocatFileDefaultAction** (defaults to 'split') -
  Determines the command used to open the file when hitting <enter> on an entry
  in the locate file results.

.. _\:Split:

- **:Split** file [file ...] -
  Behaves like the 'split' command, but allows multiple files to be supplied.
  Supports '*' and '**' wildcards.

.. _\:SplitRelative:

- **:SplitRelative** file [file ...] -
  Like **:Split** this command provides splitting of multiple files, but this
  command splits files relative to the file in the current buffer. Supports '*'
  and '**' wildcards.

.. _\:Tabnew:

- **:Tabnew** file [file ...] -
  Behaves like **:Split**, but issues a :tabnew on each file.  Supports '*' and
  '**' wildcards.

.. _\:TabnewRelative:

- **:TabnewRelative** file [file ...] -
  Behaves like **:SplitRelative**, but issues a :tabnew on each file. Supports
  '*' and '**' wildcards.

.. _\:EditRelative:

- **:EditRelative** file -
  Like **:SplitRelative** except issues an 'edit' and only supports one file at
  a time.

.. _\:ReadRelative:

- **:ReadRelative** file -
  Like **:SplitRelative** except issues a 'read' and only supports one file at a
  time.

.. _\:ArgsRelative:

- **:ArgsRelative** file_pattern [file_pattern ...] -
  Like **:SplitRelative** except executes 'args'.

.. _\:ArgAddRelative:

- **:ArgAddRelative** file_pattern [file_pattern ...] -
  Like **:SplitRelative** except executes 'argadd'.

.. _\:VimgrepRelative:

- **:VimgrepRelative** /regex/ file_pattern [ file_pattern ...] -
  Executes :vimgrep relative to the current file.

.. _\:VimgrepAddRelative:

- **:VimgrepAddRelative** /regex/ file_pattern [ file_pattern ...] -
  Executes :vimgrepadd relative to the current file.

.. _\:LvimgrepRelative:

- **:LvimgrepRelative** /regex/ file_pattern [ file_pattern ...] -
  Executes :lvimgrep relative to the current file.

.. _\:LvimgrepAddRelative:

- **:LvimgrepAddRelative** /regex/ file_pattern [ file_pattern ...] -
  Executes :lvimgrepadd relative to the current file.

.. _\:CdRelative:

- **:CdRelative** dir -
  Executes :cd relative to the current file.

.. _\:LcdRelative:

- **:LcdRelative** dir -
  Executes :lcd relative to the current file.

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
  of the following shortcuts\:

  - **E** (shift-e) - Open the file with 'edit'.
  - **S** (shift-s) - Open the file with 'split'.
  - **T** (shift-t) - Open the file with 'tabnew'.
  - **D** (shift-d) - Deletes the buffer and removes it from the list.

  In addition to the above mappings you can also use <return> to execute the
  configured default action on the buffer under the cursor.

  To configure the default action you can set the following variable\:

  **g:EclimBuffersDefaultAction** (defaults to 'split')

  By default entries will be sorted by path name, but you may change the
  sorting via these two variables\:

  - **g:EclimBuffersSort** (defaults to 'path')  Supports one
    of 'path', 'status' (active or hidden), 'bufnr'.
  - **g:EclimBuffersSortDirection** (defaults to 'asc')
    Supports one of 'asc' or 'desc'.

.. _\:Only:

- **:Only** -
  Alternative for vim's :only command.  The purpose of this command and the
  original vim version is to close all but the current window.  Unfortunately
  there is no way to tell the vim version to exclude some windows you may wish
  to keep open (taglist, quickfix, etc.).  The eclim version provides that
  ability via the **g:EclimOnlyExclude** variable.

  **g:EclimOnlyExclude** (defaults to
  '\(ProjectTree_*\|__Tag_List__\|-MiniBufExplorer-\|command-line\)')

.. _\:OtherWorkingCopyDiff:

- **:OtherWorkingCopyDiff** <project> -
  Diffs the current file against the same file in another project (one which
  has the same project relative path). This is most useful if you find yourself
  doing branch development and want to view the differences of the current file
  against one of the other branches.  Supports command line tab completion of
  project names which contain a file with the same relative path as the current
  file: ``:OtherWorkingCopyDiff <tab>``.

.. _\:OtherWorkingCopyEdit:

- **:OtherWorkingCopyEdit** <project> -
  Like **:OtherWorkingCopyDiff**, except open the file in the current window.

.. _\:OtherWorkingCopySplit:

- **:OtherWorkingCopySplit** <project> -
  Like **:OtherWorkingCopyDiff**, except open the file in a new window.

.. _\:OtherWorkingCopyTabopen:

- **:OtherWorkingCopyTabopen** <project> -
  Like **:OtherWorkingCopyDiff**, except open the file in a new tab.
