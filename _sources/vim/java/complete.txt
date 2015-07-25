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

Java Code Completion
====================

Java code completion uses the standard
:doc:`Vim code completion mechanism </vim/code_completion>` like so\:

::

  System.o<Ctrl-X><Ctrl-U>
  System.out.pri<Ctrl-X><Ctrl-U>

Screenshot of completion in action\:

.. image:: ../../images/screenshots/java/completion.png


Configuration
-------------

:doc:`Vim Settings </vim/settings>`

.. _g\:EclimJavaCompleteCaseSensitive:

- **g:EclimJavaCompleteCaseSensitive** (Default: !&ignorecase) -
  When set to a value greater than 0, eclim will filter out completions that
  don't start with the same case base that you are attempting to complete (the
  base and the suggested completion must have the same case).
