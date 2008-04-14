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

.. _vim/xml/complete:

Xml Code Completion
===================

Xml code completion uses the standard <a href="../code_completion.html">Vim code
completion mechanism</a> like so\:

::

  <ser<Ctrl-X><Ctrl-U>

  <servlet>
    <ser<Ctrl-X><Ctrl-U>

  <servlet>
    <servlet-name>
  ...


.. note::

  Requires a valid dtd or xsd to determine possible completions.

In addition to the standard code completion, eclim also supports auto
completion of end tags. When you type '</' eclim will attempt to determine
which element you are closing and complete it for you.  If you wish to disable
this feature you can simply set g:EclimSgmlCompleteEndTag to 0.


Configuration
--------------

Vim Variables

.. _EclimSgmlCompleteEndTag:

- **g:EclimSgmlCompleteEndTag** -
  If set to 0, disables auto completion of end tags.

.. _EclimSgmlCompleteEndTagIgnore:

- **b:EclimSgmlCompleteEndTagIgnore** -
  Buffer local variable that can be set to a list of tags to ignore when
  searching for the start tag to complete.

  Example that can be added to an html ftplugin file\:

  .. code-block:: vim

    let b:EclimSgmlCompleteEndTagIgnore = ['br', 'input']
