.. Copyright (C) 2005 - 2011  Eric Van Dewoestine

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

**Linux / Mac / BSD (and other unix based systems)**:
To start eclimd from linux, simply execute the eclimd script found in your
eclipse root directory: $ECLIPSE_HOME/eclimd

.. note::
  When starting the eclim daemon, you must start it as the same user who will
  be running vim.

**Windows**: The easiest way to start eclimd in windows is to double click on
the eclimd.bat file found in your eclipse root directory:
%ECLIPSE_HOME%/eclimd.bat

.. note::
  Even though an eclipse gui is not started in eclim's headless mode, eclipse
  still requires a running X server to function.  To run eclimd on a truely
  headless server, please see the :ref:`headless guide <guides/headless>`.

**Stopping eclimd**

To cleanly shutdown eclim use any one of the following.

- From Vim:

  .. code-block:: vim

    :ShutdownEclim

- From a console:

  .. code-block:: bash

    $ $ECLIPSE_HOME/eclim -command shutdown

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

.. note::

  By default, if you open an instance of gvim from within eclipse, the eclimd
  view will be opened for you if necessary.  This behavior is configurable via
  the Vimplugin preferences.

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

Please note that if you want to use supported eclipse features (code
completion, validation, searching, etc.) from the embedded gvim editor, you
must have the :ref:`eclimd view <eclimd_headed>` open.

.. note::
  If you'd like to have the embedded gvim editor as the default for one or more
  file types, you can configure it to be in your eclipse preferences:

    Window > Preferences > General > Editors > File Associations

The eclim installer should take care of locating your gvim installation for use
inside of eclipse, but in the event that it could not locate it, you can set
the location and other settings via the vimplugin preferences located under the
Windows menu at:

  Preferences -> Vimplugin

.. note::
  If you have vimplugin installed you should remove it prior to using the eclim
  version.

.. note::
  Some users have reported issues with the embedded gvim's command line being
  cut off or possible rendering issues when scrolling through the file.  If you
  experience either of these issues, try adding the following to your vimrc
  file, which should hopefully resolve those problems:

  .. code-block:: vim

    set guioptions-=m " turn off menu bar
    set guioptions-=T " turn off toolbar

**Eclipse/Vim key shortcuts in embedded gvim**

Depending on your OS and windowing system, when the embedded gvim has focus,
you will fall into one of two groups:

1. In the first group of users, all key presses are received by eclipse prior
   to sending them to gvim.

  For this group, when typing a possible key shortcut (ctrl-n for example),
  eclipse will first evaluate that key stroke to see if there are any eclipse
  key bindings registered.  If there are, then eclipse will run the associated
  command and the key stroke is never sent to gvim.  If no key binding is
  found, then eclipse will pass the key stroke through to gvim.  What this
  means for you is that for any gvim key mappings that you use that have an
  eclipse key binding, they will not be evaluated inside of gvim.  So, if you
  encounter this issue, you'll need to remap the keys in vim or eclipse.  To
  remove the key binding from the eclipse side, simply open the "Keys"
  preferences page:

  ::

    Window -> Preferences -> General -> Keys

  Then find the entry in the list that corresponds with the key binding you
  want to remove, select it, and hit the "Unbind Command" button.

  .. note::
    By default eclim will auto-remove a couple of the standard eclipse
    bindings whenever an embedded gvim editor has focus and then restore them
    with a non-gvim editor gains focus:

    - Ctrl+W: in eclipse this closes a tab, but in gvim this is needed to
      switch windows (ex. ctrl-w j).
    - Ctrl+U: in eclipse this run "Execute", but in gvim this is needed to
      run code completion (ex. ctrl-x ctrl-u).

.. _FeedKeys:

2. In the second group, all key presses are received by gvim and not evaluated
   at all by eclipse.

  For this group of users, you may have an eclipse key shortcut that you like
  to use (Shift+Ctrl+R for example), but when you hit that key combination, it
  will be evaluated by gvim instead of eclipse.  To remedy this situation,
  eclim provides a means to map eclipse shortcuts inside of gvim.  To register
  a shortcut, simply add your mappings to your vimrc, gvimrc, or other standard
  gvim file like so:

    .. code-block:: vim

      " maps Ctrl-F6 to eclipse's Ctrl-F6 key binding (switch editors)
      nmap <silent> <c-f6> :call eclim#vimplugin#FeedKeys('Ctrl+F6')<cr>

      " maps Ctrl-F7 to eclipse's Ctrl-F7 key binding (switch views)
      nmap <silent> <c-f7> :call eclim#vimplugin#FeedKeys('Ctrl+F7')<cr>

      " maps Ctrl-F to eclipse's Ctrl-Shift-R key binding (find resource)
      nmap <silent> <c-f> :call eclim#vimplugin#FeedKeys('Ctrl+Shift+R')<cr>

      " maps Ctrl-M to eclipse's Ctrl-M binding to maximize the editor
      nmap <silent> <c-m> :call eclim#vimplugin#FeedKeys('Ctrl+M', 1)<cr>

  The value supplied to the `FeedKeys` function must be an eclipse compatible key
  binding string as found in:

    Windows -> Preferences -> General -> Keys

  Be sure to notice the extra argument to the FeedKeys function in the last
  mapping. Supplying 1 as the arg will result in the refocusing of gvim after
  the eclipse key binding has been executed.


~/.eclimrc
----------

On unix platforms (linux, mac, bsd) eclim supports an optional .eclimrc file
located in your home directory.  In this file you may supply any system
properties or vm args which you would like passed to eclimd at startup.  The
format of this file is the same as the standard java properties file format
with the exception of any vm args which you would like to include.

Ex.

.. code-block:: cfg

  # Bind eclimd to all interfaces
  nailgun.server.host=0.0.0.0

  # Specifies the port that nailgun / eclimd listens on for client requests.
  nailgun.server.port=10012

  # Specifies the workspace directory to use
  # See $ECLIPSE_HOME/configuration/config.ini for other osgi properties.
  osgi.instance.area.default=@user.home/myworkspace

  # increase heap size
  -Xmx256M

  # increase perm gen size
  -XX:PermSize=64m
  -XX:MaxPermSize=128m

The eclim client will also utilize this file, but only to determine the
nailgun server port should you choose to change the default.

.. note::

  Your system must have **sed** available so that eclim can
  process your .eclimrc file.

Both the eclim and eclimd scripts also support a -f argument allowing you to
specify an alternate location for your .eclimrc:

::

  $ eclimd -f ~/.my_eclimrc
  $ eclim -f ~/.my_eclimrc -command ping


eclimd logging
--------------

Eclimd utilizes log4j for all of its logging.  As such, the logging can be
configured via the $ECLIPSE_HOME/plugins/org.eclim_version/log4j.xml file.

By default, eclimd writes all logging info to both the console and to a log
file in your workspace: <workspace>/.metadata/.log.eclimd

.. _eclimd_extdir:

Hosting third party nailgun apps in eclimd
-------------------------------------------

Since nailgun provides a simple way to alleviate the startup cost of the jvm,
other projects utilize it as well.  However, running several nailgun servers
isn't ideal, so eclim supports hosting other nailgun apps via an ext dir where
you can drop in jar files which will be made available to eclim's nailgun
server.

The ext dir that eclim reads from is located in your vim files directory:

Linux / BSD / OSX:

  ::

    ~/.eclim/resources/ext

Windows:

  ::

    $HOME/.eclim/resources/ext

For an example of utilizing the ext dir, please take a look at the
:ref:`VimClojure <guides/clojure/vimclojure>` guide.

.. _eclim user: http://groups.google.com/group/eclim-user
.. _vimplugin: http://vimplugin.org
