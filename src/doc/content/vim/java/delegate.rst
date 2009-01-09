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

.. _vim/java/delegate:

Delegate Methods
================

.. _\:JavaDelegate:

Eclim supports generation of delegate methods via the **:JavaDelegate** command.
To utilize this functionality you must first place the cursor on a global field
(in the main source file class or within an inner class), and then invoke the
**:JavaDelegate** command.

In the following source, you can place the cursor anywhere starting from the
first 'p' in private, to the trailing semicolon, and then invoke the
**:JavaDelegate** command.

.. code-block:: java

  private List myList;

Invoking this command with the cursor on some other source element will generate
the appropriate error.

Once successfully invoked, the result will be the opening of a lower window with
all the methods that may be inserted that will delegate to the value of the
field.

Here is a section of the content displayed when invoking the command on a field
of type java.util.List like the one above.

.. code-block:: java

  com.test.TestList

  package java.util;
  public interface List
    public abstract int size ()
    public abstract boolean isEmpty ()
    public abstract boolean contains (Object o)
    public abstract Object[] toArray ()
    ...

From this newly opened window you can select a method by simply hitting <enter>
with the cursor over the method signature and a delegate method will be created.

For example, if you hit <enter> on the ``size()`` method, then the following
code will be inserted.

.. code-block:: java

  /**
   */
  public int size ()
  {
    return myList.size();
  }

If you would like to generate delegate methods for all methods in an interface
or class, then simply hit <enter> with the cursor over the class name, and
delegate methods will be created for each method in that interface or class.

.. note::

  The insertion of method stubs is done externally with Eclipse and with
  that comes a couple :ref:`caveats <vim/issues>`.

This functionality is currently supported for both outer and inner classes, but
not for anonymous inner classes.
