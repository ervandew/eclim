.. Copyright (C) 2005 - 2020  Eric Van Dewoestine

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

Getting Started
===============

Once you've :doc:`installed </install>` eclim, the next step is to create
your first project after which you can then start writing code and
familiarizing yourself with eclim's features.

First make sure eclimd is running (see the :doc:`eclimd docs </eclimd>` if you
are unsure how to start eclimd).

.. _gettingstarted-create:

Creating your first project
---------------------------

Once you've got eclimd running, open an instance of vim and create your project
like so:

::

  :ProjectCreate /path/to/my_project -n java

This example creates a project with a java nature (-n java), but the same
method can be used to create a project for other languages by simply changing
the nature accordingly:

::

  :ProjectCreate /path/to/my_java_project -n java
  :ProjectCreate /path/to/my_python_project -n python

The path supplied to the :ref:`:ProjectCreate <:ProjectCreate>` command will be
the path to the root of your project.  This path may or may not exist.  If it
does not exist it will be created for you.  After you've created your project,
there will be a ``.project`` file added to your project's root along with
another file where references to your project's source directories and any
third party libraries your project uses reside.  The name of this file will
vary depending on your project's nature, but in all cases eclim will provide
you with commands to manage this file:

* java - :doc:`.classpath file </vim/java/classpath>`
* python - :doc:`.pydevproject file </vim/python/path>`

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

* :doc:`java </vim/java/classpath>`
* :doc:`python </vim/python/path>`

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
that directory and make use of the :doc:`java functionality </vim/java/index>`
provided by eclim.


.. _gettingstarted-coding:

Writing code in your new project
--------------------------------

Now that you have a project created, you can start writing code and utilize the
features that eclim provides.

.. note::

   Below we'll walk through a trivial java example, but some of the steps apply to
   all the languages that eclim supports, although the command names may differ a
   bit.  For additional docs on working with the language of your choice, please
   see the relevant section of the docs:

   - :doc:`java </vim/java/index>`
   - :doc:`python </vim/python/index>`
   - :doc:`etc. <vim/index>`

Lets get started writing our first java application using eclim.

1. First, navigate to your new project's source directory (src/java in this
   example) and create any necessary package directories:

   ::

     $ cd /path/to/my_project/src/java
     $ mkdir -p org/test/

2. Then start editing your first java source file:

   ::

     $ vim org/test/TestMain.java

   .. code-block:: java

     package org.test;

     public class TestMain
     {
       public static final void main(String[] args)
       {

       }
     }

3. You can start to use some of the core features now.  For example, lets add
   the following code to the main method so we can test eclim's source code
   validation:

   .. code-block:: java

     System.

   Then save the file and note that an error marker is placed in the left
   margin of your file and when the cursor is on that line an error message is
   printed at the bottom of your vim window.  You can also run :lopen to view
   all the errors in the file at once.

4. Now lets try out code completion.  Place your cursor on the '.' of 'System.'
   and start insert mode in vim using 'a', then follow the example below:

   .. code-block:: java

     System.<ctrl-x><ctrl-u>             // starts the completion mode
     System.<ctrl-n>                     // cycle through the completion suggestions
     System.out                          // assuming you chose the 'out' suggestion
     System.out.p<ctrl-x><ctrl-u>        // now start completion again
     System.out.p<ctrl-n>                // hit <ctrl-n> until you get 'println'
     System.out.println(
     System.out.println("Hello World");  // finish up the example code.

5. After saving the file you should have no more validation errors, so now we
   can run the code like so:

   ::

     :Java

   After running the :Java command in vim you should now see your output in a
   new split window.

This only scratches the surface on the number of :doc:`java features
</vim/java/index>` that eclim provides, but hopefully this example was enough to
get you started.

.. _gettingstarted-maven:

Maven Users
-----------

Creating your first project with maven can be accomplished using the same
method as any other java project, or you can utilize some of maven's built in
features to get your project started.

1. Run maven's generate archetype to create the project directory and samples:

   .. code-block:: bash

     $ mvn archetype:generate

2. Once you've created the initial project directory, cd into that directory
   and run the following command to generate the necessary eclipse files:

   .. code-block:: bash

     $ cd <project_dir>
     $ mvn eclipse:eclipse

3. Now you can start an instance of vim at the project's root directory and run
   the following commands to:

   - set the necessary eclipse classpath variable to point to your maven
     repository.
   - import your new project into eclipse.

   .. code-block:: bash

     $ vim
     :MvnRepo
     :ProjectImport /path/to/new/project
