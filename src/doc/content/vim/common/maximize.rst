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

.. _vim/common/maximize:

Window Maximize / Minimize
==========================

When working with multiple windows in a single vim instance things can get
cluttered and focusing on a subset of the windows can become difficult.  To help
solve this issue, eclim ships with a plugin which allows you to maximize one of
the vim windows, or minimize one or more windows.

.. _\:MaximizeWindow:

Maximize
--------

To maximize a window you can use the command **:MaximizeWindow** which will
toggle maximization of the current window.  When a window is maximized all other
windows will be minimized.  Also, when moving to another window, that window
will be maximized and the previous window minimized.

By default some specialty windows will not be minimized, namely the taglist
window, quick list, location list, eclim project tree, and mini buf explorer.
Window name patterns to exclude may be added or removed by explicitly setting
the variable :ref:`g:MaximizeExcludes`.

.. _\:MinimizeWindow:
.. _\:MinimizeRestore:

Minimize
--------

While **:MaximizeWindow** minimizes all but the current window,
**:MinimizeWindow** will minimize the current window. Or if a space separated
list of windows numbers is provided, then those windows will be minimized.

.. note::

  Minimize works in most cases, but there is at lease one case where it doesn't
  behave as desired.  If you find such a case, feel free to send me the steps to
  reproduce it.


To reset the current set of minimized windows, eclim also provides the command
**:MinimizeRestore**.


Configuration
-------------

Vim Variables

.. _g\:MaximizeExcludes:

- **g:MaximizeExcludes**
  (Default: '\(ProjectTree_*\|__Tag_List__\|-MiniBufExplorer-\|command-line\)')

  Regular expression used to match window buffer names to exclude from
  minimizing.

.. _g\:MaximizeSpecialtyWindowsRestore:

- **g:MaximizeSpecialtyWindowsRestore**
  Setting used to force specialty windows, other than the quickfix and
  location list windows, to a pre-defined width or height.
  This variable is a multi-dimensional array where each entry in the
  outer array is an array containing a variable for the buffer title of
  the window of the specialty window, and the command to set it to the
  desired width or height.

  Default:

  .. code-block:: vim

    let g:MaximizeSpecialtyWindowsRestore = [
        \ ['g:TagList_title', '"vertical <window>resize " . g:Tlist_WinWidth'],
        \ [
          \ 'g:EclimProjectTreeTitle',
          \ '"vertical <window>resize " . g:EclimProjectTreeWidth'
        \ ],
      \ ]

.. _g\:MaximizeMinWinHeight:

- **g:MaximizeMinWinHeight** (Default: 0)

  Height that vertically minimized windows will be set to.

.. _g\:MaximizeMinWinWidth:

- **g:MaximizeMinWinWidth** (Default: 0)

  Height that horizontally minimized windows will be set to.

.. _g\:MaximizeQuickfixHeight:

- **g:MaximizeQuickfixHeight** (Default: 10)

  Height that the quickfix and location list windows will be kept at.

.. _g\:MaximizeStatusLineEnabled:

- **g:MaximizeStatusLineEnabled** (Default: 0)

  When set to 1, enables the maximize status line which adds the buffer
  and window number to the status line (just after line and column
  numbers).  Allows you to quickly determine a given window's number
  when using **:MinimizeWindow**.
