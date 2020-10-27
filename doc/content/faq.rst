.. Copyright (C) 2005 - 2020  Eric Van Dewoestine

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

=====================
FAQ / Troubleshooting
=====================

FAQ
===

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

If you are using the headed version of eclimd, then setting the jvm memory
arguments for eclim is the same procedure as setting them for eclipse.  Details
can be found on the `eclipse wiki`_.

.. _eclim_troubleshoot:

How do I troubleshoot features not functioning, or errors encountered?
----------------------------------------------------------------------

For troubleshooting eclim, please see the dedicated
:ref:`troubleshooting <troubleshooting>` section below.

.. _eclim_full_headless:

How can I run eclimd on a truly headless server?
------------------------------------------------

Please see the :ref:`headless guide <install-headless>`.

.. _eclim_encoding:

How can I set the default encoding used by eclipse/eclimd?
----------------------------------------------------------

To set the default encoding you can set the ``file.encoding`` system property
according to your setup:

1. Headless eclimd users on any unix variant (Linux, OSX, etc) can simply add
   the following property to your .eclimrc file in your home directory:

   ::

     # set the default file encoding
     file.encoding=utf-8

2. Headed eclimd users can add the system property (eg.
   ``-Dfile.encoding=utf-8``) to your eclipse.ini file found in your eclipse
   install's root directory. Be sure to add the property on a new line after
   the ``-vmargs`` line:

   ::

     ...
     -vmargs
     ...
     -Dfile.encoding=utf-8

   You can read more about the eclipse.ini file on the `eclipse wiki`_.

.. _troubleshooting:

Troubleshooting
===============

The purpose of this guide is to serve as a means to help troubleshoot common
issues encountered when getting start with eclim, or providing information if
you've discovered a possible bug.

The first thing to note is that as of eclim 1.6.1, errors raised by eclimd when
executing an autocmd from vim, like validating a file on write, are no longer
echoed as errors to the user. Instead these errors are logged and only
displayed if your eclim log level is set to a relevant level.  You can set the
log level at any time by running:

.. code-block:: vim

  :let g:EclimLogLevel = 'trace'

in vim, which in this case sets the logging to verbose (the default log level
is ``info``). After setting the log level, any external commands that are run or
autocmd errors encountered will be printed (you may need to run :messages to see
them all).

Below are a series of sections broken up by the behavior (or lack of)
experienced and the steps for diagnosing the cause of that behavior.

If you can't find the answer to your question here, be sure to take a look at
the :doc:`faq </faq>` to see if your question is answered there.


.. _ts_workspace:

Eclim does not recognize any of my existing projects.
-----------------------------------------------------

A fairly common occurrence for users new to eclim, is that after starting
eclimd and then attempting to execute some project dependent functionality,
the functionality appears to do nothing or eclim complains that the project
could not be determined.  If you have existing projects created in eclipse and
eclim is not finding them, then the likely cause is that your projects are
located in a non-default workspace location.

For the answer to how to specify the location of your workspace when starting
eclimd, please see the :ref:`faq <eclim_workspace>` devoted to this.


.. _ts_completion:

I'm editing a [java, python, php, etc] file and code completion doesn't work.
-----------------------------------------------------------------------------

- If you receive the message "E764: Option 'completefunc' is not set", please
  see the :ref:`file type section below <ts_ftplugin>`.

- Next step is to make sure that the current file is in an eclipse project by
  running the following command in the vim windows containing the file you are
  working on.

  ::

    :ProjectInfo

  If that returns an error that it is unable to determine the project, then see
  the :ref:`workspace section above <ts_workspace>` or the guide on
  :ref:`creating a new project <gettingstarted-create>`.

- If the correct project info is found, then try running the completion again,
  if it still doesn't return any results run the command:

  ::

    :messages

  This will print out any messages that you might have missed.  If you see an
  error regarding a java exception while running a command then see the section
  on :ref:`troubleshooting exceptions <ts_exception>`.


.. _ts_ftplugin:

I'm editing a [java, python, php, etc] file and none of the file type commands exist.
-------------------------------------------------------------------------------------

This usually indicates that you don't have file type plugins enabled in vim.
To check you can run:

::

  :EclimValidate

If it complains about filetype plugin support not being found, then follow its
directions on adding the following to your vimrc:

.. code-block:: vim

  filetype plugin indent on


.. _ts_signs_misplaced:

Code validation signs are showing up on the wrong lines.
--------------------------------------------------------

This is most likely a result of eclipse being setup to use a different file
encoding than vim, most likely cp1251 (windows-1251) vs utf-8.  You should be
able to resolve this issue by :ref:`setting eclipse's default encoding
<eclim_encoding>` accordingly.

If you're unsure what encoding to use, try using utf-8.


.. _ts_exception:

I received a java exception "while executing command" message.
--------------------------------------------------------------

If you receive a java exception while performing some action in vim, it should
also include a message indicating the issue.  However, if you receive something
like a NullPointerException or some other exception which doesn't include a
helpful message, then you may have encountered a bug.

Once you've encountered this type of issue, the first step it to get more
details which you can do by enabling eclim debugging in vim:

.. code-block:: vim

  :let g:EclimLogLevel = 'trace'

Then you can perform the same action that triggered the error again. This time
you should receive the full stack trace of the error.

If the error occurs while manually running a command (``:JavaSearch``, etc),
then you can instead prepend ``verbose`` to the command to view the full stack
trace:

.. code-block:: vim

  :verbose JavaSearch ...

Once you've obtained the stack trace, the next step it to send it to the
eclim-user_ mailing list along with a description of what you were doing when
the error occurred, as well as the OS you are on, and whether you were using
eclimd headless or headed (inside of the eclipse gui).

.. _ts_incompatible_plugins:

Incompatible Plugins
--------------------

There are some third party eclipse plugins which currently may interfere with
eclim.  Below is a list of these known plugin incompatibilities.

- **Spring IDE**: At least one user has reported that eclim's java validation
  no longer works after installing the Spring IDE.


.. _eclipse wiki: http://wiki.eclipse.org/Eclipse.ini
.. _eclim-user: http://groups.google.com/group/eclim-user
