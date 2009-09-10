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

.. _vim/faq:

FAQ
====

.. _eclim_workspace:

How do I tell eclim which eclipse workspace to use?
---------------------------------------------------

To configure the workspace you can start eclimd like so:

  ::

    $ eclimd -Dosgi.instance.area.default=@user.home/another_workspace

Note the system property ``osgi.instance.area.default``, which is used to
specify the location of your workspace.  Also note the variable
``@user.home`` which will be replaced with your home directory at runtime.

If you are running a unix variant (linux, mac osx, bsd, etc.) then you
can specify the above system property in the .eclimrc file in your home
directory.

  ::

    $ echo "osgi.instance.area.default=@user.home/another_workspace" >> ~/.eclimrc

For Windows users there are a couple alternatives to the unsupported
.eclimrc:

  1. Your first option is to add a new environment variable:

     - | Windows 2000: Control Panel > System > Advanced > Environment Variables
       | Windows XP: Control Panel > Performance And Maintenance > System >
         Advanced > Environment Variables
     - | Under "User variables..." click "New..."
       | Variable Name: ECLIMD_OPTS
       | Variable Value: -Dosgi.instance.area.default=\@user.home/another_workspace

     - Then you can start eclimd as normal (via the eclimd.bat file).

  2. The second option is to create a shortcut to the eclimd.bat file:

     - In Windows Explorer, open your eclipse folder.
     - Hold down the right mouse button and drag the eclimd.bat file to where
       you want the shortcut to exist (like your desktop) and release the
       right mouse button.
     - Choose "Create Shortcut(s) Here"
     - Right click the shortcut and choose "Properties"
     - | On the "Shortcut" tab edit the "Target:" field and append:
       | -Dosgi.instance.area.default=\@user.home/another_workspace


.. _eclim_proxy:

How can I configure eclim to use a proxy?
-----------------------------------------

The occasional eclim feature requires network access to function properly.
For example, xml validation may require validating the file against a dtd or
xsd located remotely.  If you are behind a proxy then you may need to provide
eclim with the necessary proxy settings.

  ::

    $ eclimd -Dhttp.proxyHost=my.proxy -Dhttp.proxyPort=8080

If you are running a unix variant (linux, mac osx, bsd, etc.) then you
can specify the above system property in the .eclimrc file in your home
directory.

  ::

    $ echo -e "http.proxyHost=my.proxy\nhttp.proxyPort=8080" >> ~/.eclimrc

If your proxy requires authentication, you'll need to supply the
``-Dhttp.proxyUser`` and ``-Dhttp.proxyPassword`` properties as well.

On Windows systems you can use the same steps described above, for setting
the workspace location, to also set the proxy settings.

.. _eclim_memory:

How do I specify jvm memory arguments for eclim (fix OutOfMemory errors).
-------------------------------------------------------------------------

If you are using the headless version of eclimd, then you have a couple
options:

1. pass the necessary jvm args to eclimd. For example, to increase the heap
   size:

   ::

     $ eclimd -Xmx256M

2. if you are using a unix variant, then you can add the necessary vm args to
   a .eclimrc file in your home directory.

   ::

      # increase heap size
      -Xmx256M

      # increase perm gen size
      -XX:PermSize=64m
      -XX:MaxPermSize=128m

   On Windows systems you can use the same steps described above, for setting
   the workspace location, to also specify the jvm memory args.

If you are using the headed version of eclimd, then setting the jvm memory
arguments for eclim is the same procedure as setting them for eclipse.  Details
can be found on the `eclipse wiki`_.


.. _eclim_troubleshoot:

How do I troubleshoot features not functioning, or errors encountered?
----------------------------------------------------------------------

For troubleshooting eclim, please see the dedicated
:ref:`troubleshooting guide <guides/troubleshoot>`.

.. _eclim_full_headless:

How can I run eclimd on a truly headless server?
------------------------------------------------

Please see the :ref:`headless guide <guides/headless>`.


.. _eclipse wiki: http://wiki.eclipse.org/Eclipse.ini
