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

.. _vim/common/archive:

Archive Viewer
==============

Eclim includes a plugin which allows you to view the contents of archive files
(jar, tar, tbz2, tgz, zip, and more).

.. code-block:: sh

  $ vim myarchive.tar.gz

Unlike the tar and zip plugins that ship with vim, the eclim version gives you
the option to view and navigate the archive in a tree or list interface, and
supports nested archives.

When viewing the archive you can switch layouts by using **:AsList** to switch
from the tree layout to the list, and **:AsTree** to switch back.

There are also various mappings depending on the view\:

- **Tree**

  - **<enter>** - View the file under the cursor in a new split window, or
    in the case of a directory, expand / collapse it.
  - **o** - For files, this has the same behavior as **&lt;Enter&gt;**. For
    directories, it will fold / unfold open directories preserving the state of
    all the sub directories.
  - **p** - Jumps to the nearest parent directory.
  - **P** - Jumps to the furthest child in the currect branch of directories.
  - **i** - Prints the size and date for the file under the cursor.
- **List**
  - **<enter>** - View the file under the cursor in a new split window.


Configuration
--------------

Vim Variables

.. _g\:EclimArchiveViewerEnabled:

- **g:EclimArchiveViewerEnabled** (Default: 1)

  When greater than 0, enables the eclim archive viewer (disabling the default
  vim tar and zip plugins).

.. _g\:EclimArchiveLayout:

- **g:EclimArchiveLayout** (Default: none)

  When non existant or equal to 'tree', displays the archive as a tree.  When
  set to 'list', display the archive as a flat list.
