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

Introduction
=============

As stated in the project description, eclim's main goal is to bring Eclipse
functionality to the Vim editor.  Here you will find documentation on the
functionality that eclim provides.

.. note::

  For installation instructions please see the
  :doc:`install guide </guides/install>`.

The eclim documentation is broken up into two parts.  The first is the
comprehensive documentation which details the usage and purpose of every
command and setting.  This portion is found under the :doc:`Documentation
</documentation>` sub-menu.  Here you will find configuration information, and
the detailed functionality documentation which is broken up into categories
(common functionality, java development functionality, etc.).

The second portion of the documentation consists of a set of guides which are
written to help you perform specific tasks.  These guides are located under the
:doc:`Guides </guides/index>` sub-menu.

Core Commands
-------------

Before diving in, you should first familiarize yourself with the following
commands. After that, take a look at the docs for :doc:`managing projects
</vim/project>`.  Then feel free to move on to the other categories.

.. _\:PingEclim:

- **:PingEclim** -
  Pings eclimd to see if it is up and running.

.. _\:ShutdownEclim:

- **:ShutdownEclim** -
  Shuts down the current running eclimd instance.

.. _\:EclimSettings:

- **:EclimSettings** -
  Allows you to view / edit the global :doc:`settings </vim/settings>`.
  For project level settings see the :ref:`:ProjectSettings` command on the
  :doc:`project documentation page </vim/project>`.

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

- **:EclimHelpGrep** /<pattern>/ -
  Command which allows you to search the eclim help files via vimgrep.

  Ex.

  ::

    :EclimHelpGrep /completion/

Configuration
-------------

:doc:`Eclim Settings </vim/settings>`

.. _org.eclim.user.name:

- **org.eclim.user.name**
  Should be set to your name. Used by various commands that add contact or
  author information to a file.

.. _org.eclim.user.email:

- **org.eclim.user.email**
  Should be set to the email address where you can be contacted.  Used by
  various commands that add contact or author information to a file.

.. _org.eclim.project.version:

- **org.eclim.project.version**
  Should be set to the version number of your project.  This is used by various
  commands that add version info to a file or utilize the version number in
  some other manner.

  Defaults to "1.0".

:doc:`Vim Settings </vim/settings>`

The following is a list of some of the common Vim variables available.

.. _g\:EclimLogLevel:

- **g:EclimLogLevel** (Default: 5)

  Much like the Vim 'verbose' option, this variable allows you to
  control the level of output from eclim as follows\:

  - <= 0: No output.
  - >= 1: Fatal errors.
  - >= 2: Errors.
  - >= 3: Warning messages.
  - >= 4: Info messages.
  - >= 5: Debug messages.
  - >= 6: Trace messages.

  Each level also has a corresponding variable to set the highlighting group
  used for the text.

  .. _g\:EclimFatalHighlight:

  - **g:EclimFatalHighlight** (Default: "Error")

  .. _g\:EclimErrorHighlight:

  - **g:EclimErrorHighlight** (Default: "Error")

  .. _g\:EclimWarningHighlight:

  - **g:EclimWarningHighlight** (Default: "WarningMsg")

  .. _g\:EclimInfoHighlight:

  - **g:EclimInfoHighlight** (Default: "Statement")

  .. _g\:EclimDebugHighlight:

  - **g:EclimDebugHighlight** (Default: "Normal")

  .. _g\:EclimTraceHighlight:

  - **g:EclimTraceHighlight** (Default: "Normal")

.. _g\:EclimSignLevel:

- **g:EclimSignLevel** (Default: 5)

  Behaves just like **g:EclimLogLevel** except this applies
  to placing of Vim signs for displaying validation errors / warnings,
  or marking :[vim]grep matches.

  The resulting signs also use the same highlighting variables above.

.. _g\:EclimEchoHighlight:

- **g:EclimEchoHighlight** (Default: "Statement")

  Determines which highlight group will be used for informative
  messages.

.. _g\:EclimBrowser:

- **g:EclimBrowser** (Default: Dependent on OS)

  Configures the external web browser to use when opening urls.
  By default eclim will attempt to set a default browser based on your
  system, but if it cannot find a compatible browser, you will need to
  set one in your vimrc.

  - | Firefox
    | let g:EclimBrowser = 'firefox'
  - | Mozilla
    | let g:EclimBrowser = 'mozilla'
  - | Opera
    | let g:EclimBrowser = 'opera'
  - | IE
    | let g:EclimBrowser = 'iexplore'

  Note: The above examples assume that the browser executable is in your path.
  On windows machines they won't be by default, so you will need to add them.

.. _g\:EclimShowCurrentError:

- **g:EclimShowCurrentError** (Default: 1)

  This variable determines whether or not a CursorHold autocommand is
  created that will echo the error associated with the current line if
  any error exists.  Setting this variable to 0 disables this feature.

.. _g\:EclimMakeLCD:

- **g:EclimMakeLCD** (Default: 1)

  When set to a non-0 value, all eclim based make commands (:Ant, :Maven, :Mvn,
  etc) will change to the current file's project root before executing.

  Enabling this has the benefit of allowing you to run these commands from any
  file regardless of where it was opened from without having to worry about the
  directory it is executing from.  For example if you have a file open from
  project A and split a file from project B, you can execute **:Ant** from the
  project B file and it will utilize project B's build.xml even though your
  current working directory is in project A.

.. _g\:EclimMenus:

- **g:EclimMenus** (Default: 1)

  When set to a non-0 value, enabled auto generation of gvim menus (under
  Plugin.eclim) for each eclim command available for the current buffer.
