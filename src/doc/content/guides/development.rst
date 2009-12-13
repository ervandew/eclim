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

.. _guides/development:

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

      > ant "-Declipse.home=C:/Program Files/eclipse"

  This will build and deploy eclim to your eclipse and vim directories.  If you
  don't want to supply the eclipse home directory every time, you can set the
  environment variable ECLIM_ECLIPSE_HOME which the build script will then
  utilize.

  .. note::

    If your vimfiles directory is not located at the default location for your
    OS, then you can specify the location using the "vim.files" property:

      ::

        $ ant -Dvim.files=<your vimfiles dir>

  By default the above ant call will build all the eclim plugins, requiring you
  to have all the related dependencies already installed in your eclipse
  distribution.  However, if you only want a subset of the eclim plugins to be
  built, you can specify so using the 'plugins' system property:

  ::

    # build only ant and jdt (java) support
    $ ant -Dplugins=ant,jdt

    # build only cdt (c/c++) support
    $ ant -Dplugins=cdt

    # build only pdt (php) support (requires wst and dltk)
    $ ant -Dplugins=wst,dltk,pdt

    # build only ruby support (requires dltk)
    $ ant -Dplugins=dltk,dltkruby

  .. note::

    On windows you will need to quote the plugins argument if you are building
    more than one plugin:

      > ant "-Dplugins=ant,jdt"

  The currently available list of plugin names include:

  - **jdt**: java support using the eclipse jdt.
  - **ant**: ant support.
  - **maven**: maven support.
  - **wst**: web development support using the eclipse wst.
  - **cdt**: c/c++ support using the eclipse cdt.
  - **dltk**: base support for dltk based lanugages (currently php and ruby).
  - **pdt**: php support using the eclipse pdt.
  - **dltkruby**: ruby support using the eclipse dltk-ruby.


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


What's Next
------------

Now that you're familiar with the basics of building and patching eclim, the
next step is to familiarize yourself with the eclim architecture and to review
the detailed docs on how new features are added.

All of that and more can be found in the
:ref:`eclim development docs <development/index>`.


.. _git: http://git-scm.com/
.. _eclim development group: http://groups.google.com/group/eclim-dev
.. _guide to forking: http://help.github.com/forking/
.. _git-format-patch: http://www.kernel.org/pub/software/scm/git/docs/git-format-patch.html
