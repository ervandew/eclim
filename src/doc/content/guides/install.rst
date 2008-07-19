.. Copyright (C) 2005 - 2008  Eric Van Dewoestine

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
- `Eclipse SDK 3.3.x`_
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

Step 3: Start the eclimd server and test it.
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

See the documentation :ref:`below <start_test>`.

.. _start_test:

Starting and testing eclim.
---------------------------

Once you have finished the installation process, you can then start up the
eclim server and verify that it is running properly.

#.  The first step is to start the eclimd server.

    - **Linux**:  To start eclimd from linux, simply execute the eclimd script
      found in $ECLIPSE_HOME/plugins/org.eclim_version/bin.
    - **Windows**: The easiest way to start eclimd in windows is to double
      click on the eclimd.bat file found in
      $ECLIPSE_HOME/plugins/org.eclim_version/bin.

#.  Once you have started the server you can test it by opening a Vim
    window and issuing the command, :ref:`:PingEclim`.  The result of executing
    this command should be "eclim *version*" echoed to the bottom of your Vim
    window.  If however, you receive ``unable to connect to eclimd - connect:
    Connection refused``, or something similar, then your eclimd server is not
    running or something is preventing eclim from connecting to it.  If you
    receive this or any other errors and are unsure of what steps to take,
    please feel free to visit the forums_ so that someone can help resolve your
    issue.

    Example of successful ping\:

    .. image:: ../images/screenshots/ping_success.png

    Example of failed ping\:

    .. image:: ../images/screenshots/ping_failed.png

#.  Regardless of the ping result, you can also verify your vim settings
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

After doing that you can then procede to create your first project\:

- :ref:`Java Project Guide <guides/java/project>`
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

.. _java development kit: http://java.sun.com/javase/downloads/index.html
.. _eclipse sdk 3.3.x: http://eclipse.org/downloads/index.php
.. _vim 7.1.x: http://www.vim.org/download.php
.. _eclim_version.sh: http://sourceforge.net/project/platformdownload.php?group_id=145869&sel_platform=5687
.. _eclim_version.exe: http://sourceforge.net/project/platformdownload.php?group_id=145869&sel_platform=5685
.. _forums: http://sourceforge.net/forum/?group_id=145869
