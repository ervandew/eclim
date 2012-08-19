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

Eclim Developers Guide
======================

This guide is intended mostly for those who wish to contribute to eclim by
fixing bugs or adding new functionality, but the first section is also useful
for users who would like to use the latest development version of eclim.

.. _development-build:

Checking out the code and building it.
--------------------------------------

1. Check out the code:

   ::

     $ git clone git://github.com/ervandew/eclim.git

   Once you have a local git repository you can utilize the extensive local git
   functionality allowing you to commit code locally, create local branches,
   etc.  For guidelines on managing patches and submitting them, please see the
   :ref:`patch guide <development-patches>` below.

2. Build eclim:

   ::

     $ cd eclim
     $ ant -Declipse.home=/your/eclipse/home/dir

   .. note::

     If your eclipse home path contains a space, be sure to quote it:

     ::

       > ant "-Declipse.home=C:/Program Files/eclipse"

   This will build and deploy eclim to your eclipse and vim directories.

   .. note::

     If your vimfiles directory is not located at the default location for your
     OS, then you can specify the location using the "vim.files" property:

     ::

       $ ant -Dvim.files=<your vimfiles dir>

   If you don't want to supply the eclipse home directory, or any other
   properties, on the command line every time you build eclim, you can create a
   `user.properties` file at the eclim source root and put all your properties
   in there:

   ::

     $ vim user.properties
     eclipse.home=/opt/eclipse
     vim.files=${user.home}/.vim/bundle/eclim

.. _development-patches:

Developing / Submitting Patches
-------------------------------

The preferred means of developing and submitting patches is to use a github
fork. Github provides a nice `guide to forking`_ which should get you started.

Although using a github fork is preferred, you can of course still submit
patches via email using git's format-patch command:

::

  $ git format-patch -M origin/master

Running the above command will generate a series of patch files which can be
submitted to the `eclim development group`_.


Building the eclim installer
----------------------------

It should be rare that someone should need to build the eclim installer, but
should the need arise here are the instructions for doing so.

To build the installer you first need a couple external tools installed:

* sphinx_: Sphinx is used to build the eclim documentation which is included in
  the installer.  Please note however that eclim includes extensions to sphinx
  which target a specific version, so you should install the version that those
  extensions were built for.  The most reliable way to determine the proper
  version is to view the git log for eclim's sphinx extensions, typically the
  most recent log entry will note the proper sphinx version:

  ::

    $ git log -1 src/doc/extension/
    commit df2e9f250b2ccdf53ed7932018acec808ae4538f
    Author: ervandew <ervandew@gmail.com>
    Date:   Sun Nov 1 20:27:45 2009 -0800

        update to sphinx 0.6.3

* formic_: The eclim installer has been developed using the formic framework,
  and requires it to build the installer distributables.  Formic doesn't
  currently have an official release, so you'll need to check out the source
  code:

  ::

    $ git clone git://github.com/ervandew/formic.git

  After checking out the code, you'll need to build the formic distribution:

  ::

    $ cd formic
    $ ant dist

  Then extract the formic tar to the location of your choice

  ::

    $ tar -zxvf build/dist/formic-0.2.0.tar.gz -C /location/of/your/choice

Once you have installed the above dependencies, you can then build the eclim
installer with the following command.

::

  $ ant -Dformic.home=/your/formic/install/dir dist


What's Next
------------

Now that you're familiar with the basics of building and patching eclim, the
next step is to familiarize yourself with the eclim architecture and to review
the detailed docs on how new features are added.

All of that and more can be found in the
:doc:`eclim development docs </development/index>`.


.. _git: http://git-scm.com/
.. _eclim development group: http://groups.google.com/group/eclim-dev
.. _guide to forking: http://help.github.com/forking/
.. _git-format-patch: http://www.kernel.org/pub/software/scm/git/docs/git-format-patch.html
.. _sphinx: http://sphinx.pocoo.org
.. _formic: http://github.com/ervandew/formic
