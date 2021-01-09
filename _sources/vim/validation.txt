:orphan:

.. Copyright (C) 2005 - 2014  Eric Van Dewoestine

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

.. begin-disable

.. note::

  When enabled, syntastic_ is disabled so that eclim and syntastic don't step on
  each other. If you'd like to use syntastic over eclim for validation, then
  simply disable eclim's validation.

  If you'd like to disable eclim's source code validation for all languages,
  eclim provides a global variable for that as well:

  .. code-block:: vim

    let g:EclimFileTypeValidate = 0

.. _syntastic: https://github.com/scrooloose/syntastic

.. end-disable
