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

Installing / Upgrading
======================

Requirements
------------

Before beginning the installation, first confirm that you have met the
following requirements.

- `Java Development Kit`_ 1.5 or greater
- `Eclipse 3.7.x (Indigo)`_
- `Vim 7.1.x`_
- Mac and Linux users must also have make and gcc installed.

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

Step 1: Download and run the installer.
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

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

  If your machine is behind a proxy, take a look at the instructions for
  :ref:`running the installer behind a proxy <installer-proxy>`.

  If you encounter an error running the installer, then consult the known
  :ref:`potential <installer-issues>` issues below.

Step 2: Testing the installation
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

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
  :doc:`troubleshooting guide </guides/troubleshoot>` or feel free to post your
  issue on the `eclim-user`_ mailing list.

  Example of a successful ping:

  .. image:: ../images/screenshots/ping_success.png

  Example of a failed ping:

  .. image:: ../images/screenshots/ping_failed.png

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


What's Next
-----------

Now that you have eclim installed, the next step is to familiarize yourself
with at least the core set of commands that eclim provides, all of which are
found at the index of the eclim :doc:`documentation </vim/index>`.

After doing that you can then proceed to :doc:`getting started guide
</gettingstarted>`.


Upgrading
---------

The upgrading procedure is the same as the installation procedure but please be
aware that the installer will remove the previous version of eclim prior to
installing the new one.  The installer will delete all the files in the eclim
eclipse plugins and the files eclim adds to your .vim or vimfiles directory.
So if you made any alterations to any of these files, be sure to back them up
prior to upgrading.


Building from source
--------------------

If you would like to use the bleeding edge development version of eclim or you
would like to contribute code, then you can checkout and build eclim from
source.  Instructions on doing so can be found in the
:ref:`developers guide <development-build>`.


.. _install-automated:

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

On exception to this is eclim's python plugin which currently does not rely
on any eclipse features, so to enable the installation of that plugin, just add
``-DfeatureList.python=true`` to the install command above.

**Required Properties:**

* **eclipse.home** - The absolute path to your eclipse installation.
* **vim.files** (or **vim.skip=true**) - The absolute path to your vim files
  directory. Or if you want to omit the installation of the vim files
  (emacs-eclim users for example) you can supply ``-Dvim.skip=true`` instead.

.. _java development kit: http://java.sun.com/javase/downloads/index.html
.. _eclipse 3.7.x (indigo): http://eclipse.org/downloads/index.php
.. _vim 7.1.x: http://www.vim.org/download.php
.. _eclim-user: http://groups.google.com/group/eclim-user
.. _installer dependencies: https://github.com/ervandew/eclim/blob/master/org.eclim.installer/build/resources/dependencies.xml
