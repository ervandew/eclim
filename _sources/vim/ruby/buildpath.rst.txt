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

Ruby Interpreters / Build Path
==============================

.. _\:RubyInterpreterAdd:
.. _\:RubyInterpreterRemove:
.. _\:RubyInterpreterList:

Interpreters
------------

When creating your first ruby project you will be prompted to configure a new
interpreter if you haven't already done so in eclipse. You can also manually
manage your ruby interpreters with the following commands:

- **:RubyInterpreterAdd** [-n <name>] <path> - Add a ruby interpreter.
- **:RubyInterpreterRemove** <path> - Remove a ruby interpreter.
- **:RubyInterpreterList**  - List the available ruby interpreters.

If you have more than one interpreter configured when you create subsequent
projects you will be prompted to choose the interpreter to use. If you remove an
interpreter used by one of your projects, you'll have to go back to that project
and edit its ``.buildpath`` file and change the interpreter name in the
container entry.

Example (wrapped for readability): Changing ``ruby1.9`` to ``ruby1.8``:

.. code-block:: xml

  <buildpathentry kind="con"
      path="org.eclipse.dltk.launching.INTERPRETER_CONTAINER/
            org.eclipse.dltk.internal.debug.ui.launcher.GenericRubyInstallType/ruby1.9"/>

.. code-block:: xml

  <buildpathentry kind="con"
      path="org.eclipse.dltk.launching.INTERPRETER_CONTAINER/
            org.eclipse.dltk.internal.debug.ui.launcher.GenericRubyInstallType/ruby1.8"/>

If there is no suffix on the container entry, that project will be using what
ever is he default interpreter:

.. code-block:: xml

   <buildpathentry kind="con"
       path="org.eclipse.dltk.launching.INTERPRETER_CONTAINER"/>

Build Path
----------

.. include:: /vim/dltk/buildpath.rst
   :start-after: begin-buildpath
   :end-before: end-buildpath
   :ref-suffix: ruby
