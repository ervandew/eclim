:orphan:

.. Copyright (C) 2005 - 2014  Eric Van Dewoestine

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

Settings
========

Certain aspects of eclim can be controlled by modifying one or more
settings.

There are two types of settings available:

- **Global workspace / project settings.**

  These are settings that reside in your Eclipse workspace and are used to
  control certain aspects of the eclim server's behavior. These settings
  can be viewed and modified using one of the following commands:

  - :ref:`:WorkspaceSettings`
  - :ref:`:ProjectSettings`

- **Vim global variable settings.**

  These are your typical global Vim variables which can be set within your
  vimrc file.

Given these two types, you may be ask, why do we need two? Or, when a new
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
global variable is a matter of answering the following:

- Will this setting vary from one project to another?

  Yes: Add this setting an Eclipse preference.

- Does eclim need access to this setting regardless of whether an Eclipse
  instance is running or not?

  Yes: Add this setting a Vim global variable.
