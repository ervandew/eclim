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

Java Code Inspection
===================

.. _\:JavaHierarchy:

Class / Interface Hierarchy
---------------------------

When viewing a java class or interface you can view the type hierarchy by
issuing the command **:JavaHierarchy**.  This will open a temporary buffer with
an inversed tree view of the type hierarchy with the current class / interface
at the root.

.. code-block:: java

  public class XmlCodeCompleteCommand
    public class WstCodeCompleteCommand
      public class AbstractCodeCompleteCommand
        public class AbstractCommand
          public interface Command

Inner classes / interfaces are also supported.  Just place the cursor on the
inner class / interface before calling **:JavaHierarchy**.

While you are in the hierarchy tree buffer, you can jump to the type under the
cursor using one of the following key bindings:

- <cr> - open the type using the
  (:ref:`default action <g:EclimJavaHierarchyDefaultAction>`).
- E - open the type via :edit
- S - open the type via :split
- T - open the type via :tabnew
- ? - view help buffer

Configuration
^^^^^^^^^^^^^

:doc:`Vim Settings </vim/settings>`

.. _g\:EclimJavaHierarchyDefaultAction:

- **g:EclimJavaHierarchyDefaultAction** (defaults to 'split') -
  Determines the command used to open the type when hitting <enter> on the type
  entry in the hierarchy buffer.
