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

.. _eclimd:

Eclimd
======

Starting eclimd
---------------

**Linux**:  To start eclimd from linux, simply execute the eclimd script found
in $ECLIPSE_HOME/plugins/org.eclim_version/bin.

**Windows**: The easiest way to start eclimd in windows is to double click on
the eclimd.bat file found in $ECLIPSE_HOME/plugins/org.eclim_version/bin.


Stopping eclimd
---------------

To cleanly shutdown eclim use any one of the following.

- From Vim:

  .. code-block:: vim

    :ShutdownEclim

- From a console:

  .. code-block:: bash

    $ $ECLIPSE_HOME/plugins/org.eclim_version/bin/eclim -command shutdown

- Lastly you can use Ctrl-C at the console if you are running eclimd in the
  foreground, or issue a kill to the eclimd java process.

  .. code-block:: bash

    $ kill *pid*

  You will need to kill the java process since killing the eclimd or eclipse
  process will not do so.  While eclim provides a shutdown hook to support a
  clean shutdown when the java process is killed in this manner, it is still
  recommended that you utilize one of the first two methods instead, and
  reserve this as a last resort. Also note that when killing the java process
  eclipse will pop up an alert dialog notifying you that the java process was
  terminated underneath it.  This is nothing to be alarmed about.


~/.eclimrc
----------

On unix platforms eclim supports an optional .eclimrc file located in your home
directory.  In this file you may supply any system properties which you would
like passed to eclimd at startup.  The format of this file is the same as the
standard java properties file format.

Ex.

.. code-block:: cfg

  # Specifies the port that nailgun / eclim listens on for client requests.
  nailgun.server.port=10012

  # Specifies the workspace directory to use
  # See $ECLIPSE_HOME/configuration/config.ini for other osgi properties.
  osgi.instance.area.default=@user.home/myworkspace

The eclim client will also utilize this file, but only to determine the
nailgun server port should you choose to change the default.

.. note::

  Your system must have **sed** available so that eclim can
  process your .eclimrc file.


eclimd logging
--------------

Eclimd utilizes log4j for all of its logging.  As such, the logging can be
configured via the
$ECLIPSE_HOME/plugins/org.eclim_version/log4j.xml file.

By default, eclimd writes all logging info to both the console and
$ECLIPSE_HOME/plugins/org.eclim_version/log/eclimd.log.
