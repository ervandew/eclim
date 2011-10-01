.. Copyright (C) 2005 - 2011  Eric Van Dewoestine

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

.. _vim/html/complete:

Html Code Completion
====================

Html code completion uses the standard
:ref:`Vim code completion mechanism <vim/code_completion>` like so\:

::

  <ht<Ctrl-X><Ctrl-U>

  <html>
    <he<Ctrl-X><Ctrl-U>

  <html>
    <head>
      <lin<Ctrl-X><Ctrl-U>

  <html>
    <head>
      <link ty<Ctrl-X><Ctrl-U>

  <html>
    <head>
      <link type
  ...


.. warning::

  Html completion has been disabled on Windows when using the headless version
  of eclim because of a native call which blocks indefinitely.  Hopefully in
  the future this issue will be resolved or a work around found.
