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


In addition to the standard code completion, eclim also supports auto completion
of end tags. When you type '</' eclim will attempt to determine which element
you are closing and complete it for you.  If you wish to disable this feature
you can simply set g:EclimSgmlCompleteEndTag to 0.

.. warning::

  Html completion has been disabled on Windows when using the headless version
  of eclim because of a native call which blocks indefinitely.  Hopefully in
  the future this issue will be resolved or a work around found.


Configuration
-------------

Vim Variables

.. _g\:EclimSgmlCompleteEndTag:

- **g:EclimSgmlCompleteEndTag** -
  If set to 0, disables auto completion of end tags.

.. _g\:EclimSgmlCompleteEndTagIgnore:

- **b:EclimSgmlCompleteEndTagIgnore** -
  Buffer local variable that can be set to a list of tags to ignore when
  searching for the start tag to complete.

  Example that can be added to an html ftplugin file:

  .. code-block:: vim

    let b:EclimSgmlCompleteEndTagIgnore = ['br', 'input']
