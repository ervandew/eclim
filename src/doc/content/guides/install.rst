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

.. _guides/install:

Installing / Upgrading
======================

Requirements
------------

Before beginning the installation, first confirm that you have met the
following requirements.

- `Java Development Kit`_ 1.5 or greater
- `Eclipse SDK 3.4.x`_
- `Vim 7.1.x`_

  **Minimum Vim Settings**: In order for eclim to function properly, there is a
  minimum set of vim options that must be enabled in your vimrc file (:h vimrc).

  - **set nocompatible**

    Execute :h 'compatible' for more info.  You can confirm that
    compatibliity is turned off by executing the following:

    .. code-block:: vim

      :echo &compatible

    Which should output '0'.
  - **filetype plugin on**

    Execute :h filetype-plugin-on for more info.  You can confirm
    that file type plugins are enabled by executing the following:

    .. code-block:: vim

      :filetype

    Which should output 'filetype detection:ON  plugin:ON indent:ON', showing
    at least 'ON' for 'detection' and 'plugin'.

.. _installer:

Eclim Graphical Installer
-------------------------

Step 1: Download the eclim installer for your platform.
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

- **Linux (and other unix based systems):**
  `eclim_version.sh`_
- **Windows:**
  `eclim_version.exe`_


Step 2: Run the installer.
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. note::

  If you have eclipse running, please close it prior to starting the
  installation procedure.

- **Linux (and other unix based systems):**
  You can start the installer by running the script you downloaded
  (note: you may have to make it executable first).

  .. code-block:: bash

    $ chmod 755 eclim_version.sh
    $ ./eclim_version.sh

- **Windows:**
  On Windows systems, simply double click the eclim_version.exe file you
  downloaded.

After the installer starts up, simply follow the steps in the wizard
to install the application.

.. note::

  In some rare cases you might encounter the following error\:

  ::

    java.lang.IncompatibleClassChangeError
      at org.formic.ant.logger.Log4jLogger.printMessage(Log4jLogger.java:51)
      ...

  This is most likely caused by an incompatible version of log4j installed in
  your jave ext.dirs.  To combat this you can run the installer like so\:

  ::

    $ FORMIC_OPTS="-Djava.ext.dirs" ./eclim_1.4.0.sh


Step 3: Testing the installation
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

To test eclim you first need to start the eclim daemon.  How you start the
daemon will depend on how you intend to use eclim.

If you plan on using eclim along with the eclipse gui, then:

- start eclipse with the -clean option

  ::

    $ eclipse -clean

  .. note::

    You should only need to start eclipse with the -clean option the first time
    after installing or upgrading eclim.

- open the eclimd view

  Window -> Show View -> Other -> Eclim -> eclimd

If you plan on using eclim without the eclipse gui, then:

- start the eclimd server.

  - **Linux**:  To start eclimd from linux, simply execute the eclimd script
    found in your eclipse root directory:

    ::

      $ $ECLIPSE_HOME/eclimd

  - **Windows**: The easiest way to start eclimd in windows is to double
    click on the eclimd.bat file found in your eclipse root directory:
    %ECLIPSE_HOME%/eclimd.bat

Once you have the eclim daemon (headed or headless) running, you can then test
eclim:

- open a vim window and issuing the command, :ref:`:PingEclim`.  The result of
  executing this command should be the eclim and eclipse version echoed to the
  bottom of your Vim window.  If however, you receive ``unable to connect to
  eclimd - connect: Connection refused``, or something similar, then your
  eclimd server is not running or something is preventing eclim from connecting
  to it.  If you receive this or any other errors you can start by first
  examining the eclimd output to see if it gives any info as to what went
  wrong.  If at this point you are unsure how to proceed, feel free to post
  your issue on the `eclim user`_ mailing list.

    Example of successful ping:

    .. image:: ../images/screenshots/ping_success.png

    Example of failed ping:

    .. image:: ../images/screenshots/ping_failed.png

- Regardless of the ping result, you can also verify your vim settings
  using the command **:EclimValidate**.  This will check
  various settings and options and report any problems. If all is ok
  you will receive the following message\:

  ::

    Result: OK, required settings are valid.


What's Next
-----------

Now that you have eclim installed, the next step is to familiarize yourself
with at least the core set of commands that eclim provides, all of which are
found at the index of the eclim :ref:`documentation <vim/index>`.

After doing that you can then proceed to create your first project\:

- :ref:`Java Project Guide <guides/java/project>`
- :ref:`Python Project Guide <guides/python/project>`
- :ref:`Php Project Guide <guides/php/project>`


Upgrading
---------

The upgrading procedure is the same as the installation procedure but there are
a couple things worth noting.

- The installer will remove the previous version of eclim and install the new
  one.  This includes all the files in the eclim eclipse plugins and the files
  eclim adds to your .vim or vimfiles directory.  So if you made any
  alterations to any of these files, be sure to back them up prior to
  upgrading.
- Since the previous version is removed any time you run the installer, whether
  upgrading or reinstalling the current version, you will need to always select
  the features you want installed regardless of whether they are already
  installed.  In a future version, the installer will attempt to auto select
  them for you.


Building from source
--------------------

If you would like to use the bleeding edge development version of eclim or you
would like to contribute code, then you can checkout and build eclim from
source.  Instructions on doing so can be found in the
:ref:`developers guide <development-build>`.


.. _java development kit: http://java.sun.com/javase/downloads/index.html
.. _eclipse sdk 3.3.x: http://eclipse.org/downloads/index.php
.. _vim 7.1.x: http://www.vim.org/download.php
.. _eclim_version.sh: http://sourceforge.net/project/platformdownload.php?group_id=145869&sel_platform=5687
.. _eclim_version.exe: http://sourceforge.net/project/platformdownload.php?group_id=145869&sel_platform=5685
.. _eclim user: http://groups.google.com/group/eclim-user
