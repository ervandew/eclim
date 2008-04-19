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

.. _vim/common/project:

Eclipse Projects
================

The core concept in most IDEs is the that of a project, and Eclipse is no
different.  Since a project must exist before you can perform any of the more
interesting tasks, eclim provides a set of commands to create and manage
projects from within Vim.

.. note::

  For the commands below that accept a project name as an argument, you may use
  Vim's command line completion to complete the project name.

  ``:ProjectSettings a_p<Tab>``

  ``:ProjectSettings a_project``

.. _ProjectCreate:

- **:ProjectCreate**
  <folder> [-p <project_name>]
  -n <nature> ...
  [-d <project_dependency> ...]

  - **-p**: Optional argument used to specify the project
    name.  If ommitted, eclim will use the last segment of the project's
    path, with any spaces replaced with underscores, as the project name.
  - **-n**: Required argument which specifies a space
    separated list of project natures (java, php, etc.) to add to the
    project.  If you want to create a project with no natures, you can
    use the word "none" here.

    .. code-block:: vim

      :ProjectCreate ~/workspace/test -n none

    Note that eclim supports command completion of available nature
    names.

    .. code-block:: vim

      :ProjectCreate ~/workspace/test -n p<tab>
      :ProjectCreate ~/workspace/test -n php

  - **-d**: Optional argument used to specify a space
    separated list of project names which the project you're creating
    depends on.

  **Some Examples**

  .. code-block:: vim

    :ProjectCreate ~/projects/a_project -n java
    :ProjectCreate ~/projects/a_project -n java -d another_project yet_another_project
    :ProjectCreate ~/projects/a_project -n java php -p My\ Project\ Name

.. _ProjectList:

- **:ProjectList**

  Simply echos a list of available projects.

.. _ProjectSettings:

- **:ProjectSettings** [<project>]

  Opens a window with the project's available settings. If a project name is
  supplied with this command, then the settings for that project are opened.  If
  no project name is supplied, and the current file is in a project directory,
  then the settings for the current project will be opened.

  In the resulting window you can modify the values and save the changes by
  simply writing the file in the usual Vim manner (:w). The format of the
  buffer is in the standard properties file format as supported by java, so all
  the same rules apply when editing. You can refer to the
  :ref:`settings <vim/settings>` documentation for a description of the
  available settings.

.. _ProjectDelete:

- **:ProjectDelete** <project>

  Deletes the project with the specified name.

.. _ProjectRefresh:

- **:ProjectRefresh** [<project> <project> ...]

  Refreshes the supplied list of named projects by synchronizing each project
  against the current files on disk. If no projects names are supplied, refresh
  the current project. Useful when files may have been added, removed, or
  updated by a secondary application, like a version control system (cvs,
  subversion, etc).

.. _ProjectRefreshAll:

- **:ProjectRefreshAll**

  Refreshes all projects.

.. _ProjectInfo:

- **:ProjectInfo** [<project>]

  Echos info about the current or supplied project.

.. _ProjectOpen:

- **:ProjectOpen** <project>

  Opens a closed project.

.. _ProjectClose:

- **:ProjectClose** <project>

  Closes a project. According to the Eclipse documentation, closing unused
  projects can reduce the amount of memory used, and may improve performance
  when building projects.

.. _ProjectNatures:

- **:ProjectNatures** [<project>]

  Echos list of natures for the supplied project name or for all projects if no
  project name specified.

.. _ProjectNatureAdd:

- **:ProjectNatureAdd** <project> [<nature> ...]

  Adds one or more natures to a project. Supports command line completion of
  nature names.

.. _ProjectNatureRemove:

- **:ProjectNatureRemove** <project> [<nature> ...]

  Removes one or more natures from a project. Supports command line
  completion of nature names.

.. _ProjectCD:

- **:ProjectCD**

  Changes the global current working directory to the root directory of the
  current file's project (executes a :cd).

.. _ProjectLCD:

- **:ProjectLCD**

  Changes the current working directory of the current window to the root
  directory of the current file's project (executes a :lcd).

.. _ProjectTree:

- **:ProjectTree** [<project> <project> ...]

  Opens a window containing a navigable tree for the root directory of one or
  more projects.  If no arguments are supplied, the resulting tree is for the
  current project.  Otherwise, the tree contains multiple root nodes, one for
  each project root directory.

  **Available key mappings in project tree window.**

  - **<cr>** -
    Toggles expansion / collapsing of a directory, or executes the first
    available action for a file.
  - **E** -
    Opens the current file using 'edit' in the content window.
  - **S** -
    Opens the current file in a new split.
  - **T** -
    Opens the current file in a new tab.
  - **o** -
    Toggles folding / unfolding of a directory, or opens a window of available
    actions to be executed on the selected file.  Hitting <enter> on an entry in
    the action window will execute that action on the current file.
  - **s** -
    Executes **:shell** for the directory under the cursor or the parent
    directory of the file under the cursor.
  - **R** -
    Refreshes the current directory against the current state of the file
    system.
  - **A** -
    Toggles whether or not hidden files are displayed in the tree.
  - **H** -
    Changes the root node to the current user's home directory.
  - **C** -
    Changes the root node to the directory under cursor.
  - **B** -
    Changes the root node to the parent directory of the current root node.
  - **p** -
    Moves the cursor to the parent of the node under the cursor.
  - **P** -
    Moves the cursor to the last child of the nearest open directory.
  - **:CD** <dir> -
    Changes the root to the supplied directory.

.. _ProjectsTree:

- **:ProjectsTree**

  Similar to **:ProjectTree** but opens a tree containing all projects.

.. _ProjectGrep:

- **:ProjectGrep** /<pattern>/ file_pattern [file_pattern ...]

  Executes vimgrep using the supplied arguments from the root of the
  current project allowing you to run a project wide grep from any file
  within the project.

.. _ProjectGrepAdd:

- **:ProjectGrepAdd** /<pattern>/ file_pattern [file_pattern ...]

  Just like **:ProjectGrep** but using vimgrepadd instead.

.. _ProjectLGrep:

- **:ProjectLGrep** /<pattern>/ file_pattern [file_pattern ...]

  Just like **:ProjectGrep** but using lvimgrep instead.

.. _ProjectLGrepAdd:

- **:ProjectLGrepAdd** /<pattern>/ file_pattern [file_pattern ...]

  Just like **:ProjectGrep** but using lvimgrepadd instead.


Tracker
-------

.. _TrackerTicket:

- **:TrackerTicket** <ticket_id>

  Opens the supplied ticket via your web browser for the configured tracking
  system configureed via org.eclim.project.tracker_.


Configuration
-------------

Vim Settings

.. _EclimProjectTreeAutoOpen:

- **g:EclimProjectTreeAutoOpen** (Default: 0)

  When non 0, a project tree window will be auto opened for new Vim
  sessions or new tabs in existing sessions if the current file is in a
  project.

.. _EclimProjectTreeAutoOpenProjects:

- **g:EclimProjectTreeAutoOpenProjects** (Default: ['CURRENT'])

  List of project names that will be in the project tree when it is auto
  opened.  The special name 'CURRENT' represents the current project of
  the file being loaded in Vim when the tree is auto opened.

.. _EclimProjectTreeTaglistRelation:

- **g:EclimProjectTreeTaglistRelation** (Default: 'below')

  Used to define the relation to the taglist_ window if the taglist_ plugin is
  present.  Valid values include 'below' and 'above'.

.. _EclimProjectTreeWincmd:

- **g:EclimProjectTreeWincmd**

  (Default: Varies depending on presence of taglist_ plugin and its settings.)
  Vim command prepended to the split command used to open the project tree
  window (Ex. 'botright', 'botright vertical', etc.).

.. _EclimProjectTreeContentWincmd:

- **g:EclimProjectTreeContentWincmd**

  (Default: Varies depending on presence of taglist_ plugin and its settings.)
  Vim command used to move focus to the nearest content window where actions to
  open selected files will be executed from.  For instance, when issuing a
  split on a file from the project tree, you most likely do not want to split
  that file relative to the project tree, but rather split it relative to the
  file open in the window to the left or right of the project tree (Ex. 'winc
  h', 'winc l', etc).

.. _EclimProjectTreeWidth:

- **g:EclimProjectTreeWidth**

  (Default: Defaults to same width as taglist_ if present, otherwise defaults
  to 30.) Only used if the project tree is to be opened in a vertical window.

.. _EclimProjectTreeHeight:

- **g:EclimProjectTreeHeight**

  (Default: Defaults to same height as taglist_ if present, otherwise defaults
  to 10.) Only used if the project tree is to be opened in a horizontal window.

.. _EclimProjectTreeActions:

- **g:EclimProjectTreeActions**

  Default\:

  .. code-block:: vim

    let g:EclimProjectTreeActions = [
        \ {'pattern': '.*', 'name': 'Split', 'action': 'split'},
        \ {'pattern': '.*', 'name': 'Tab', 'action': 'tabnew'},
        \ {'pattern': '.*', 'name': 'Edit', 'action': 'edit'},
      \ ]

  Map of file patterns to the available actions for opening files that match
  that pattern.

Eclim Settings

.. _org.eclim.project.tracker:

- **org.eclim.project.tracker**
  Url used to view tickets in your ticket tracking software. This url supports
  the '<id>' placeholder which will be replaced with the ticket id.

  Ex. An example setting for a Trac installation\:

  ::

    org.eclim.project.tracker=http://somedomain.com/trac/ticket/<id>

  In addition to being used by :TrackerTicket, this setting is also used in
  conjunction with :ref:`:VcsLog <vcslog>` and
  :ref:`:VcsChangeSet <vcschangeset>` to enable linking of ticket ids of the
  form #ticket_id (#1234) found in user supplied commit comments.

.. _taglist: http://www.vim.org/scripts/script.php?script_id=273
