.. Copyright (C) 2005 - 2010  Eric Van Dewoestine

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

- Install a sun jdk:

  ::

    $ sudo apt-get install sun-java6-jdk

- Install Xvfb and some other gui related dependencies:

  ::

    $ sudo apt-get install xvfb libgtk-directfb-2.0-dev x11-xkb-utils libgl1-mesa-dri

The next step is to install eclipse.  Usually the eclipse version found in your
distro's package manager repository is behind the latest versions found on
eclipse.org and may not be supported by eclim any longer.  So, the recommended
means of installing eclipse is to download a version from `eclipse.org`_.  You
can either use a console based browser like elinks, or you can navigate to the
download page on your desktop and copy the download url and use wget to
download the eclipse archive.  Once downloaded you can then extract the archive
in the directory of your choice.

  ::

    $ wget <eclipse_mirror>/eclipse-<version>-linux-gtk.tar.gz
    $ tar -zxvf eclipse-<version>-linux-gtk.tar.gz

Once eclipse is installed, you can then install eclim.  Since the eclim
installer does not yet support console installs, you can checkout the code from
git and build it:

- Install the necessary packages to build eclim:

  ::

    $ sudo apt-get install build-essential git-core
    $ sudo apt-get --no-install-recommends install ant ant-optional

- Clone eclim from the git repository and optionally checkout the version of
  eclim you wish to build (1.5.2 in this case):

  ::

    $ git clone git://github.com/ervandew/eclim.git
    $ cd eclim
    $ git checkout 1.5.2

- Then you can build eclim with the plugins you wish to enable (see the
  :ref:`developers guide <guides/development>` for more info on building
  eclim).

  ::

    $ ant -Declipse.home=/home/ervandew/eclipse -Dplugins=ant,jdt

  If you want to build in support for one or more eclim plugins for which the
  required dependency is not installed in your eclipse distribution, you can
  install the dependency using eclipse's p2 command line client.  Make sure the
  command references the correct repository for your eclipse install (helios
  in this example) and that you have Xvfb running as described in the next step
  of this guide:

  ::

    DISPLAY=:1 ./eclipse/eclipse -nosplash -consolelog -debug
      -application org.eclipse.equinox.p2.director
      -repository http://download.eclipse.org/releases/helios
      -installIU org.eclipse.wst.web_ui.feature.feature.group

  For a list of eclim plugins and which eclipse features they require, please
  see the `installer dependencies`_.  Note that the suffix '.feature.group'
  must be added to the dependency id found in that file when supplying it to
  the '-installIU' arg of the above command.

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
.. _installer dependencies: http://github.com/ervandew/eclim/blob/master/src/installer/resources/dependencies.xml
