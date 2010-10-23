.. Copyright (C) 2005 - 2010  Eric Van Dewoestine

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

- **:LocateFile** [file_pattern] -
  Attempts to locate the supplied file pattern or if no argument is supplied,
  opens a temporary window where the text you type is turned into a pattern and
  search results are presented as you type.

  .. image:: ../../images/screenshots/locate.png

  While in this completion mode the following key bindings are available:
    - <esc> - close the search window without selecting a file
    - <tab> or <down> - cycle forward through the results
    - <shift><tab> or <up> - cycle backwards through the results
    - <enter> - open the selected file using the
      :ref:`default action <g:EclimLocateFileDefaultAction>`
    - <ctrl>e - open the selected file via :edit
    - <ctrl>s - open the selected file via :split
    - <ctrl>t - open the selected file via :tabnew
    - <ctrl>l - switch the locate scope
    - <ctrl>h - toggle the help buffer

  By default, the search string accepted by the completion mode is intended to
  be just portions of the file name you are looking for, which is then
  automatically expanded in an effort to help you find the file with the fewest
  keystrokes possible.

  The argument version of **:LocateFile** on the other hand, accepts a hybrid
  glob/regex pattern.  The glob portion allows you to use * and ** to match
  portions of a path or traverse multiple directories.  You can mix * and **
  with standard perl compatible regex operators to construct your search
  pattern.

  If you prefer the more explicit patterns supported by the argument version of
  **:LocateFile** over the default "fuzzy" pattern supported by the completion
  version of **:LocateFile**, then you can turn off the fuzzy matching support
  using the **g:EclimLocateFileFuzzy** variable described below.

  By default, all searching by both variants of this command is limited to the
  current project and any projects listed as dependencies, but you can widen
  the search scope to include all open projects by setting
  **g:EclimLocateFileScope** to 'workspace', which is the default scope when
  **:LocateFile** is executed outside of a project.

  In addition to the 'project' and 'workspace' scopes, **:LocateFile** also
  supports the following scopes:

    - buffers: search listed buffers
    - quickfix: search the quickfix results
    - vcsmodified: search files reported by your vcs as modified or untracked.

  .. note::

    For performance reasons, locating files in the 'project' and 'workspace'
    scopes depends on eclipse being aware of all your project files.  For the
    most part this is handled automatically as you create and edit files within
    vim.  However, actions you take outside of vim or eclipse (moving/removing
    files, updates from a version control system, etc.) will not be visible
    until you force a project refresh via :ref:`:ProjectRefresh`.

  **Configuration**

  Vim Settings

  .. _g\:EclimLocateFileDefaultAction:

  - **g:EclimLocateFileDefaultAction** (Default: 'split') -
    Determines the command used to open the file when hitting <enter> on an entry
    in the locate file results.

  .. _g\:EclimLocateFileScope:

  - **g:EclimLocateFileScope** (Default: 'project') -
    Determines the scope for which to search for files.

    - 'project': search only the current project and its dependencies.
    - 'workspace': search the entire workspace (all open projects).
    - 'buffers': search listed buffers
    - 'quickfix': search the quickfix results
    - 'vcsmodified': search files reported by your vcs as modified or
      untracked.

  .. _g\:EclimLocateFileFuzzy:

  - **g:EclimLocateFileFuzzy** (Default: 1) -
    Determines whether or not 'fuzzy' searching will be used on the no arugment
    version of :LocateFile.

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
  of the following shortcuts\:

  - **E** (shift-e) - Open the file with 'edit'.
  - **S** (shift-s) - Open the file with 'split'.
  - **T** (shift-t) - Open the file with 'tabnew'.
  - **D** (shift-d) - Deletes the buffer and removes it from the list.
  - **\?** - View the help buffer.

  In addition to the above mappings you can also use <return> to execute the
  configured default action on the buffer under the cursor.

  To configure the default action you can set the following variable\:

  **g:EclimBuffersDefaultAction** (defaults to 'split')

  By default entries will be sorted by path name, but you may change the
  sorting via these two variables\:

  **Configuration**

  Vim Settings

  .. _g\:EclimBuffersSort:

  - **g:EclimBuffersSort** (defaults to 'path')  Supports one
    of 'path', 'status' (active or hidden), 'bufnr'.

  .. _g\:EclimBuffersSortDirection:

  - **g:EclimBuffersSortDirection** (defaults to 'asc')
    Supports one of 'asc' or 'desc'.

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
  ability via the **g:EclimOnlyExclude** variable.

  **Configuration**

  Vim Settings

  .. _g\:EclimOnlyExclude:

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
