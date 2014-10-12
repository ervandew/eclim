.. Copyright (C) 2014  Eric Van Dewoestine

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

Java Debugging
==============

.. _\:JavaDebugStart:

Starting a  debug session
-------------------------

Before starting a debug session from vim you first need to do a couple things:

1. Start your java program with the debugging hooks enabled:

::

  $ java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=1044 \
      -classpath ./bin org.eclim.test.debug.Main

2. Start vim with the --servername argument (eclimd currently sends debugger
   updates to vim using vim's remote invocation support):

::

  $ vim --servername debug ...

.. note::

  The server name you choose doesn't matter as long as you don't have another vim
  instance running with that same name.

Once you've got your java program running and vim started with a servername, you
can then start your debug session using the **:JavaDebugStart** command.
This command requires the hostname/IP and the port number on which the debug
server is running.

.. code-block:: vim

  :JavaDebugStart localhost 1044

.. _\:JavaDebugBreakpointAdd:

Add a breakpoint
-----------------

To add a breakpoint, simply open the file, position the cursor on the desired
line and run the **:JavaDebugBreakpointToggle** command. Note that if a breakpoint
was already defined on this line, then its status will be toggled; i.e., if it was
enabled, then it wil be disabled and vice-versa.

.. code-block:: vim

  :JavaDebugBreakpointToggle

.. _\:JavaDebugBreakpointsList:

Listing your breakpoints
------------------------

To view a list of all your breakpoints you can use the
**:JavaDebugBreakpointsList** command, which by default will list all
breakpoints for the current file, or with the ``!`` suffix, it will list all
breakpoints for the current project and all project dependencies.

.. code-block:: vim

  :JavaDebugBreakpointsList
  :JavaDebugBreakpointsList!

This will open a new window which displays your breakpoints grouped by file or
by project and file.

**Mappings**

- **<cr>** - Jump to the file and line of the breakpoint under the cursor
- **T** - Toggles the breakpoint under the cursor, or all breakpoints under the
  file or project when used on the class name or project name.
- **D** - Deletes the breakpoint under the cursor, or all breakpoints under the
  file or project when used on the class name or project name.

.. _\:JavaDebugBreakpointRemove:

Remove breakpoints
------------------

In addition to using the delete mapping provided in the
:ref:`:JavaDebugBreakpointsList <:JavaDebugBreakpointsList>` window, you can
also remove all breakpoints in the current file using the
**:JavaDebugBreakpointRemove** command, or with the ``!`` suffix, you can remove
all breakpoints in the current project.

.. code-block:: vim

  :JavaDebugBreakpointRemove
  :JavaDebugBreakpointRemove!

.. _\:JavaDebugStep:

Step Through the Code
---------------------
There are 3 ways to step through code using the **:JavaDebugStep** command and
an action argument.

- **over**: Step over current line
- **into**: Step into a function
- **return**: Return from current function

::

  :JavaDebugStep over
  :JavaDebugStep into
  :JavaDebugStep return

.. _\:JavaDebugStatus:

Status
------
When a debugging session is started, a status window is automatically opened at
the bottom in a horizontal split window. It has 2 panes\:

- Debug Threads: The left pane shows active threads along with its stack frames.

  **Mappings**

  - **s** - Suspend the thread under the cursor.
  - **S** - Suspend all threads.
  - **r** - Resume the thread under the cursor.
  - **R** - Resume all threads.
  - **B** - Open the breakpoints window showing all breakpoints for this project
    and dependencies.

- Debug Variables: The right pane shows the variables available for the thread
  selected on the left pane. Variables can be seen only for suspended threads.
  If there are suspended threads, then one of them is automatically selected and
  its variables displayed.

  **Mappings**

  - **<cr>** - Expands the variable. Nested variables are shown in a tree like
    structure. To collapse the variable, press <CR> again.
  - **p** - Displays the toString value of the variable under cursor. This is
    equivalent to the Details pane in Eclipse.
  - **B** - Open the breakpoints window showing all breakpoints for this project
    and dependencies.

If for some reason, the status window is not updated, or you accidentally closed it,
you can manually refresh it by running **:JavaDebugStatus** command.

.. code-block:: vim

  :JavaDebugStatus

.. _\:JavaDebugStop:

Suspend / Resume
-----------------

In addition to using the mappings provided in the :ref:`:JavaDebugStatus
<:JavaDebugStatus>` threads window, you can also suspend and resume threads
using the following commands:

- To suspend the entire debugging session (all threads), run
  **:JavaDebugThreadSuspendAll** from any window.
- To resume the entire debugging session (all threads), run
  **:JavaDebugThreadResumeAll** from any window.

Stop
-----

To stop a debug session, you can use the **:JavaDebugStop** command.

.. code-block:: vim

  :JavaDebugStop

.. _\:JavaDebugThreadSuspendAll:
.. _\:JavaDebugThreadResume:
.. _\:JavaDebugThreadResumeAll:

Configuration
-------------
.. _g\:EclimJavaDebugLineHighlight:

- **g:EclimJavaDebugLineHighlight** (Default: 'DebugBreak')
  Highlight group to use for showing the current line being debugged.

.. _g\:EclimJavaDebugLineSignText:

- **g:EclimJavaDebugLineSignText** (Default: '•')
  Text to use on sign column for showing the current line being debugged.

.. _g\:EclimJavaDebugStatusWinOrientation:

- **g:EclimJavaDebugStatusWinOrientation** (Default: 'vertical')
  Sets the orientation for the splits inside the debug status windows;
  if they should be tiled vertically or horizontally.
  Possible values\:
  - horizontal
  - vertical

.. _g\:EclimJavaDebugStatusWinWidth:

- **g:EclimJavaDebugStatusWinWidth** (Default: 50)
  Sets the window width for the splits inside the debug status window.
  This is only applicable when the orientation is horizontal.

.. _g\:EclimJavaDebugStatusWinHeight:

- **g:EclimJavaDebugStatusWinHeight** (Default: 10)
  Sets the window height for the splits inside the debug status window.
  This is only applicable when the orientation is vertical.

Troubleshooting
---------------

- Expanding a variable shows an empty line with just a dot.
  You probably haven't pressed the <Enter> key on the variable.
  Nested variables are retreived one level at a time from the server to be
  performant. Since we are using VIM folds, any mapping that simply opens a
  fold will not cause variables to be retrieved.

- A split window is created when stepping into a function (JavaDebugStep into)
  from the debug status window. It is not clear why this is happening. To avoid
  this problem, run step into command outside the debug status window.

.. _eclim-user: http://groups.google.com/group/eclim-user
