.. Copyright (C) 2005 - 2014  Eric Van Dewoestine

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
================

.. _\:JavaDebugStart:

Start debug session
-------------------
A debug session can be started by using **:JavaDebugStart** command.
This command requires a hostname/IP and the port number on which the debug server is running.

.. code-block:: vim

  :JavaDebugStart localhost 5000

.. _\:JavaDebugBreakpointAdd:

Add a breakpoint
-----------------
To add a breakpoint, simply open the file, position the cursor on the desired
line and run **:JavaDebugBreakpointAdd** command.

.. code-block:: vim

  :JavaDebugBreakpointAdd

.. _\:JavaDebugBreakpointRemove:

Remove breakpoint(s)
---------------------
There are 3 ways to remove breakpoints.

- Remove breakpoint under cursor

- Remove all breakpoints defined in current file

- Remove all breakpoints defined in workspace

.. code-block:: vim

  :JavaDebugBreakpointRemove
  :JavaDebugBreakpointRemoveFile
  :JavaDebugBreakpointRemoveAll

.. _\:JavaDebugBreakpoint:

Retrieve breakpoints
--------------------
There are 2 ways to retrieve breakpoints.

- Get breakpoints defined in current file

- Get all breakpoints defined in workspace

.. code-block:: vim

  :JavaDebugBreakpointGet
  :JavaDebugBreakpointGetAll

.. _\:JavaDebugStep:

Step
----
There are 3 ways to step through code using **:JavaDebugStep** command and an
action argument.

- over: Step over current line

- into: Step into a function

- return: Return from current function

.. code-block:: vim

  :JavaDebugStep over
  :JavaDebugStep into
  :JavaDebugStep return

.. _\:JavaDebugStatus:

Status
------
When a debugging session is started, a status window is automatically opened at
the bottom in a horizontal split window. It has 2 panes\:

- Debug Threads: The left pane shows active threads along with its stack frames.

- Debug Variables: The rigt pane shows the variables available for the thread
  selected on the left pane. Variables can be seen only for suspended threads.
  If there are suspended threads, then one of them is automatically selected and
  its variables displayed.

If for some reason, the status window is not updated, you can manually refresh it
by running **:JavaDebugStatus** command.

.. code-block:: vim

  :JavaDebugStatus

.. _\:JavaDebugStop:

Stop
-----
To stop a debug session, you can use **:JavaDebugStop** command.

.. code-block:: vim

  :JavaDebugStop

.. _\:JavaDebugSuspend:

Suspend
--------
There are 2 ways to suspend execution.

- To suspend execution of a single thread, jump to the Debug Threads split
  window, place cursor on desired thread, and run **:JavaDebugThreadSuspend**
  command.

- To suspend the entire debugging session (all threads), run
  **:JavaDebugThreadSuspendAll** from any window.

.. code-block:: vim

  :JavaDebugThreadSuspend
  :JavaDebugThreadSuspendAll

.. _\:JavaDebugResume:

Resume
------
There are 2 ways to resume execution.

- To resume execution of a single thread, jump to the Debug Threads split
  window, place cursor on desired thread, and run **:JavaDebugThreadResume**
  command. For convenience, this command is allowed from any window. If it detects
  that the cursor is not in the Debug Threads window, it will try to suspend the
  last thread that the user was stepping through.

- To resume the entire debugging session (all threads), run
  **:JavaDebugThreadResumeAll** from any window.

.. code-block:: vim

  :JavaDebugThreadResume
  :JavaDebugThreadResumeAll

.. _eclim-user: http://groups.google.com/group/eclim-user
