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

.. _guides/python/project:

Creating A Python Project
=========================

The goal of this guide is to walk you through creating your first python project
via eclim.


Creating a project
------------------

The first step is to open a Vim window and create the project by executing\:

.. code-block:: vim

  :ProjectCreate /path/to/my_project -n none

The path supplied will be the path to the root of your project.  This path may
or may not exist.  If it does not exist it will be created for you.

Once you've created your project you can use the
**:ProjectList** command to list the available projects and
you should see your newly created one in the list.

::

  my_project - open   - /path/to/my_project

The **:ProjectList** result is in the form of
``projectName - (open|closed) - /project/root/path``.  When you create projects,
the last path element will be used for the project name.  If that element
contains any spaces, these will be converted to underscores.


Editing project settings
------------------------

After creating a project, the next thing you'll probably want to do is edit your
project's settings.  To do this you can use the
<a href="../../vim/common/project.html#ProjectSettings">
**:ProjectSettings**
</a>
command.  If your current Vim window's working directory is at or under the
project's root directory then you can execute the **:ProjectSettings** with no
arguments, otherwise you will need to supply the project name.

.. code-block:: vim

  :ProjectSettings projectName

After your first time editing your project's settings, a .settings directory
will be created in the project's root directory.  In there are the project's
preferences files.  You should avoid editing these files directly and stick to
using **:ProjectSettings** to update them.

.. note::

  If you have only one project or many projects that share the same settings you
  can use the
  <a href="../../vim/index.html#EclimSettings">
  **:EclimSettings**
  </a>
  command instead to edit the global settings.  These global settings will apply
  to any project that has not overridden them with values via
  **:ProjectSettings**.
