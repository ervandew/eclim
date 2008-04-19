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

.. _vim/common/util:

Utility Commands
================

The following is a list of utility commands provided by eclim.  These are
general purpose commands that are useful in and outside the scope of eclim.

.. _LocateFileEdit:

- **:LocateFileEdit** [file] -
  Locates a file via the following steps\:

  #.  If current file is in a project, locate the file relative to that project.
  #.  If no results from step #1, then attempt to locate relative to the current
      file.
  #.  If no results from step #2, then attempt to locate relative to the other
      projects.

  If no file argument is supplied to the **:LocateFileEdit** command then it
  will attempt to extract a file name from the cursor position of the current
  file.  When a file is located, an :edit is invoked to open it.

.. _LocateFileSplit:

- **:LocateFileSplit** [file] -
  Like **:LocateFileEdit** except open the file with :split.

.. _LocateFileTab:

- **:LocateFileTab** [file] -
  Like **:LocateFileEdit** except open the file with :newtab.

.. _Split:

- **:Split** file [file ...] -
  Behaves like the 'split' command, but allows multiple files to be supplied.
  Supports '*' and '**' wildcards.

.. _SplitRelative:

- **:SplitRelative** file [file ...] -
  Like **:Split** this command provides splitting of multiple files, but this
  command splits files relative to the file in the current buffer. Supports '*'
  and '**' wildcards.

.. _Tabnew:

- **:Tabnew** file [file ...] -
  Behaves like **:Split**, but issues a :tabnew on each file.  Supports '*' and
  '**' wildcards.

.. _TabnewRelative:

- **:TabnewRelative** file [file ...] -
  Behaves like **:SplitRelative**, but issues a :tabnew on each file. Supports
  '*' and '**' wildcards.

.. _EditRelative:

- **:EditRelative** file -
  Like **:SplitRelative** except issues an 'edit' and only supports one file at
  a time.

.. _ReadRelative:

- **:ReadRelative** file -
  Like **:SplitRelative** except issues a 'read' and only supports one file at a
  time.

.. _ArgsRelative:

- **:ArgsRelative** file_pattern [file_pattern ...] -
  Like **:SplitRelative** except executes 'args'.

.. _ArgAddRelative:

- **:ArgAddRelative** file_pattern [file_pattern ...] -
  Like **:SplitRelative** except executes 'argadd'.

.. _VimgrepRelative:

- **:VimgrepRelative** /regex/ file_pattern [ file_pattern ...] -
  Executes :vimgrep relative to the current file.

.. _VimgrepAddRelative:

- **:VimgrepAddRelative** /regex/ file_pattern [ file_pattern ...] -
  Executes :vimgrepadd relative to the current file.

.. _LvimgrepRelative:

- **:LvimgrepRelative** /regex/ file_pattern [ file_pattern ...] -
  Executes :lvimgrep relative to the current file.

.. _LvimgrepAddRelative:

- **:LvimgrepAddRelative** /regex/ file_pattern [ file_pattern ...] -
  Executes :lvimgrepadd relative to the current file.

.. _CdRelative:

- **:CdRelative** dir -
  Executes :cd relative to the current file.

.. _LcdRelative:

- **:LcdRelative** dir -
  Executes :lcd relative to the current file.

.. _DiffLastSaved:

- **:DiffLastSaved** -
  Performs a diffsplit with the last saved version of the currently modifed
  file.

.. _SwapWords:

- **:SwapWords** -
  Swaps two words (with cursor placed on the first word).  Supports swapping
  around non-word characters like commas, periods, etc.

.. _Sign:

- **:Sign** -
  Toggles adding or removing a vim sign on the current line.

.. _Signs:

- **:Signs** -
  Opens a new window containing a list of signs for the current buffer.  Hitting
  <enter> on one of the signs in the list will take you to that sign in the
  corresponding buffer.

.. _SignClearUser:

- **:SignClearUser** -
  Removes all vim signs added via :Sign.

.. _SignClearAll:

- **:SignClearAll** -
  Removes all vim signs.

.. _QuickFixClear:

- **:QuickFixClear** -
  Removes all entries from the quick fix window.

.. _LocationListClear:

- **:LocationListClear** -
  Removes all entries from the location list window.

.. _Buffers:

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

.. _Only:

- **:Only** -
  Alternative for vim's :only command.  The purpose of this command and the
  original vim version is to close all but the current window.  Unfortunately
  there is no way to tell the vim version to exclude some windows you may wish
  to keep open (taglist, quickfix, etc.).  The eclim version provides that
  ability via the **g:EclimOnlyExclude** variable.

  **g:EclimOnlyExclude** (defaults to
  '\(ProjectTree_*\|__Tag_List__\|-MiniBufExplorer-\|command-line\)')

.. _OtherWorkingCopyDiff:

- **:OtherWorkingCopyDiff** <project> -
  Diffs the current file against the same file in another project (one which
  has the same project relative path). This is most useful if you find yourself
  doing branch development and want to view the differences of the current file
  against one of the other branches.  Supports command line tab completion of
  project names which contain a file with the same relative path as the current
  file: ``:OtherWorkingCopyDiff <tab>``.

.. _OtherWorkingCopyEdit:

- **:OtherWorkingCopyEdit** <project> -
  Like **:OtherWorkingCopyDiff**, except open the file in the current window.

.. _OtherWorkingCopySplit:

- **:OtherWorkingCopySplit** <project> -
  Like **:OtherWorkingCopyDiff**, except open the file in a new window.

.. _OtherWorkingCopyTabopen:

- **:OtherWorkingCopyTabopen** <project> -
  Like **:OtherWorkingCopyDiff**, except open the file in a new tab.
