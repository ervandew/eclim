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

Method Generation
=================

.. _\:JavaConstructor:

Constructors
------------

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

.. _\:JavaGetSet:

Getters / Setters
-----------------

- **:JavaGetSet** -
  Generates both getters and setters for the field under the cursor or for
  all fields in the specified range.

.. _\:JavaGet:

- **:JavaGet** -
  Generates getters for the field under the cursor or for all fields in
  the specified range.

.. _\:JavaSet:

- **:JavaSet** -
  Generates setters for the field under the cursor or for all fields in
  the specified range.

.. note::

   If you would like to generate the indexed getter or setter then you can
   suffix the appropriate command above with '!' and if the property is an
   array, an indexed accessor will be created.

   .. code-block:: vim

      :JavaGetSet!

Given the following file:

.. code-block:: java

  public class Foo
  {
    private String name;
    private Bar[] bars;
  }

You can place the cursor on one of the fields and execute **:JavaGetSet** to
generate the getters and setters for the field.  All of the above commands
support ranges as well, so you can use a visual selection or a numbered range to
generate methods for a set of fields.

.. _\:JavaImpl:

Override / Impl
---------------

Eclim provides the ability to view all the methods that the current source file
can implement or override according to what interfaces it implements and
classes it extends. From the list of methods you can then choose which you
want to implement or override, and the appropriate method stub will be inserted
into the file.

The first step in the process is to execute **:JavaImpl** which will open a Vim
window containing a list possible methods to implement / override and the
interface / class which defines those methods.

Here is some example content from a class that extends java.io.InputStream\:

.. code-block:: java

  com.test.TestStream

  package java.io;
  public class InputStream
    public int read ()
      throws IOException
    public int read (byte[] b)
      throws IOException
    public int read (byte[] b, int off, int len)
      throws IOException
    public long skip (long n)
      throws IOException
    public int available ()
      throws IOException
    public void close ()
      throws IOException
    public void mark (int readlimit)
    public void reset ()
      throws IOException
    public boolean markSupported ()

  package java.io;
  public interface Closeable
    public void close ()
      throws IOException

  package java.lang;
  public class Object
    public int hashCode ()
    public boolean equals (Object obj)
    protected Object clone ()
      throws CloneNotSupportedException
    public String toString ()
    protected void finalize ()
      throws Throwable

From the newly opened window you can select a method to generate a stub for by
simply hitting <enter> with the cursor over the method signature.

If you would like to generate stubs for all methods in an interface or class,
then simply hit <enter> with the cursor over the class name and stub methods
will be created for each method in that class or interface.

This functionality is currently supported for both outer and inner classes, but
not for anonymous inner classes.  To view the list of methods to override for an
inner class, simply execute **:JavaImpl** with the cursor somewhere in the inner
class.

.. _\:JavaDelegate:

Delegate Methods
----------------

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

This functionality is currently supported for both outer and inner classes, but
not for anonymous inner classes.
