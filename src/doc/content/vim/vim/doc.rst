.. Copyright (C) 2005 - 2008  Eric Van Dewoestine

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

.. _vim/vim/doc:

Vim Doc
=======

.. _VimDoc:

To facilitate quick access of Vim help for elements of a Vim script, eclim
provides the command **:VimDoc**.  When executed it will open the help docs for
the supplied argument or for the element under the cursor.  When mapped to a key
binding of your choosing, this command can save you some repetitive typing and
provides an internal map of some keywords to their help topic, which are not
always as straight forward as :help word.

.. note::

  If you come across any instances where this command does not find the
  expected Vim help topic, please report it so that it may be accounted for
  in future revisions.
