.. Copyright (C) 2005 - 2013  Eric Van Dewoestine

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

Java Refactoring
================

.. _\:JavaRename:

Rename
-------

The first refactoring that eclim supports is **:JavaRename**, which can be used
to rename various java elements.

Example:

.. code-block:: java

  package org.foo.bar;

  public class Foo {
    public void bar(){
    }
  }

To rename the class from 'Foo' to 'Bar' you simply position the cursor over the
class name 'Foo' and execute:

.. code-block:: vim

  :JavaRename Bar

The result of which will the following prompt:

::

  Rename "Foo" to "Bar"
  ([e]xecute / [p]review / [c]ancel):

This prompt give you three options:

#. execute: Execute the refactoring without previewing the changes to be made.
   The current file and any other changed files open in the current instance of
   vim will be reloaded.

#. preview: Preview the changes that the refactoring will perform.  This will
   open a scratch buffer with a list of changes to be made along with a link at
   the bottom to execute the refactoring.

   The contents of the preview window will vary depending on what you are
   renaming.

   If we are renaming 'Foo' to 'Bar' the contents would be like so:

   ::

      other: Rename compilation unit 'Foo.java' to 'Bar.java'

     |Execute Refactoring|

   If we are renaming the method 'bar' to 'foo', the contents would look like
   so:

   ::

     |diff|: /home/someuser/workspace/test_java/src/org/foo/bar/Foo.java

     |Execute Refactoring|

   If the first instance, there is not much to preview.  Since this particular
   class is not referenced anywhere else, the only operation eclipse will
   perform, is to rename the file from 'Foo.java' to 'Bar.java' which will also
   update the class name in that file.

   In the second instance eclipse provides a preview of the actual changes to
   the file what will be performed.  If the method were referenced elsewhere,
   you would see an entry for each file that would be modified by the
   refactoring.  To actually for a vim diff split of the changes that will be
   performed, simple position the cursor on the diff entry and hit <enter>.

   Once you are satisfied with changes that eclipse will perform, you can then
   execute the refactoring by positioning the cursor over the "\|Execute
   Refactoring\|" link and hit <enter>.

#. cancel: Cancel the refactoring (Hitting enter without typing a choice or
   hitting Ctrl-C will also cancel the refactoring).

**Package Renaming**

Renaming a package is performed just like renaming any other element.  However,
the name you supply to the **:JavaRename** command must be the full package
name that you are renaming the package to.  For example.  In sample java file
above, if you place the cursor on the 'org' portion of the package declaration,
you can rename 'org' to 'com' by running ``:JavaRename com``.  If you want to
rename the 'foo' package to 'baz' you can do so by running ``:JavaRename
org.baz``.  Note that if you were to only supply the name 'baz', the 'foo'
package would be moved to the same level as 'org' and then renamed.

.. warning::

  When renaming a package, the associated directory will also be renamed in the
  underlying file system.  Eclim will do its best to reload any files that have
  moved as a result of the directory renaming and adjust your current working
  directory if necessary, but only for the current vim session.  If you have
  other vim sessions open with files located in the directory that is renamed,
  then eclim will be unable to reload those files in those sessions for you, so
  you will have to do so manually.  A best practice would be to close any other
  vim sessions that might be affected by the renaming of a package.

.. _\:JavaMove:

Move
----

Eclim also supports moving a top level class or interface from one package to
another using the **:JavaMove** command.

In this example the current file would be moved from its current package to the
package ``org.foo``:

.. code-block:: vim

  :JavaMove org.foo

Like the package renaming described in the previous section, the argument to
**:JavaMove** must be the full package name you want to move the current file
to.

.. include:: /vim/refactoring.rst
   :start-after: begin-refactor-undo-redo
   :end-before: end-refactor-undo-redo

.. include:: /vim/refactoring.rst
   :start-after: begin-refactor-config
   :end-before: end-refactor-config
   :ref-suffix: java

.. _eclim-user: http://groups.google.com/group/eclim-user
