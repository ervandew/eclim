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

.. _vim/java/abbreviations:

Abbreviations
=============

Eclim provides the following Vim abbreviations\:

- **fori**

  .. code-block:: java

    for (int ii = 0; ii < ${array}.length; ii++){
    }

- **forI**

  .. code-block:: java

    for (Iterator ii = ${col}.iterator(); ii.hasNext();){
    }

- **fore**

  .. code-block:: java

    for (${object} ${var} : ${col}){
    }

When editing a java source file simply enter the abbreviation while in insert
mode, followed by a space or <ESC> and the corresponding code above will be
added for you.

You will also notice that the code above has replacement variables in the form
of ${variable}.  When the abbreviation is expanded, eclim will remove the first
variable and position the cursor at that position so that you can fill in the
appropriate value.  Additional values, if any, can be replaced by using the
``FillTemplate`` funtion (please see the
:ref:`suggested mappings <javamappings>` for more information). The variable
will be removed and the cursor placed in its position for you to again enter
the appropriate value in its place.

For example, to use the for each (or enhanced for) abbreviation above to iterate
over an array of Date objects stored in a variable "dates", you would simply
(assuming you mapped the template replacement to <Tab>)\:

#. Enter insert mode.</li>
#. Type "fore" followed by a space.</li>
#. Type "Date", then <ESC>.</li>
#. Hit <Tab>.</li>
#. Type "date", then <ESC>.</li>
#. Hit <Tab> again.</li>
#. Type "dates", then <ESC>.</li>

After that sequence you would have the following code.

.. code-block:: java

  for (Date date : dates){
  }

With the for each example you will only save yourself a few keystrokes, but with
the other for loop abbreviations you can save many more.
