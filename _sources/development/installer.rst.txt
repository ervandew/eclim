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

Building the eclim installer
============================

Unless you are working on improvements to the eclim installer, you shouldn't
ever need to build it, but should the need arise here are the instructions for
doing so.

To build the installer you first need a couple external tools installed:

* sphinx_: Sphinx is used to build the eclim documentation which is included in
  the installer.

  Eclim also uses a custom sphinx theme which is included in eclim as a git
  submodule. So before you can build the installer you will need to initialize
  the submodule:

  ::

    $ git submodule init
    $ git submodule update

* graphviz_:  The docs include a few uml diagrams which are generated using
  plantuml_ (included in the eclim source tree) which in turn requires
  graphviz_.

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

In lieu of supplying the formic home on the command line, you can instead put
it in a ``user.properties`` file at the eclim source root:

::

  $ vim user.properties
  formic.home=/your/formic/install/dir

.. _formic: http://github.com/ervandew/formic
.. _graphviz: http://www.graphviz.org/
.. _plantuml: http://plantuml.sourceforge.net/
.. _sphinx: http://sphinx-doc.org
