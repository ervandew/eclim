.. Copyright (C) 2005 - 2013  Eric Van Dewoestine

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

.. _\:LocateFile:

Locate File
================

Eclim provides the **:LocateFile** command to allow you to quickly find and
open files or buffers.

- **:LocateFile** [file_pattern] -
  Attempts to locate the supplied file pattern or if no argument is supplied,
  opens a temporary window where the text you type is turned into a pattern and
  search results are presented as you type.

  .. image:: ../../images/screencasts/locate.gif

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

  .. note::

    For performance reasons, locating files in the 'project' and 'workspace'
    scopes depends on eclipse being aware of all your project files.  For the
    most part this is handled automatically as you create and edit files within
    vim.  However, actions you take outside of vim or eclipse (moving/removing
    files, updates from a version control system, etc.) will not be visible
    until you force a project refresh via :ref:`:ProjectRefresh`.

  .. note::

    If you would like to use :LocateFile even when eclimd is not running or for
    projects not known to eclim, one option would be to install `the silver
    searcher`_, then install my `ag plugin`_, and configure eclim to use the
    plugin as the fallback default:

    .. code-block:: vim

        let g:EclimLocateFileNonProjectScope = 'ag'

  **Configuration**

  :doc:`Vim Settings </vim/settings>`

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
    Determines whether or not 'fuzzy' searching will be used on the no argument
    version of :LocateFile.

  .. _g\:EclimLocateFileCaseInsensitive:

  - **g:EclimLocateFileCaseInsensitive** (Default: 'lower') -
    Determines when case insensitive searching is performed.

    - 'lower': when the search string is all lower case the search will be case
      insensitive, but if one or more capital letters are present, then the
      search will be case sensitive.
    - 'always': searching will always be case insensitive.
    - 'never': searching will never be case insensitive.

  .. note::

    **Search Filters**: eclim does not yet expose the ability to add filters
    should you want to ignore certain directories, but you can configure this
    ability from within Eclipse:

    :menuselection:`<right click on your project> --> Properties --> Resource
    --> Resource Filters`

.. _the silver searcher: https://github.com/ggreer/the_silver_searcher
.. _ag plugin: https://github.com/ervandew/ag
