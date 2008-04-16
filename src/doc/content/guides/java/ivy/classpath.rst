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

.. _guides/java/ivy/classpath:

Ivy: Maintain Classpath
=======================

For users of ivy_, eclim provides support for auto updating the ``.classpath``
for your project every time you save your ivy.xml file.  Any entries found in
the ivy.xml that are not in the ``.classpath`` will be added, any entries that
differ in version will be updated, and any stale entries deleted.


.. _IvyRepo:

Initial Setup
-------------

Before you can start utilizing this auto updating support, you must first set
the location of your ivy repository (ivy cache).  This is the directory where
ivy will download the dependencies to and where eclipse will then pick them up
to be added to your project's classpath.

To set the repository location you can use the **:IvyRepo** command which is
made available when editing an ivy.xml file.

.. code-block:: vim

  :IvyRepo /path/to/ivy/repository


If you fail to set this prior to writing the ivy.xml file, eclim will emit an
error notifying you that you first need to set the IVY_REPO variable via this
command.


Updating .classpath
-------------------

Once you have performed the :ref:`initial setup <ivyrepo>`, updating the
Eclipse ``.classpath`` file is as easy as saving your ivy.xml file (:w) and
letting eclim do the rest.


Preserving manually added entries
---------------------------------

When utilizing the ivy support, eclim will attempt to remove any stale entries
from your .classpath file.  If you have some manually added entries, these may
be removed as well.  To prevent this you can add a classpath entry attribute
notifying eclim that the entry should be preserved.

Ex.

.. code-block:: xml

  <classpathentry kind="lib" path="lib/j2ee-1.4.jar">
    <attributes>
      <attribute name="eclim.preserve" value="true"/>
    </attributes>
  </classpathentry>


.. _IvyDependencySearch:

Search Online Maven Repository
------------------------------

Eclim also provides the command **:IvyDependencySearch** which allows you to
search for dependencies in the online maven repository as described in the
:ref:`maven documentation <vim/java/maven/dependencies>`.

.. _ivy: http://jayasoft.org/ivy
