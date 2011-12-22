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

.. _guides/troubleshoot:

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

    :let g:EclimLogLevel = 10

in vim, which in this case sets the logging to verbose (the default log level
is 4).  After setting the log level any external commands that are run or
autocmd errors encountered will be printed (you may need to run :messages to
see them all).

Below are a series of sections broken up by the behavior (or lack of)
experienced and the steps for diagnosing the cause of that behavior.

If you can't find the answer to your question here, be sure to take a look at
the :ref:`faq <vim/faq>` to see if your question is answered there.


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
  the :ref:`workspace section above <ts_workspace>` or one of the
  :ref:`guides <guides>` on creating a new project.

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
details.  To do so you will need to obtain the command that was being issued by
eclim and run it on the command line where the full stack trace will be
emitted.  To gather these details you will need to:

1. run :messages which will print all the messages for your current vim session
   and find the last executed command which will look like:

   ::

     while executing command: -command ...

   If there are a lot of messages, you may have to page through them to the
   end to find the most recent command.  Once you've found the command, copy
   the command text from "-command" to the end of the command arguments.

2. The next step is to run the command on a command line.

   - Linux / Mac / BSD:

     - open a shell and start by typing the location of your eclim script and
       then append the command you copied.:

       ::

         $ /opt/eclipse/eclim -command ...

       Run that and you should see the entire stack trace for the error.


   - Windows:

     - open a dos prompt and make sure you are on the same drive as your
       eclipse installation (if it's on your D: drive, then type "D:" at the
       prompt and hit enter.
     - Then type out the path to your eclim script and append the command you
       copied:

       ::

         > "C:\Program Files\eclipse\eclim" -command ...

       Run that and you should see the entire stack trace for the error.

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
- **viPlugin**: Attempting to open a file using the embedded gvim support
  fails if viPlugin is installed.  This issue has been reported on the
  viPlugin site.


.. _eclim-user: http://groups.google.com/group/eclim-user
