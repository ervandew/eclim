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

.. _\:JavaDebugBreakpointToggle:

Add/remove a single breakpoint
------------------------------
To add a breakpoint, simply open the file, position the cursor on the desired
line and run **:JavaDebugBreakpointToggle** command.  To remove the breakpoint,
run the same command again.

.. code-block:: vim

  :JavaDebugBreakpointToggle

.. _\:JavaDebugBreakpoint:

Retrieve/Delete all breakpoints
-------------------------------
To get all existing breakpoints or delete all existing breakpoints, run
**:JavaBreakpoint** command with an action.

.. code-block:: vim

  :JavaDebugBreakpoint get_all
  :JavaDebugBreakpoint delete_all

.. _\:JavaDebugStep:

Step
----
There are 3 ways to step through code using **:JavaDebugStep** command and an
action argument.
#. over: Step over current line
#. into: Step into a function
#. return: Return from current function

.. code-block:: vim

  :JavaDebugStep into
  :JavaDebugStep over
  :JavaDebugStep return

.. _\:JavaDebugStatus:

Status
------
When a debugging session is started, a status window is automatically opened at
the bottom in a horizontal split window. It has 2 panes:
- Debug Threads: The left pane shows active threads along with its stack frames.

- Debug Variables: The rigt pane shows the variables available for the thread
  selected on the left pane. Variables can be seen only for suspended threads.
  If there are suspended threads, then one of them is automatically selected and
  its variables displayed.

  As of now, there is only suport for displaying primitive variables. Support for
  viewing objects will be added soon.

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
To suspend execution of a thread, you can use **:JavaDebugThreadSuspend** command.
Right now, it suspends a random thread. We will soon expose functionality to
suspend a specific thread or threads.

.. code-block:: vim

  :JavaDebugThreadSuspend

.. _\:JavaDebugResume:

Resume
------
To resume execution of a thread, you can use **:JavaDebugThreadResume** command.
Right now, it resumes a random suspended thread. We will soon expose functionality
to resume a specific thread or threads.

.. code-block:: vim

  :JavaDebugThreadSuspend

.. _eclim-user: http://groups.google.com/group/eclim-user
