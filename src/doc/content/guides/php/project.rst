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

.. _guides/php/project:

Creating A Php Project
======================


The goal of this guide is to walk you through creating your first php project
via eclim.


Creating a project
------------------

The first step is to open a Vim window and create the project by executing\:

.. code-block:: vim

  :ProjectCreate /path/to/my_project -n php


The path supplied will be the path to the root of your project.  This path may
or may not exist.  If it does not exist it will be created for you. When the
project is created two files will be added to the root directory of your
project, ``.project`` and ``.buildpath`` At this time, the ``.project`` file is
nothing that you need to worry about maintaining as it is purely for Eclipse.
The ``.buildpath`` file on the other hand, is used to manage your project's
dependencies, including dependencies on other projects and other libraries.
For more on maintaining this file see the
:ref:`build path docs <vim/php/buildpath>`.

Once you've created your project you can use the **:ProjectList** command to
list the available projects and you should see your newly created one in the
list.

::

  my_project - open   - /path/to/my_project


The **:ProjectList** result is in the form of ``projectName - (open|closed) -
/project/root/path``.  When you create projects, the last path element will be
used for the project name.  If that element contains any spaces, these will be
converted to underscores.

Now that you have a php project created, you can now leverage the various
:ref:`php features <vim/php/index>` that eclim provides.
