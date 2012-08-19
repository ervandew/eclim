.. Copyright (C) 2005 - 2012  Eric Van Dewoestine

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

Gotchas / Quirks
================

Some portions of eclim make use of the editing APIs provided by Eclipse.  While
this makes for much cleaner code and far fewer chances of errors while trying
to accomodate everyone's coding style, or other issues, it does have its
drawbacks:

- **Undo:**

  When changes to the source file are made outside of Vim, the plugin must
  issue an :edit on the file to force Vim to re-read the now updated file.  The
  downside is that Vim clears the undo tree when the file is re-read.

  I have had an email exchange with Bram regarding this issue and he has added
  an entry to the vim todo list to as a result:

  ::

    See ":e" as a change operation, find the changes and add them to the
    undo info.  Needed for when an external tool changes the file.

  Until that change is made to vim, you can still revert your file using the
  :doc:`local history </vim/history>` support.


- **Formatting (tabs vs. spaces):**

  When inserting new code eclim will always use tabs and the corresponding Vim
  plugin will issue a :retab on the new code so that the user's Vim settings
  will reformat the code to the user's chosen preference.  However, Eclipse
  gets in the way a little bit here.  Eclipse defaults to tabs for all
  indentation, so if that is how your Vim options are setup then you probably
  don't have anything to worry about.

  If you instead have Vim setup to expand tabs to spaces, then you may
  encounter times when Eclipse will insert the code with no indentation.

  To resolve this you will need to edit some eclipse settings:

  Using vim:

  #. Open the eclim settings
     (global: :ref:`:EclimSettings` or project level: :ref:`:ProjectSettings`)
  #. Edit the indentation settings to match your vim options\:

     JDT -> org.eclipse.jdt.core.formatter.*

  Using Eclipse:

  #. Shutdown eclimd.
  #. Open Eclipse.
  #. Under the "Window" menu choose "Preferences"
  #. Expand the "Java" node and then the "Code Style" node in the tree on
     the left.
  #. Click on the "Formatter" item under the "Code Style" node.
  #. Click the "New" button to create a new formatter profile (I named
     my "eclim") and then click "OK".
  #. When the "Edit Profile" window comes up, you should be on the
     "Indentation" tab where you can edit the tab policy.  Change it to match
     your Vim settings (note: using Mixed may or may not work all the time).
     When using "Spaces only" be sure to set the "Indentation size" and "Tab
     size" to your preference.

  This should fix any known indentation issues, but if you encounter any other
  problems just send me a sample file along with your Vim and Eclipse settings
  so that I can attempt to reproduce and fix the problem.
