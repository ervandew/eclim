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

.. _guides/uninstall:

Uninstalling
============

Uninstalling eclim consists of simply removing the eclipse and vim plugins installed\:

- <eclipse_home>/plugins/org.eclim*

  Remove all directories under plugins that start with ``org.eclim``.

  - if you are using a package manager installed version of eclipse, then the
    plugins directory where eclim is installed may be under
    ``~/.eclipse/org.eclipse.platform_<version>/configuration/eclipse/plugins``

  - On Windows systems you can also remove all the eclim* files and ng.exe from
    your eclipse home directory.
  - On Linux, BSD, OSX, etc., you can remove the symlinks created to eclim and
    eclimd found in your eclipse home directory.

- <vimfiles>/eclim

  Remove the eclim directory under your vimfiles dir.

  - on unix based systems, this should be ``~/.vim/eclim``
  - on Windows systems, this should be ``%HOME%/vimfiles``

- <vimfiles>/plugin/eclim.vim

  Lastly remove eclim.vim from your vim plugin directory.
