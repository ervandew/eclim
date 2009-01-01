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

.. _eclimd_headless:

Headless eclipse server
-----------------------

The most mature usage scenario that eclim provides, is the running of a
headless eclipse server and communicating with that server inside of vim.
Starting and stopping of the headless eclipse instance is detailed below.

.. warning::
  While the headless server is running, opening an eclipse gui is **strongly**
  discouraged.  Doing so has the potential to corrupt eclipse's persisted
  state, leading to errors on the next headless startup.

**Starting eclimd**

**Linux**:  To start eclimd from linux, simply execute the eclimd script found
in $ECLIPSE_HOME/plugins/org.eclim_version/bin.

**Windows**: The easiest way to start eclimd in windows is to double click on
the eclimd.bat file found in $ECLIPSE_HOME/plugins/org.eclim_version/bin.


**Stopping eclimd**

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


.. _eclimd_headed:

Headed eclipse server
---------------------

For users that find themselves periodically needing the eclipse gui, or
otherwise wanting to keep the gui open while using eclim, there is support for
running the eclim server inside of a headed eclipse instance.

**Starting eclimd**

The eclim daemon inside of eclipse is implemented as an eclipse view.  To open
the view, go to the Window menu and select:

  Show View -> Other -> Eclim -> eclimd

The view will be opened in a new tab in the same pane as the "Problems" tab, as
shown below.

.. image:: images/screenshots/eclipse/java_editor_eclim_view.png

**Stopping eclimd**

As long as the eclimd tab is open then the eclim daemon will be running.
Stopping the eclim daemon is just a matter of closing the eclimd tab.  Also
note that leaving the tab open and closing eclipse will shutdown the daemon as
well, and on the next start of eclipse the tab will be opened, but the eclim
daemon will not start until the tab is forced to display itself. In other
words, the daemon will not start until the eclimd tab is the active tab in that
group.


.. _gvim_embedded:

Embedded gvim
-------------

.. note::
  Please note that this feature is currently considered alpha.  Please post any
  issues on the `eclim user`_ mailing list.

Another feature provided by eclim for those who prefer to work inside of the
eclipse gui, is the embedding of gvim inside of eclipse.  This feature is
provided by an eclim local fork of `vimplugin`_.  The feature adds a new editor
to eclipse which allows you to open files in gvim by right clicking the file
name in the eclipse tree and then selecting:

  Open With -> Vim

.. image:: images/screenshots/eclipse/gvim_eclim_view.png

The eclim installer should take care of locating your gvim installation for use inside of eclipse, but in the event that it could not locate it, you can set the location and other settings via the vimplugin preferences located under the Windows menu at:

  Preferences -> Vimplugin

.. note::
  If you have vimplugin installed you should remove it prior to using the eclim
  version.

**Eclipse key shortcuts in embedded gvim**

While the embedded gvim has focus, all the eclipse keyboard shortcuts you would
normally use to perform eclipse specific commands will be intercepted by gvim.
Since gvim has its own set of key bindings, the eclipse ones will be either
ignored or perform whatever action they have been mapped to in gvim.

To remedy this situation, eclim provides a means to map eclipse shortcuts
inside of gvim.  To register a shortcut, simply add your mappings to your
vimrc, gvimrc, or other standard gvim file like so:

  .. code-block:: vim

    " maps Ctrl-F6 to eclipse's Ctrl-F6 key binding (switch editors)
    nmap <silent> <c-f6> :call eclim#vimplugin#FeedKeys('Ctrl+F6')<cr>

    " maps Ctrl-F7 to eclipse's Ctrl-F7 key binding (switch views)
    nmap <silent> <c-f7> :call eclim#vimplugin#FeedKeys('Ctrl+F7')<cr>

    " maps Ctrl-F to eclipse's Ctrl-Shift-R key binding (find resource)
    nmap <silent> <c-f> :call eclim#vimplugin#FeedKeys('Ctrl+Shift+R')<cr>

The value supplied to the `FeedKeys` function must be an eclipse compatible key
binding string as found in:

  Windows -> Preferences -> General -> Keys


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

.. _eclim user: http://groups.google.com/group/eclim-user
.. _vimplugin: http://vimplugin.org
