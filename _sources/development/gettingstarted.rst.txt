.. Copyright (C) 2005 - 2015  Eric Van Dewoestine

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

Developers Guide
================

This guide is intended for those who wish to contribute to eclim by
fixing bugs or adding new functionality.

Checking out the code and building it.
--------------------------------------

.. begin-build

1. Check out the code:
^^^^^^^^^^^^^^^^^^^^^^

::

  $ git clone git://github.com/ervandew/eclim.git

2. Build eclim:
^^^^^^^^^^^^^^^

::

  $ cd eclim
  $ ant -Declipse.home=/your/eclipse/home/dir

This will build and deploy eclim to your eclipse and vim directories.

.. warning::

  Building eclim as root is highly discouraged. If your eclipse install is only
  writable as root, you can supply the ``eclipse.local`` property to tell eclim
  where your eclipse user local directory is located and eclimd will be
  installed there (make sure to replace ``<version>`` portion of the path below
  accordingly):

  ::

    $ ant \
        -Declipse.home=/opt/eclipse \
        -Declipse.local=$HOME/.eclipse/org.eclipse.platform_<version>

  If you do not yet have a ``.eclipse`` directory in your home directory, you
  can run either of the following commands to create it:

  ::

    $ ant -Declipse.home=/opt/eclipse eclipse.init

  or

  ::

    $ /path/to/eclipse/eclipse -initialize

.. note::

  If your eclipse home path contains a space, be sure to quote it:

  ::

    > ant "-Declipse.home=C:/Program Files/eclipse"

.. note::

  If your vimfiles directory is not located at the default location for your
  OS, then you can specify the location using the "vim.files" property:

  ::

    $ ant -Dvim.files=<your vimfiles dir>

When the build starts, it will first examine your eclipse installation to
find what eclipse plugins are available. It will then use that list to determine
which eclim features/plugins should be built and will output a list like the one
below showing what will be built vs what will be skipped:

::

  [echo] ${eclipse}: /opt/eclipse
  [echo] # Skipping org.eclim.adt, missing com.android.ide.eclipse.adt
  [echo] # Skipping org.eclim.dltk, missing org.eclipse.dltk.core
  [echo] # Skipping org.eclim.dltkruby, missing org.eclipse.dltk.ruby
  [echo] # Skipping org.eclim.pdt, missing org.eclipse.php
  [echo] Plugins:
  [echo]   org.eclim.cdt
  [echo]   org.eclim.jdt
  [echo]   org.eclim.pydev
  [echo]   org.eclim.sdt
  [echo]   org.eclim.wst

In this case we can see that four eclim plugins will be skipped along with the
eclipse feature that would be required to build those plugins.

If you don't want to supply the eclipse home directory, or any other
properties, on the command line every time you build eclim, you can create a
``user.properties`` file at the eclim source root and put all your properties
in there:

::

  $ vim user.properties
  eclipse.home=/opt/eclipse
  eclipse.local=${user.home}/.eclipse/org.eclipse.platform_<version>
  vim.files=${user.home}/.vim/bundle/eclim

.. note::

  The eclim vim help files, used by the :ref:`:EclimHelp <:EclimHelp>` command,
  are not built by default. To build these you first need to install sphinx_,
  then run the following command:

  ::

    $ ant vimdocs

  This target also supports the ``vim.files`` property if you want the docs
  deployed to a directory other than the default location.

  .. warning::

    Debian/Ubuntu users: The debian version of sphinx has unfortunately been
    patched to behave differently than the upstream version, resulting in one or
    more eclim supplied sphinx extensions not loading. Another issue you may run
    into is the docutils package, which sphinx depends on, is outdated on
    debian/ubuntu, resulting in another set of errors.

    So to get around these issues you'll need to install sphinx using pip_ or
    similar.

.. _sphinx: http://sphinx-doc.org
.. _pip: http://pip.readthedocs.org/en/latest/index.html

.. end-build

3. Add eclim as a project:
^^^^^^^^^^^^^^^^^^^^^^^^^^

Once you built eclim, you can then :ref:`start the daemon <eclimd-start>` and
add eclim as a project:

::

  :ProjectImport /path/to/git/checkout/of/eclim

.. _coding-style:

Coding Style
------------

When contributing code please try to adhere to the coding style of similar code
so that eclim's source can retain consistency throughout. For java code, eclim
includes a checkstyle configuration which can be run against the whole project:

::

  $ ant checkstyle

or against the current java file from within vim:

::

  :Checkstyle

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
:doc:`eclim development docs </development/index>`.


.. _git: http://git-scm.com/
.. _eclim development group: http://groups.google.com/group/eclim-dev
.. _guide to forking: http://help.github.com/forking/
.. _git-format-patch: http://www.kernel.org/pub/software/scm/git/docs/git-format-patch.html
