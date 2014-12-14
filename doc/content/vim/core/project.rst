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

Eclipse Project Management
==========================

The core concept in most IDEs is the that of a project, and Eclipse is no
different.  Since a project must exist before you can perform any of the more
interesting tasks, eclim provides a set of commands to create and manage
projects from within Vim.

For the commands below that accept a project name as an argument, you may use
Vim's command line completion to complete the project name.

.. code-block:: vim

  :ProjectSettings a_p<Tab>
  :ProjectSettings a_project


.. _\:ProjectCreate:

- **:ProjectCreate**
  <folder> [-p <project_name>]
  -n <nature> ...
  [-d <project_dependency> ...]

  - **-p**: Optional argument used to specify the project
    name.  If omitted, eclim will use the last segment of the project's
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

.. _\:ProjectImport:

- **:ProjectImport** <folder>

  If you have an existing eclipse project folder which does not exist as a
  project in your current workspace, you can import that project using this
  command:

  .. code-block:: vim

    :ProjectImport ~/workspace/some_project

- **:ProjectImportDiscover** <folder>

  If you have several existing eclipse projects within a parent directory that
  you'd like to import, you can do so using this command, which scans the
  supplied folder for all ``.project`` files and imports the corresponding
  project into your current workspace:

  .. code-block:: vim

    :ProjectImport ~/some/parent/dir

.. _\:ProjectList:

- **:ProjectList**

  Simply echo a list of available projects.

.. _\:ProjectSettings:

- **:ProjectSettings** [<project>]

  Opens a window with the project's available settings. If a project name is
  supplied with this command, then the settings for that project are opened.  If
  no project name is supplied, and the current file is in a project directory,
  then the settings for the current project will be opened.

  In the resulting window you can modify the values and save the changes by
  simply writing the file in the usual Vim manner (:w). The format of the
  buffer is in the standard properties file format as supported by java, so all
  the same rules apply when editing. You can refer to the
  :doc:`settings </vim/settings>` documentation for a description of the
  available settings.

.. _\:ProjectDelete:

- **:ProjectDelete** <project>

  Deletes the project with the specified name.

.. _\:ProjectRename:

- **:ProjectRename** [<project>] <name>

  Renames a project.  If two arguments are supplied then the first argument is
  interpreted as the name of the project to rename and the second argument as
  the new name for the project.  When only a single argument is supplied, then
  that argument is used as the new name for the project which the current file
  belongs to.

.. _\:ProjectMove:

- **:ProjectMove** [<project>] <dir>

  Moves a project to the specified directory.  If two arguments are supplied
  then the first argument is interpreted as the name of the project to move and
  the second argument as the directory to move the project to.  When only a
  single argument is supplied, then that argument is used as the directory to
  move the current project to.

  .. warning::

    **:ProjectMove**, and possibly **:ProjectRename**, will result in the
    renaming of your project's directory in the underlying file system.  Eclim
    will do its best to reload any files that have moved as a result of the
    directory renaming and adjust your current working directory if necessary,
    but only for the current vim session.  If you have other vim sessions open
    with files from the project, then eclim will be unable to reload those files
    in those sessions for you, so you will have to do so manually.  A best
    practice would be to close any other vim sessions that might be affected by
    the moving or renaming of a project.

  .. note::

    When open files have moved as a result of **:ProjectMove** or
    **:ProjectRename**, eclim will reload those files in the current session, but
    it must do so via an :edit, which means that vim's undo tree will be lost.
    However, you will still have access to the eclipse
    :doc:`history </vim/core/history>`.

.. _\:ProjectRefresh:

- **:ProjectRefresh** [<project> <project> ...]

  Refreshes the supplied list of named projects by synchronizing each project
  against the current files on disk. If no projects names are supplied, refresh
  the current project. Useful when files may have been added, removed, or
  updated by a secondary application, like a version control system (cvs,
  subversion, etc).

.. _\:ProjectRefreshAll:

- **:ProjectRefreshAll**

  Refreshes all projects.

.. _\:ProjectInfo:

- **:ProjectInfo** [<project>]

  Echo info about the current or supplied project.

  .. _g\:EclimProjectStatusLine:

  Eclim supports displaying info about the current project in vim's status line
  by adding a call to ``eclim#project#util#ProjectStatusLine()`` to your
  statusline option:

  .. code-block:: vim

    set statusline=%<%f\ %M\ %h%r%=%-10.(%l,%c%V\ %{eclim#project#util#ProjectStatusLine()}%)\ %P

  By default, this will just include the project name, but you can customize the
  output by setting g:EclimProjectStatusLine:

  .. code-block:: vim

    let g:EclimProjectStatusLine = 'eclim(p=${name}, n=${natures})'

.. _\:ProjectOpen:

- **:ProjectOpen** [<project>]

  Opens a closed project.

.. _\:ProjectClose:

- **:ProjectClose** [<project>]

  Closes the current or supplied project. According to the Eclipse
  documentation, closing unused projects can reduce the amount of memory used,
  and may improve performance when building projects.

.. _\:ProjectNatures:

- **:ProjectNatures** [<project>]

  Echo list of natures for the supplied project name or for all projects if no
  project name specified.

.. _\:ProjectNatureAdd:

- **:ProjectNatureAdd** <project> [<nature> ...]

  Adds one or more natures to a project. Supports command line completion of
  nature names.

.. _\:ProjectNatureRemove:

- **:ProjectNatureRemove** <project> [<nature> ...]

  Removes one or more natures from a project. Supports command line
  completion of nature names.

.. _\:ProjectProblems:

- **:ProjectProblems[!]** [<project>]
  Populates vim's quickfix with a list of all eclipse build errors and warnings
  for the current, or specific project, and all related projects.  Very
  similar to eclipse's "Problems" view.  By default, if the current quickfix
  list represents a problems list, then as you save source files this list will
  be updated accordingly.

  Appending '!' limits the problem results to just errors.

  .. note::

    Problems are only reported for those projects that have an associated
    builder in their .project file.  If a project is not reporting errors,
    first check that a proper builder is present in the .project file.  For
    java projects created via eclim prior to eclim 1.5.2, the java builder may
    be missing, so you'll need to recreate the project, at which time eclim
    will add the java builder.

  **Configuration**

  :doc:`Vim Settings </vim/settings>`

  .. _g\:EclimQuickFixOpen:

  - **g:EclimQuickFixOpen** (Default: 'botright copen')

    Specifies the command used to open the quickfix window when executing the
    :ref`:ProjectProblems` command.

  .. _g\:EclimProjectProblemsUpdateOnSave:

  - **g:EclimProjectProblemsUpdateOnSave** (Default: 1)

    When non 0, indicates that the problems list should be updated when saving
    source files, but only if the quickfix list currently represents a problems
    list.

  - **g:EclimProjectProblemsUpdateOnBuild** (Default: 1)

    When non 0, indicates that the problems list should be updated after a
    :ProjectBuild, but only if the quickfix list currently represents a
    problems list.

.. _\:ProjectBuild:

- **:ProjectBuild** [<project>]

  Builds the current or supplied project.

.. _\:ProjectRun:

- **:ProjectRun** [<launch_config_name>]

  Runs an eclipse launch configuration. When no launch configuration name is
  supplied, the first available launch configuration will be used.

  Before you can run this command, you must first start vim with the
  ``--servername`` argument (eclimd currently sends the process stdout and
  stderr to vim using vim's remote invocation support):

  ::

    $ vim --servername run ...

  When your launch configuration is executed, a temporary buffer will be opened
  in vim to display any output. When you close this window the launch
  configuration process will be terminated. You can also terminate the process
  from within the temporary output buffer by running the ``:Terminate`` command
  (only available from that window). Also note that if you close vim, any
  running launch configurations will be terminated.

.. _\:ProjectRunList:

- **:ProjectRunList**

  Print a list of available eclipse launch configurations for the current
  project.

.. _\:ProjectCD:

- **:ProjectCD**

  Changes the global current working directory to the root directory of the
  current file's project (executes a :cd).

.. _\:ProjectLCD:

- **:ProjectLCD**

  Changes the current working directory of the current window to the root
  directory of the current file's project (executes a :lcd).

.. _\:ProjectTree:

- | **:ProjectTree** [<project> <project> ...]
  | **:ProjectTree** <dir> [<dir> <project> ...]

  Opens a window containing a navigable tree for the root directory of one or
  more projects.  If no arguments are supplied, the resulting tree is for the
  current project.  Otherwise, the tree contains multiple root nodes, one for
  each project root directory. The command also supports one or more arbitrary
  directories as arguments as well should you want to open a tree for a project
  not managed by eclim/eclipse (Note: the last part of the path will be used as
  the project's name).

  **Available key mappings in project tree window.**

  - **<cr>** -
    Toggles expansion / collapsing of a directory, or executes the first
    available action for a file.
  - **E** -
    Opens the current file using 'edit' in the content window.
  - **S** -
    Opens the current file in a new split.
  - **|** (pipe) -
    Opens the current file in a new vertical split.
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
  - **~** -
    Changes the root node to the current user's home directory.
  - **C** -
    Changes the root node to the directory under cursor.
  - **B** -
    Changes the root node to the parent directory of the current root node.
  - **K** -
    Changes the root node to the root path which will be either the project
    root or the file system root.
  - **p** -
    Moves the cursor to the parent of the node under the cursor.
  - **P** -
    Moves the cursor to the last child of the nearest open directory.
  - **:CD** <dir> -
    Changes the root to the supplied directory.
  - **D** -
    Prompts you for a directory name to create, pre-filled with the directory
    path in the tree where this mapping was executed.
  - **F** -
    Prompts you for a new or existing filename to open, pre-filled with the
    directory path in the tree where this mapping was executed.
  - **Y** - Yanks the path of the current file/directory to your clipboard.
  - **\?** - View the help buffer

  **Configuration**

  :doc:`Vim Settings </vim/settings>`

  .. _g\:EclimProjectTreeAutoOpen:

  - **g:EclimProjectTreeAutoOpen** (Default: 0)

    When non 0, a project tree window will be auto opened for new Vim
    sessions or new tabs in existing sessions if the current file is in a
    project.

  .. _g\:EclimProjectTreeAutoOpenProjects:

  - **g:EclimProjectTreeAutoOpenProjects** (Default: ['CURRENT'])

    List of project names that will be in the project tree when it is auto
    opened.  The special name 'CURRENT' represents the current project of
    the file being loaded in Vim when the tree is auto opened.

  .. _g\:EclimProjectTreeExpandPathOnOpen:

  - **g:EclimProjectTreeExpandPathOnOpen** (Default: 0)

    When non 0, the path of the current file will be expanded in the project tree
    when the project tree window is opened.

  .. _g\:EclimProjectTreePathEcho:

  - **g:EclimProjectTreePathEcho** (Default: 1)

    When non 0, the root relative path of the node under the cursor will be
    echoed as you move the cursor around.

  .. _g\:EclimProjectTreeSharedInstance:

  - **g:EclimProjectTreeSharedInstance** (Default: 1)

    When non 0, a tree instance with the same list of projects will be shared
    across vim tabs.  This allows you to have the same project tree open in
    several tabs all with the same state (with the exception of folds).

  .. _g\:EclimProjectTreeActions:

  - **g:EclimProjectTreeActions**

    Default\:

    .. code-block:: vim

      let g:EclimProjectTreeActions = [
          \ {'pattern': '.*', 'name': 'Split', 'action': 'split'},
          \ {'pattern': '.*', 'name': 'Tab', 'action': 'tabnew'},
          \ {'pattern': '.*', 'name': 'Edit', 'action': 'edit'},
        \ ]

    List of mappings which link file patterns to the available actions for
    opening files that match those patterns.  Note that the first mapping is the
    list is used as the default (<cr>).

  .. note::

    ProjectTree honors vim's 'wildignore' option by filtering out files
    matching those patterns from the tree. The 'A' mapping will toggle the
    display of those files along with other hidden files and directories.

.. _\:ProjectTreeToggle:

- **:ProjectTreeToggle**

  Toggles (opens/closes) the project tree for the current project.

.. _\:ProjectsTree:

- **:ProjectsTree**

  Similar to **:ProjectTree** but opens a tree containing all projects.

.. _\:ProjectTab:

- | **:ProjectTab** <project>
  | **:ProjectTab** <dir>

  Command to initialize a new vim tab with the project tree open and the tab
  relative working directory set to the project root.  This allows you to work
  on multiple projects within a single vim instance where each project is
  isolated to its own tab. The command also supports an arbitrary directory as
  an argument instead of a project name should you want to open a tab for a
  project not managed by eclim/eclipse (Note: the last part of the path will be
  used as the project's name).

  **Configuration**

  :doc:`Vim Settings </vim/settings>`

  .. _g\:EclimProjectTabTreeAutoOpen:

  - **g:EclimProjectTabTreeAutoOpen** (Default: 1)

    When non 0, the project tree window will be auto opened on the newly created tab.

.. _\:ProjectGrep:

- **:ProjectGrep** /<pattern>/ <file_pattern> [<file_pattern> ...]

  Executes vimgrep using the supplied arguments from the root of the
  current project allowing you to run a project wide grep from any file
  within the project.

.. _\:ProjectGrepAdd:

- **:ProjectGrepAdd** /<pattern>/ <file_pattern> [<file_pattern> ...]

  Just like **:ProjectGrep** but using vimgrepadd instead.

.. _\:ProjectLGrep:

- **:ProjectLGrep** /<pattern>/ <file_pattern> [<file_pattern> ...]

  Just like **:ProjectGrep** but using lvimgrep instead.

.. _\:ProjectLGrepAdd:

- **:ProjectLGrepAdd** /<pattern>/ <file_pattern> [<file_pattern> ...]

  Just like **:ProjectGrep** but using lvimgrepadd instead.

.. _\:ProjectTodo:

- **:ProjectTodo**

  Searches all the source files in the project (those with extensions included
  in :ref:`g:EclimTodoSearchExtensions`) for the fixme / todo pattern (defined
  by :ref:`g:EclimTodoSearchPattern`) and adds all occurrences to the current
  location list.

.. _\:Todo:

- **:Todo**

  Just like :ProjectTodo, but limits the searching to the current file.

  **Configuration**

  :doc:`Vim Settings </vim/settings>`

  .. _g\:EclimTodoSearchPattern:

  - **g:EclimTodoSearchPattern**

    Default\:

    .. code-block:: vim

      let g:EclimTodoSearchPattern = '\(\<fixme\>\|\<todo\>\)\c'

    Defines the regex pattern used to identify todo or fixme entries.

  .. _g\:EclimTodoSearchExtensions:

  - **g:EclimTodoSearchExtensions**

    Default\:

    .. code-block:: vim

      let g:EclimTodoSearchExtensions = ['java', 'py', 'php', 'jsp', 'xml', 'html']

    Defines a list of file extensions that will be searched for the todo / fixme
    entries.
