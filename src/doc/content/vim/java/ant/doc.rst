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

.. _vim/java/ant/doc:

Ant Documentation Lookup
========================

.. _\:AntDoc:

When editing an ant build file eclim defines a command named **:AntDoc** which
will attempt to lookup and open in your
:ref:`configured browser <g:eclimbrowser>` the documentation for the element
under the cursor or, if supplied, the element passed to it.

This command will only lookup element names, not attribute names or values.

By default this plugin is configured to find all the standard ant tasks, types,
etc, as well as those defined by the antcontrib_ project.

.. _\:AntUserDoc:

If you have other tasks that you wish to add to this plugin, you can do so by
defining the global variable **g:AntUserDocs**.  The value of this variable is
expected to be a map of element names to the url where the documentation for
that element can be found.  The url also supports a substitution variable,
<element> which will be substituted with the lower case version of the element
name.

The following is an example which adds the tasks from the apache cactus project.

.. code-block:: vim

  let s:cactus =
    \ 'http://jakarta.apache.org/cactus/integration/ant/task_<element>.html'
  let g:AntUserDocs = {
      \  'cactifywar'     : s:cactus,
      \  'cactifyear'     : s:cactus,
      \  'cactus'         : s:cactus,
      \  'runservertests' : s:cactus,
      \  'webxmlmerge'    : s:cactus,
    \ }


.. _antcontrib: http://ant-contrib.sourceforge.net
