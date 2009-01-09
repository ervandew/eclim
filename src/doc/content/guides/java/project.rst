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

.. _guides/java/project:

Creating A Java Project
=======================

The goal of this guide is to walk you through creating your first java project
via eclim.


Creating a project
------------------

The first step is to open a Vim window and create the project by executing\:

.. code-block:: vim

  :ProjectCreate /path/to/my_project -n java

The path supplied will be the path to the root of your project.  This path may
or may not exist.  If it does not exist it will be created for you.  If the
path does exist, it will be examined for any source directories or libraries
(jar files) that should be included in the project's classpath.  In either case
the end result will be the creation of a ``.project`` and ``.classpath`` file
in the supplied directory.  At this time, the ``.project`` file is nothing that
you need to worry about maintaining as it is purely for Eclipse.  The
``.classpath`` file on the other hand, is used to manage your project's
dependencies, including dependencies on other projects, other libraries, and
the location of your source directories.  For more on maintaining this file see
the :ref:`classpath docs <vim/java/classpath>`.

Once you've created your project you can use the **:ProjectList** command to
list the available projects and you should see your newly created one in the
list.

::

  my_project - open   - /path/to/my_project


The **:ProjectList** result is in the form of
``projectName - (open|closed) - /project/root/path``.  When you
create projects, the last path element will be used for the project
name.  If that element contains any spaces, these will be converted to
underscores.


Editing project settings
-------------------------

After creating a project, the next thing you'll probably want to do is edit
your project's settings.  To do this you can use the :ref:`:ProjectSettings`
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

  If you have only one project or many projects that share the same settings
  you can use the :ref:`:EclimSettings` command instead to edit the global
  settings.  These global settings will apply to any project that has not
  overridden them with values via **:ProjectSettings**.


Your project's classpath
------------------------

In the first section, we mentioned that when a project is created a .classpath
file is also created at the root directory of the project.  If you did not
create a project from an existing project directory (one containing source
files, etc.), then your next step will be to tell Eclipse where you plan to
store your source files.

For the purpose of this example we will assume that you will store your
source files at\:

::

  /path/to/my_project/src/java


So, given that location, you will need to open the file
/path/to/my_project/.classpath in Vim.

::

  vim /path/to/my_project/.classpath

To add the source directory simply execute the following

.. code-block:: vim

  :NewSrcEntry src/java

This will add the necessary entry to the end of your .classpath file.  The
contents of this file should now look something like this\:

.. code-block:: xml

  <?xml version="1.0" encoding="UTF-8"?>
  <classpath>
    <classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER"/>
    <classpathentry kind="output" path="bin"/>
    <classpathentry kind="src" path="src/java"/>
  </classpath>

Now that your source directory is setup, you can proceed to edit java files in
that directory and make use of the java functionality provided by eclim.
