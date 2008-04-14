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

.. _vim/java/correct:

Java Code Correction
====================

.. _JavaCorrect:

Code correction in eclim is equivalent to the quick fix functionality of
Eclipse.  When you save a java source file, eclim <a
href="source.html">validates</a> the file and notes which lines contain errors.
To have eclim suggest possible corrections for an error, you simply place the
cursor on the error line and issue **:JavaCorrect**.

The result will be a small window opened at the bottom of Vim where any
correction proposals will be noted. To apply a suggested change, simply move the
cursor to the line describing the modification and hit <enter>. Upon doing so,
the change will be applied to the source file.

Example output of **:JavaCorrect**.

::

  The serializable class Foo does not declare a static final serialVersionUID field of type long
  0.1227:  Add @SuppressWarnings 'serial' to 'Foo'
    ...
    @SuppressWarnings("serial")
    public class Foo
    implements Serializable
  ...

To apply the above change you would hit <enter> on the line\:

::

  0.1227:  Add @SuppressWarnings 'serial' to 'Foo'

.. note::

  The code correction is done externally with Eclipse and with that comes a
  couple <a href="../external_editing.html">caveats</a>.
