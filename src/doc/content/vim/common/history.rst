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

.. _vim/common/history:


Local History
=============

When editing files, vim provides great undo support.  However, the undo
information is lost when you close a file or utilize one of the few eclim
features that require a reload of the current file.

Eclipse provides a nice feature which can help combat both of these
deficiencies.  It includes support for a local history, which is basically a
simplistic version control system that is updated every time you save a file.
Using this local history, you can view diffs against previously saved versions
of your file or revert to one of those revisions.

By default, as long as eclimd is running, any writes you make to a file inside
of a project will automatically be saved as a local history revision.

.. _\:History:

**:History** - Opens a temporary buffer showing the local history for the
current file.  In this buffer you can perform the following actions using the
specified key bindings:

- v - view the contents of the revision under the cursor.
- d - diff the revision under the cursor against the current contents.
- r - revert the current file to the revision under the cursor.
- c - clear the local history for the file.

.. _\:HistoryClear:

**:HistoryClear[!]** - Clears the local history for the current file.  When the
bang (!) is supplied, you are not prompted before clearing the history.

.. _\:HistoryDiffNext:

**:HistoryDiffNext** - While the history buffer is open, this command allows
you to diff the current file against the next entry in the history stack.

.. _\:HistoryDiffPrev:

**:HistoryDiffPrev** - Just like **:HistoryDiffNext** but diffs against the
previous entry in the stack.


Configuration
--------------

Eclipse Settings

- When writing to the local history, eclim simply proxies the request to
  eclipse, so all eclipse settings are honored.  To modify these settings you
  currently have to do so inside of the eclipse gui.  First shut down eclimd if
  you are running a headless version, then open the eclipse gui and navigate
  to:

  ::

    Window > Preferences > General > Workspace > Local History

  And there you can edit your settings as necessary.

Vim Settings

.. _g\:EclimHistoryDiffOrientation:

- **g:EclimHistoryDiffOrientation (Default: 'vertical')** -
  When initiating diffs, this setting controls whether the diff window is
  opened as a horizontal split or vertical.  Supported values include
  'horizontal' and 'vertical'.
