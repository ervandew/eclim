.. Copyright (C) 2005 - 2010  Eric Van Dewoestine

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

.. _vim/settings:

Settings
=============

Certain aspects of eclim can be controlled by modifying one or more
settings.

There are two types of settings available:


- **Eclim global / project settings.**

  These are settings that reside in your Eclipse workspace and are used to
  control certain aspects of the eclim server's behavior.  These settings
  can be viewed and modified using one of the following commands\:
  - :ref:`:EclimSettings`
  - :ref:`:ProjectSettings`

- **Vim global variable settings.**

  These are your typical global Vim variables which can be set within your
  vimrc file.

Givin these two types, you may be ask, why do we need two? Or, when a new
setting is added, how do you decide which type to make it?

The reasoning behind having two types is that there are some settings that may
vary from one project to another.  For instance, I may have one project that
can be used in jdk 1.3 and utilizes log4j for logging, while another project of
mine requires jdk 1.4 and utilizes slf4j for logging.  Instances like this
require that each project be capable of storing their own settings.  Rather
than reinvent this support in Vim, we utilize Eclipse's built in preferences
system.

If the Eclipse preferences system can store project level and global settings,
why not make all the eclim settings of this type?  Well, the downside to
Eclipse preferences system is that an Eclipse instance must be running to
obtain the value of that preference.  Eclim however, requires access to many
settings, regardless of whether Eclipse is running or not.  So, to ensure that
these settings are always available, we utilize the standard Vim global
variable support.

When adding a new setting, deciding between an Eclipse preference or a Vim
global variable is a matter of answering the following\:

- Will this setting vary from one project to another?

  Yes: Add this setting an Eclipse preference.

- Does eclim need access to this setting regardless of whether an Eclipse
  instance is running or not?

  Yes: Add this setting a Vim global variable.


Eclim global / project settings
-------------------------------

The following is a list of the common global / project settings
available.

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


Vim global variables
--------------------

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
