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

- Remove breakpoint under cursor. The cursor should be placed in the desired
  Java file.

- Remove all breakpoints defined in current file. The cursor should be placed in
  dsired Java file.

- Remove all breakpoints defined in workspace.

.. code-block:: vim

  :JavaDebugBreakpointRemove
  :JavaDebugBreakpointRemoveFile
  :JavaDebugBreakpointRemoveAll

.. _\:JavaDebugBreakpoint:

Retrieve breakpoints
--------------------
There are 2 ways to retrieve breakpoints and place them in a split window.

- Get breakpoints defined in current file. The cursor should be placed in the
  desired Java file.

- Get all breakpoints defined in workspace.

.. code-block:: vim

  :JavaDebugBreakpointGet
  :JavaDebugBreakpointGetAll

**Mappings**

.. code-block:: vim

  t - Toggle the breakpoint(s) by either enabling or disabling it.
  d - Delete the breakpoint(s).

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

**Mappings**

.. code-block:: vim

  <CR> - Expands the variable. Nested variables are shown in a tree like structure.
  To collapse the variable, press <CR> again.

  p - Displays the toString value of the variable under cursor. This is
  equivalent to the Details pane in Eclipse.

If for some reason, the status window is not updated, or you accidentally closed it,
you can manually refresh it by running **:JavaDebugStatus** command.

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

Configuration
-------------
- **g:EclimJavaDebugLineHighlight** (Default: 'DebugBreak')

  Highlight group to use for showing the current line being debugged.

- **g:EclimJavaDebugStatusWinOrientation** (Default: 'vertical')

  Sets the orientation for the splits inside the debug status windows;
  if they should be tiled vertically or horizontally.
  Possible values\:

  - horizontal

  - vertical

- **g:EclimJavaDebugStatusWinWidth** (Default: 50)

  Sets the window width for the splits inside the debug status window.
  This is only applicable when the orientation is horizontal.

- **g:EclimJavaDebugStatusWinHeight** (Default: 10)

  Sets the window height for the splits inside the debug status window.
  This is only applicable when the orientation is vertical.

Suggested Mappings
------------------
.. code-block:: vim

  noremap <silent> <localleader>q :JavaDebugStop<CR>
  nnoremap <silent> <localleader>s :JavaDebugThreadSuspend<CR>
  nnoremap <silent> <localleader>r :JavaDebugThreadResume<CR>
  nnoremap <silent> <localleader>b :JavaDebugBreakpointAdd<CR>
  nnoremap <silent> <localleader>br :JavaDebugBreakpointRemove<CR>
  nnoremap <silent> <localleader>bg :JavaDebugBreakpointGet<CR>
  nnoremap <silent> ; :JavaDebugStep over<CR>
  nnoremap <silent> <localleader>e :JavaDebugStep into<CR>
  nnoremap <silent> <localleader>x :JavaDebugStep return<CR>

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
