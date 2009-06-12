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

.. _vim/code_completion:

Code Completion
===============

All the code completion functionality provided by eclim (ant, java, etc) makes
use of the new "User Defined Completion" added to Vim 7.  To initiate code
completion enter insert mode and type *Ctrl-X Ctrl-U*.  By default Vim will
open a popup if there is more than one completion.

Example with java completion

.. image:: ../images/screenshots/java/completion.png

Once you have started the completion you can use *Ctrl-N* to proceed to the
next match and *Ctrl-P* to move to the previous match.

If you are like me and you find those key strokes a bit cumbersome, then you
can use the SuperTab_ plugin which allows you to use the Tab key for
completion.

To find out more about Vim's insert completion execute

  **:h ins-completion**

from within Vim.

.. note::

  If you are using the embedded gvim inside of eclipse and after hitting ctrl-x
  ctrl-u you still see the text

  ::

    -- ^X mode (^]^D^E^F^I^K^L^N^O^Ps^U^V^Y)

  in the vim status bar, then eclipse has a command mapped to ctrl-u which is
  preventing gvim from receiving that keystroke.  To resolve the issue:

  1. open the key preferences page: Window -> Preferences -> General -> Keys
  2. find the eclipse command bound to Ctrl+U (it should be the "Execute"
     command).
  3. select that command and either unbind it or change the key combination
     that it is bound to.

.. _supertab: http://www.vim.org/scripts/script.php?script_id=1643
