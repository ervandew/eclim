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
settings. There are two types of settings available:

Eclim daemon settings
---------------------

These are settings that reside in your Eclipse workspace or project and are used
to configure the behavior of the eclim daemon, independent of which client you
use (vim, emacs, etc).

These settings can be viewed and modified using one of the following commands:

- :ref:`:WorkspaceSettings`: Edit non-project dependent settings, or define
  defaults for settings not overridden at the project level.
- :ref:`:ProjectSettings`: Define the settings for the current project.

Vim client settings
-------------------

These are the settings you use to control how vim (the primary eclimd client)
behaves. You can edit these settings one of two ways:

- :ref:`:VimSettings`: Allows you to view and edit all of eclim's vim client
  settings. Settings edited here will be stored at
  ``$HOME/.eclim/.eclim_settings``.
- Alternately, you can add the equivalent ``g:EclimXXX`` setting to your vimrc
  or an ftplugin file, and that setting will take precedence over any value set
  using **:VimSettings**.
