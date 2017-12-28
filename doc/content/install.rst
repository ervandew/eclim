.. Copyright (C) 2005 - 2017  Eric Van Dewoestine

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

==================
Download / Install
==================

Requirements
============

Before beginning the installation, first confirm that you have met the
following requirements.

- `Java Development Kit`_ 1.8 or greater
- `Vim`_ 7.1 or greater
- `Eclipse eclipse_version`_
- Mac and Linux users must also have make and gcc installed.

  **Minimum Vim Settings**: In order for eclim to function properly, there is a
  minimum set of vim options that must be enabled in your vimrc file (:h vimrc).

  - **set nocompatible**

    Execute :h 'compatible' for more info.  You can confirm that
    compatibliity is turned off by executing the following in vim:

    .. code-block:: vim

      :echo &compatible

    Which should output '0', but if not, then add the following to your ~/.vimrc
    files (_vimrc on Windows):

    .. code-block:: vim

      set nocompatible
  - **filetype plugin on**

    Execute :h filetype-plugin-on for more info.  You can confirm
    that file type plugins are enabled by executing the following:

    .. code-block:: vim

      :filetype

    Which should output 'filetype detection:ON  plugin:ON indent:ON', showing
    at least 'ON' for 'detection' and 'plugin', but if not, then update your
    ~/.vimrc (_vimrc on Windows) to include:

    .. code-block:: vim

      filetype plugin indent on

Download
========

You can find the official eclim installer on eclim's github `releases page`_:

- :eclimdist:`jar`

.. _releases page: https://github.com/ervandew/eclim/releases/

Third Party Packages
--------------------

As an alternative to the official installer, there are also some packages
maintained by third parties:

- **Arch:** `aur (eclim) <https://aur.archlinux.org/packages/eclim/>`_,
  `aur (eclim-git) <https://aur.archlinux.org/packages/eclim-git/>`_

Installing / Upgrading
======================

Eclim can be installed a few different ways depending on your preference and
environment:

- :ref:`Graphical Installer <installer>`
- :ref:`Unattended (automated) Installer <installer-automated>`
- :ref:`Build from source <install-source>`
- :ref:`Install on a headless server <install-headless>`

.. _installer:

Graphical Installer
-------------------

Step 1: Run the installer
^^^^^^^^^^^^^^^^^^^^^^^^^

.. note::

  If you have eclipse running, please close it prior to starting the
  installation procedure.

- **First download the installer:**  :eclimdist:`jar`

- **Next run the installer:**

  .. code-block:: bash

    $ java -jar eclim_eclim_release.jar

  Windows and OSX users should be able to simply double click on the jar file
  to start the installer.

  After the installer starts up, simply follow the steps in the wizard
  to install eclim.

  .. note::

    OSX users: eclim considers your eclipse home to be the subdirectory
    that contains the eclipse ``plugins`` folder, ``eclipse.ini`` file,
    etc.—which is the ``Eclipse.app/Contents/Eclipse`` subdirectory inside
    your ``Eclipse.app`` installation. So when the installer prompts you
    for the **Eclipse Home** location, put the absolute path to that
    ``Eclipse.app/Contents/Eclipse`` subdirectory; for example:
    "``/Users/foo/java-mars/Eclipse.app/Contents/Eclipse``" or
    "``/Applications/eclipse/java-mars/Eclipse.app/Contents/Eclipse``".

  If your machine is behind a proxy, take a look at the instructions for
  :ref:`running the installer behind a proxy <installer-proxy>`.

  If you encounter an error running the installer, then consult the known
  :ref:`potential <installer-issues>` issues below.

Step 2: Test the installation
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

To test eclim you first need to start the eclim daemon.  How you start the
daemon will depend on how you intend to use eclim.

.. note::

  More info on running the eclim daemon can be found in the :doc:`eclimd
  </eclimd>` docs.

If you plan on using eclim along with the eclipse gui, then simply start
eclipse and open the eclimd view:

:menuselection:`Window --> Show View --> Other --> Eclim --> eclimd`

By default the eclimd view will also be auto opened when you open a file using:

:menuselection:`Open With --> Vim`

If you plan on using eclim without the eclipse gui, then:

- start the eclimd server.

  - **Linux / Mac / BSD (and other unix based systems)**:
    To start eclimd from linux, simply execute the eclimd script found in your
    eclipse root directory:

    ::

      $ $ECLIPSE_HOME/eclimd

  - **Windows**: The easiest way to start eclimd in windows is to double
    click on the eclimd.bat file found in your eclipse root directory:

    ::

      %ECLIPSE_HOME%/eclimd.bat

Once you have the eclim daemon (headed or headless) running, you can then test
eclim:

- open a vim window and issue the command, :ref:`:PingEclim`.  The result of
  executing this command should be the eclim and eclipse version echoed to the
  bottom of your Vim window.  If however, you receive ``unable to connect to
  eclimd - connect: Connection refused``, or something similar, then your
  eclimd server is not running or something is preventing eclim from connecting
  to it.  If you receive this or any other errors you can start by first
  examining the eclimd output to see if it gives any info as to what went
  wrong.  If at this point you are unsure how to proceed you can view the
  :ref:`troubleshooting guide <troubleshooting>` or feel free to post your
  issue on the `eclim-user`_ mailing list.

  Example of a successful ping:

  .. image:: images/screenshots/ping_success.png

  Example of a failed ping:

  .. image:: images/screenshots/ping_failed.png

- Regardless of the ping result, you can also verify your vim settings
  using the command **:EclimValidate**.  This will check
  various settings and options and report any problems. If all is ok
  you will receive the following message:

  ::

    Result: OK, required settings are valid.

.. _installer-proxy:

Running The Installer Behind a Proxy
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

If you are behind a proxy, you may need to run the installer like so (be sure
to take a look at the related :ref:`faq <eclim_proxy>` as well):

.. code-block:: bash

  $ java -Dhttp.proxyHost=my.proxy -Dhttp.proxyPort=8080 -jar eclim_eclim_release.jar

If your proxy requires authentication, you'll need to supply the
``-Dhttp.proxyUser`` and ``-Dhttp.proxyPassword`` properties as well.

You can also try the following which may be able to use your system proxy settings:

.. code-block:: bash

  $ java -Djava.net.useSystemProxies=true -jar eclim_eclim_release.jar

.. _installer-issues:

Potential Installation Issues
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

In some rare cases you might encounter one of the following errors:

1. Any exception which denotes usage of gcj.
   ::

     java.lang.NullPointerException
       at org.pietschy.wizard.HTMLPane.updateEditorColor(Unknown Source)
       at org.pietschy.wizard.HTMLPane.setEditorKit(Unknown Source)
       at javax.swing.JEditorPane.getEditorKit(libgcj.so.90)
       ...

   Gcj (GNU Compile for Java), is not currently supported.  If you receive
   any error which references libgcj, then gcj is your current default jvm.
   So, you'll need to install the openjdk or a jdk from oracle to resolve the
   installation error.

2. ::

    java.lang.IncompatibleClassChangeError
      at org.formic.ant.logger.Log4jLogger.printMessage(Log4jLogger.java:51)
      ...

   This is most likely caused by an incompatible version of log4j installed in
   your jave ext.dirs.  To combat this you can run the installer like so\:

   ::

     $ java -Djava.ext.dirs -jar eclim_eclim_release.jar

If you encounter an error not covered here, then please report it to the
eclim-user_ mailing list.

.. _installer-automated:

Unattended (automated) install
------------------------------

As of eclim 1.5.6 the eclim installer supports the ability to run an automated
install without launching the installer gui.  Simply run the installer as shown
below, supplying the location of your vim files and your eclipse install via
system properties:

.. code-block:: bash

  $ java \
    -Dvim.files=$HOME/.vim \
    -Declipse.home=/opt/eclipse \
    -jar eclim_eclim_release.jar install

Please note that when using this install method, the installer will only
install eclim features whose third party dependecies are already present in
your eclipse installation.  So before installing eclim, you must make sure that
you've already installed the necessary dependencies (for a full list of
dependencies, you can reference eclim's `installer dependencies`_ file).

**Required Properties:**

* **eclipse.home** - The absolute path to your eclipse installation.

  .. note::

    OSX users: eclim considers your eclipse installation to be in the
    subdirectory that contains the eclipse ``plugins`` folder, ``eclipse.ini``
    file, etc.—which is the ``Eclipse.app/Contents/Eclipse`` subdirectory inside
    your ``Eclipse.app`` installation. So you must run the installer like this:

    .. code-block:: bash

      $ java \
        -Dvim.files=$HOME/.vim \
        -Declipse.home=/Users/foo/java-mars/Eclipse.app/Contents/Eclipse \
        -jar eclim_eclim_release.jar install

    ...replacing ``/Users/foo/java-mars/Eclipse.app/Contents/Eclipse`` with the
    actual absolute path to the ``Eclipse.app/Contents/Eclipse`` directory in
    your particular ``Eclipse.app`` installation.

* **vim.files** (or **vim.skip=true**) - The absolute path to your vim files
  directory. Or if you want to omit the installation of the vim files
  (emacs-eclim users for example) you can supply ``-Dvim.skip=true`` instead.

.. _install-source:

Building from source
--------------------

.. include:: /development/gettingstarted.rst
   :start-after: begin-build
   :end-before: end-build

.. _install-headless:

Installing on a headless server
-------------------------------

The eclim daemon supports running both inside of the eclipse gui and as a
"headless" non-gui server. However, even in the headless mode, eclipse still
requires a running X server to function. If you are running eclim on a desktop
then this isn't a problem, but some users would like to run the eclim daemon on
a truly headless server. To achieve this, you can make use of X.Org's Xvfb
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
installer's automated install option (see the :ref:`installer-automated`
section for additional details):

.. code-block:: bash

  $ java \
    -Dvim.files=$HOME/.vim \
    -Declipse.home=/opt/eclipse \
    -jar eclim_eclim_release.jar install

The last step is to start Xvfb followed by eclimd:

::

  $ Xvfb :1 -screen 0 1024x768x24 &
  $ DISPLAY=:1 ./eclipse/eclimd -b

When starting Xvfb you may receive some errors regarding font paths and
possibly dbus and hal, but as long as Xvfb continues to run, you should be
able to ignore these errors.

The first time you start eclimd you may want to omit the 'start' argument so
that you can see the output on the console to ensure that eclimd starts
correctly.

.. include:: /eclimd.rst
   :start-after: begin-eclimd-user
   :end-before: end-eclimd-user

Upgrading
---------

The upgrading procedure is the same as the installation procedure but please be
aware that the installer will remove the previous version of eclim prior to
installing the new one.  The installer will delete all the org.eclim* eclipse
plugins along with all the files eclim adds to your .vim or vimfiles directory
(plugin/eclim.vim, eclim/\*\*/\*).

.. _uninstall:

Uninstall
=========

To uninstall eclim you can use any eclim distribution jar whose version is
1.7.5 or greater by running it with the 'uninstaller' argument like so:

.. code-block:: bash

  $ java -jar eclim_eclim_release.jar uninstaller

That will open a graphical wizard much like the install wizard which will ask
you again for the location of your vimfiles and eclipse home where you've
installed eclim and will then remove the eclim installation accordingly.

.. note::

  The uninstaller is backwards compatible and can be used to uninstall older
  versions of eclim.

.. _uninstall-automated:

Unattended (automated) uninstall
--------------------------------

Like the installer, the uninstaller also supports an unattended uninstall. You
just need to supply your vim files and eclipse paths as system properties:

.. code-block:: bash

  $ java \
    -Dvim.files=$HOME/.vim \
    -Declipse.home=/opt/eclipse \
    -jar eclim_eclim_release.jar uninstall

**Required Properties:**

* **eclipse.home** - The absolute path to your eclipse installation.
* **vim.files** (or **vim.skip=true**) - The absolute path to your vim files
  directory. Or if you never installed the vim files (emacs-eclim users for
  example) you can supply ``-Dvim.skip=true`` instead.

.. _java development kit: http://www.oracle.com/technetwork/java/javase/downloads/index.html
.. _eclipse.org: http://eclipse.org/downloads/
.. _eclipse eclipse_version: http://eclipse.org/downloads/index.php
.. _vim: http://www.vim.org/download.php
.. _eclim-user: http://groups.google.com/group/eclim-user
.. _installer dependencies: https://github.com/ervandew/eclim/blob/master/org.eclim.installer/build/resources/dependencies.xml
