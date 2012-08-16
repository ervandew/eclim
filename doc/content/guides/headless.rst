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

.. _guides/headless:

Eclim Headless Server Guide
===========================

The eclim daemon supports running both inside of the eclipse gui and as a
"headless" non-gui server.  However, even in the headless mode, eclipse still
requires a running X server to function.  If you are running eclim on a desktop
then this isn't a problem, but some users would like to run the eclim daemon on
a truly headless server.  To achieve this, you can make use of X.Org's Xvfb
server.

.. note::

  This guide uses the Ubuntu server distribution to illustrate the process of
  setting up a headless server, but you should be able to run Xvfb on the
  distro of your choice by translating the package names used here to your
  distro's equivalents.

The first step is to install the packages that are required to run eclipse and
eclim:

- Install a java jdk, xvfb, and the necessary build tools to compile eclim's
  nailgun client during installation (make, gcc, etc).

  ::

    $ sudo apt-get install openjdk-6-jdk xvfb build-essential

Then you'll need to install eclipse. You may do so by installing it from your
distro's package manager or using a version found on `eclipse.org`_. If you
choose to install a version from you package manager, make sure that the
version to be installed is compatible with eclim since the package manager
version can often be out of date. If you choose to install an `eclipse.org`_
version, you can do so by first downloading eclipse using either a console
based browser like elinks, or you can navigate to the download page on your
desktop and copy the download url and use wget to download the eclipse archive.
Once downloaded, you can then extract the archive in the directory of your
choice.

  ::

    $ wget <eclipse_mirror>/eclipse-<version>-linux-gtk.tar.gz
    $ tar -zxf eclipse-<version>-linux-gtk.tar.gz

.. note::

  Depending on what distribution of eclipse you installed and what eclim
  features you would like to be installed, you may need to install additional
  eclipse features.  If you installed eclipse from your package manager then
  your package manager may also have the required dependency (eclipse-cdt for
  C/C++ support for example). If not, you can install the required dependency
  using eclipse's p2 command line client. Make sure the command references the
  correct repository for your eclipse install (juno in this example) and that
  you have Xvfb running as described in the last step of this guide:

    ::

      DISPLAY=:1 ./eclipse/eclipse -nosplash -consolelog -debug
        -application org.eclipse.equinox.p2.director
        -repository http://download.eclipse.org/releases/juno
        -installIU org.eclipse.wst.web_ui.feature.feature.group

  For a list of eclim plugins and which eclipse features they require, please
  see the `installer dependencies`_.  Note that the suffix '.feature.group'
  must be added to the dependency id found in that file when supplying it to
  the '-installIU' arg of the above command.

Once eclipse is installed, you can then install eclim utilizing the eclim
installer's automated install option (see the :ref:`install-automated` guide
for additional details):

  .. code-block:: bash

    $ java \
      -Dvim.files=$HOME/.vim \
      -Declipse.home=/opt/eclipse \
      -jar eclim_<version>.jar install

The last step is to start Xvfb followed by eclimd:

  ::

    $ Xvfb :1 -screen 0 1024x768x24 &
    $ DISPLAY=:1 ./eclipse/eclimd start

  When starting Xvfb you may receive some errors regarding font paths and
  possibly dbus and hal, but as long as Xvfb continues to run, you should be
  able to ignore these errors.

  The first time you start eclimd you may want to omit the 'start' argument so
  that you can see the output on the console to ensure that eclimd starts
  correctly.

.. _eclipse.org: http://eclipse.org/downloads/
.. _installer dependencies: https://github.com/ervandew/eclim/blob/master/org.eclim.installer/build/resources/dependencies.xml
