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

.. _guides/java/maven/maven/classpath:

Maven: Maintain Classpath
=========================

<a href="ext:maven">Maven</a> 1.x comes bundled with an Eclipse plugin
that allows you to easily maintain your .classpath file based on your
project.xml.  This guide will walk you through the steps of setting this
up for the first time and continual usage there after.

.. note::

  For additional information on the Eclipse plugin from maven, you may visit
  their `online documentation`_.


.. _MavenRepo:

Initial Setup
-------------

To initialize maven's support for updating the eclipse classpath you first need
to set the ``MAVEN_REPO`` variable in the Eclipse workspace by
executing the following command which is made available when editing the
project.xml file in vim:

.. code-block:: vim

  :MavenRepo


Updating .classpath
-------------------

Once you have performed the <a href="#setup">initial setup</a>, updating the
Eclipse ``.classpath`` file is as easy as executing the following at a command
line\:

::

  maven eclipse

or in Vim\:

.. code-block:: vim

  :Maven eclipse

.. _online documentation: http://maven.apache.org/maven-1.x/plugins/eclipse/
