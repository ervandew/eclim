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

.. _\:JavaDebugVars:

Variables
---------
When a breakpoint is hit, or a thread is suspended, you can retrieve the
variables available in the current stack frame using **:JavaDebugVars** command.
It will open a temporary window and display the variables in it.  Right now, it
only shows values for primitive variables. Support for viewing objects and the
their fields will be added soon.

.. code-block:: vim

  :JavaDebugVars

.. _\:JavaDebugVars:

Stack Frames
------------
To view the current stack frames, run **:JavaDebugStackFrame** command. It will
open a temporary window and display the stack frames of all threads.

.. code-block:: vim

  :JavaDebugStackFrame

.. _\:JavaDebugControl:

Control
-------
To suspend, resume or stop the debug session, you can use **:JavaDebugControl**
command.

.. code-block:: vim

  :JavaDebugControl suspend
  :JavaDebugControl resume
  :JavaDebugControl stop

.. _eclim-user: http://groups.google.com/group/eclim-user
