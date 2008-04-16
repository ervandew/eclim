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

.. _vim/java/impl:

Method Override / Impl
======================

.. _JavaImpl:

This functionality gives you the ability to view all the methods that the
current source file can implement or override according to what interfaces it
implements and classes it extends.  From the list of methods you can then choose
which you want to implement or override, and the appropriate method stub will be
inserted into the file.

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

.. note::

  The insertion of method stubs is done externally with Eclipse and with
  that comes a couple :ref:`caveats <vim/issues>`.

This functionality is currently supported for both outer and inner classes, but
not for anonymous inner classes.  To view the list of methods to override for an
inner class, simply execute **:JavaImpl** with the cursor somewhere in the inner
class.
