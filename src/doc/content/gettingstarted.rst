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

.. _gettingstarted:

Getting Started
===============

The first step to getting started with eclim is to install it.  If you haven't
already done so, please see the :ref:`install guide <guides/install>` for
instruction on how to install eclim.

If you've already installed eclim, then the next step is to create your first
project after which you can then start writing code and familiarizing yourself
with eclim's features.

First make sure eclimd is running (see the :ref:`eclimd docs <eclimd>` if you
are unsure how to start eclimd).

Creating your first project
---------------------------

Once you've got eclimd running open an instance of vim and create your project
like so (maven users, please see the :ref:`maven docs <gettingstarted-maven>`):

::

  :ProjectCreate /path/to/my_project -n java

This example creates a project with a java nature (-n java), but the same
method can be used to create a project for other languages by simply changing
the nature accordingly:

::

  :ProjectCreate /path/to/my_c_project -n c
  :ProjectCreate /path/to/my_cpp_project -n c++
  :ProjectCreate /path/to/my_java_project -n java
  :ProjectCreate /path/to/my_php_project -n php
  :ProjectCreate /path/to/my_python_project -n none
  :ProjectCreate /path/to/my_ruby_project -n ruby

The only odd ball in the bunch is the creation of the python project which
currently uses the 'none' nature.

The path supplied to the :ref:`:ProjectCreate <:ProjectCreate>` command will be
the path to the root of your project.  This path may or may not exist.  If it
does not exist it will be created for you.  After you've created your project,
there will be a ``.project`` file added to your project's root along with
another file where references to your project's source directories and any
third party libraries your project uses reside.  The name of this file will
vary depending on your project's nature, but in all cases eclim will provide
you with commands to manage this file:

* java - :ref:`.classpath file <vim/java/classpath>`
* php, ruby - :ref:`.buildpath file <vim/dltk/buildpath>`
* c, c++ - ``.cproject``, managed via the :ref:`:CProjectConfigs
  <:CProjectConfigs>` command
* python - ``.ropeproject`` (see the :ref:`rope docs <vim/python/rope>`)

Once you've created your project you can use the **:ProjectList** command to
list the available projects and you should see your newly created one in the
list.

::

  my_project - open   - /path/to/my_project

The **:ProjectList** result is in the form of ``projectName - (open|closed) -
/project/root/path``.  When you create projects, the last path element will be
used for the project name.  If that element contains any spaces, these will be
converted to underscores.

Adding project source directories
---------------------------------

Before you can start writing code, you will first need to create and register
your project's source directories.  If you created your project from an
existing code base, then this step may have been perform automatically for you,
but you should validate the settings to be sure.

We will use a java project in this example but the steps for other languages
are very similar.  Please see the relevant docs for your language for more
details:

* :ref:`java <vim/java/classpath>`
* :ref:`php and ruby <vim/dltk/buildpath>`
* :ref:`c and c++ <:CProjectConfigs>`
* :ref:`python <vim/python/rope>`

For the purpose of this example we will assume that you will store your source
files at\:

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
that directory and make use of the :ref:`java functionality <vim/java/index>`
provided by eclim.

Editing your project's settings
-------------------------------

Several of eclim's features are configurable via project settings which you can
modify using the :ref:`:ProjectSettings` command.  If your current Vim window's
working directory is at or under the project's root directory then you can
execute the **:ProjectSettings** with no arguments, otherwise you will need to
supply the project name.

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


.. _gettingstarted-maven:

Maven Users
-----------

Creating your first project with maven can be accomplished using the same
method as any other java project, or you can utilize some of maven's built in
features to get your project started.

1. Run maven's generate archetype to create the project directory and samples:

  .. code-block:: bash

    $ mvn archetype:generate

2. Open an instance of vim and set the necessary eclipse classpath variable to
   point to your maven repos.

  .. code-block:: bash

    $ vim
    :MvnRepos

3. Run the following command at the root directory of your project to generate
   the necessary eclipse files:

  .. code-block:: bash

    $ mvn eclipse:eclipse

4. Open an instance of vim and import your project:

  .. code-block:: bash

    $ vim
    :ProjectImport /path/to/new/project
