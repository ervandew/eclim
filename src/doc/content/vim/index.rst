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

.. _vim/index:

Introduction
=============

As stated in the project description, eclim's main goal is to bring Eclipse
functionality to the Vim editor.  Here you will find documentation on the
functionality that eclim provides.

The eclim documentation is broken up into two parts.  The first is the
comprehensive documentation which details the usage and purpose of every
command and setting.  This portion is found under the "Documentation" sub-menu
on the left.  Here you will find configuration information
(:ref:`settings <vim/settings>`, :ref:`suggested mappings <vim/mappings>`,
etc.), and the detailed functionality documentation which is broken up into
categories (common functionality, java development functionality, etc.).

The second portion of the documentation consists of a set of guides which are
written to help you perform specific tasks.  These guides are located under the
"Guides" sub-menu to the left.

Before diving in, you should first familiarize yourself with the following
commands.  After that, take a look at the docs for
:ref:`managing projects <vim/common/project>`.  Then feel free to move onto
the other categories.

.. _\:PingEclim:

- **:PingEclim** -
  Pings eclimd to see if it is up and running.

.. _\:ShutdownEclim:

- **:ShutdownEclim** -
  Shuts down the current running eclimd instance.

.. _\:EclimSettings:

- **:EclimSettings** -
  Allows you to view / edit the global :ref:`settings <vim/settings>`.
  For project level settings see the :ref:`:ProjectSettings` command on the
  :ref:`project documentation page <vim/common/project>`.

.. _\:EclimDisable:

- **:EclimDisable** -
  Allows you to temporarily disable all communication with eclimd for the
  current vim session.  Useful if you need to shutdown eclimd for one reason or
  antoher, and would like to disable vim's attempts to communicate with the
  non-existant server.

.. _\:EclimEnable:

- **:EclimEnable** -
  Re-enables communication with eclimd (the converse of **:EclimDisable**).

.. _\:EclimHelp:

- **:EclimHelp** [<topic>] -
  Similar to vim's :help command, with the exception that this command is
  limited to opening topics for eclim.

.. _\:EclimHelpGrep:

- **:EclimHelp** /<pattern>/ -
  Command which allows you to search the eclim help files via vimgrep.

  Ex.

  ::

    :EclimHelpGrep /completion/
