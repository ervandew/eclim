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

.. _vim/java/ant/complete:

Ant Code Completion
===================

Ant code completion uses the standard <a href="../../code_completion.html">Vim
code completion mechanism</a> like so\:

::

  <ja<Ctrl-X><Ctrl-U>
  <jar de<Ctrl-X><Ctrl-U>
  <jar destfile="${bas<Ctrl-X><Ctrl-U>
  <jar destfile="${basdir
  ...

Screenshot of completion in action\:

.. image:: ../../../images/screenshots/java/ant/completion.png

.. warning::

  If your ant file has a lot of imports, then the code completion may be slow as
  Eclipse parses all the imports when creating the ant model.  You will notice
  the same slow behavior when using Eclipse directly.
