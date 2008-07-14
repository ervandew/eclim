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

.. _vim/java/constructor:

Class Constructor Generation
============================

.. _\:JavaConstructor:

**:JavaConstructor** is a command that will create either an empty constructor,
or one that takes any selected fields as arguments.

For example if you have the following class\:

.. code-block:: java

  public class Foo
  {
    private int id;
    private String name;
  }

If you were to select the range containing the 'id' and 'name' fields and issue
**:JavaConstructor**, then you would end up with the following code.

.. code-block:: java

  public class Foo
  {
    private int id;
    private String name;

    /**
     * Constructs a new instance.
     *
     * @param id The id for this instance.
     * @param name The name for this instance.
     */
    public Foo (int id, String name)
    {
      this.id = id;
      this.name = name;
    }
  }

If you issue the command with no fields selected, then a default empty
constructor is created.

.. note::

  The insertion of constructors is done externally with Eclipse and with
  that comes a couple :ref:`caveats <vim/issues>`.
