.. Copyright (C) 2005 - 2012  Eric Van Dewoestine

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

.. _guides/uninstall:

Uninstalling
============

To uninstall eclim you can use any eclim distribution jar whose version is
1.7.5 or greater by running it with the 'uninstaller' argument like so:

  .. code-block:: bash

    $ java -jar eclim_<version>.jar uninstaller

That will open a graphical wizard much like the install wizard which will ask
you again for the location of your vimfiles and eclipse home where you've
installed eclim and will then remove the eclim installation accordingly.

.. _uninstall-automated:

Unattended (automated) uninstall
--------------------------------

Like the installer, the uninstaller also supports an unattended uninstall. You
just need to supply your vim files and eclipse paths as system properties:

.. code-block:: bash

  $ java \
    -Dvim.files=$HOME/.vim \
    -Declipse.home=/opt/eclipse \
    -jar eclim_<version>.jar uninstall

**Required Properties:**

* **eclipse.home** - The absolute path to your eclipse installation.
* **vim.files** (or **vim.skip=true**) - The absolute path to your vim files
  directory. Or if you never installed the vim files (emacs-eclim users for
  example) you can supply `-Dvim.skip=true` instead.
