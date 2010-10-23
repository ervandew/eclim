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

.. _vim/cheatsheet:

Cheatsheet
==========

Here you will find a comprehensive list of eclim commands that you can use as a
reference.


Global Commands
---------------

- :ref:`:PingEclim` - Pings eclimd server.
- :ref:`:ShutdownEclim` - Shuts down eclimd server.
- :ref:`:EclimSettings` - View / edit global settings.
- :ref:`:EclimDisable` - Temporarily disables communication with eclimd.
- :ref:`:EclimEnable` - Re-enables communication with eclimd.
- :ref:`:EclimHelp` [helptopic] - View eclim documentation in vim.
- :ref:`:EclimHelpGrep` /regex/ - Search the eclim documentation in vim.


Project Commands
----------------

- :ref:`:ProjectCreate`
  <folder> [-p <project_name>] -n <nature> ... [-d <project_dependency> ...] -
  Create a new project.
- :ref:`:ProjectImport` <folder> -
  Import a project from an existing eclipse project folder.
- :ref:`:ProjectList` - List current projects.
- :ref:`:ProjectSettings` [<project>] - View / edit project settings.
- :ref:`:ProjectDelete` <project> - Delete a project.
- :ref:`:ProjectRename` [<project>] <name> - Rename a project.
- :ref:`:ProjectMove` [<project>] <dir> - Move a project.
- :ref:`:ProjectRefresh` [<project> <project> ...] -
  Refresh supplied list of projects against the current files on disk.  If
  no project names supplied, refresh the current project.
- :ref:`:ProjectRefreshAll` - Refresh all projects.
- :ref:`:ProjectInfo` [<project>] -
  Echos info for the current or supplied project.
- :ref:`:ProjectOpen` [<project>] - Opens a project.
- :ref:`:ProjectClose` [<project>] - Closes a project.
- :ref:`:ProjectNatures` [<project>] -
  View the configured natures for one or all projects.
- :ref:`:ProjectNatureAdd` <project> [<nature> ...] -
  Add one or more natures to a project.
- :ref:`:ProjectNatureRemove` <project> [<nature> ...] -
  Remove one or more natures from a project.
- :ref:`:ProjectProblems` [<project>] -
  Populates vim's quickfix with all eclipse build errors and warnings for the
  current, or specified project, and all related projects.
- :ref:`:ProjectCD` -
  Changes the global current working directory to the root directory of the
  current file's project (executes a :cd).
- :ref:`:ProjectLCD` -
  Changes the current working directory of the current window to the root
  directory of the current file's project (executes a :lcd).
- :ref:`:ProjectTree` [<project> <project> ...] -
  Opens navigable a tree for one or more projects.
- :ref:`:ProjectsTree` - Opens a tree containing all projects.
- :ref:`:ProjectTab` <project> - Opens a new tab containing the project tree
  and the tab local working directory set to the supplied project's root.
- :ref:`:ProjectGrep` /<pattern>/ file_pattern [file_pattern ...] -
  Issues a vimgrep starting at the root of the current project.
- :ref:`:ProjectGrepAdd` /<pattern>/ file_pattern [file_pattern ...] -
  Issues a vimgrepadd starting at the root of the current project.
- :ref:`:ProjectLGrep` /<pattern>/ file_pattern [file_pattern ...] -
  Issues a lvimgrep starting at the root of the current project.
- :ref:`:ProjectLGrepAdd` /<pattern>/ file_pattern [file_pattern ...] -
  Issues a lvimgrepadd starting at the root of the current project.
- :ref:`:TrackerTicket` <ticket_id> -
  Opens the supplied ticket in your configured web based tracking tool.
- :ref:`:ProjectTodo` -
  Searches project files for todo / fixme patterns and adds them to the
  location list.
- :ref:`:Todo` -
  Searches the current file for todo / fixme patterns and adds them to the
  location list.


Ant Commands
------------

- :ref:`:Ant` [<target> ...] - Execute ant from the current project.
- :ref:`:AntDoc` [<element>] -
  Find and open the documentation for the element under the cursor or the
  element supplied as an argument.
- :ref:`:Validate <:Validate_ant>` - Validate the current ant build file.


C/C++ Commands
-----------------

- :ref:`:Validate <:Validate_c>` - Validate the current file.
- :ref:`:CSearch <:CSearch>` [-p <pattern> -t <type> -s <scope> -x <context>] -
  Search for classes, functions, methods, macros, etc.
- :ref:`:CSearchContext` -
  Find the element under the cursor based on its context.
- :ref:`:CProjectConfigs <:CProjectConfigs>` [project] -
  Open a temp buffer to view/modify the current projects cdt configurations.
- :ref:`:CCallHierarchy <:CCallHierarchy>` -
  Display the call hierarchy for the function or method under the cursor.


Css Commands
-----------------

- :ref:`:Validate <:Validate_css>` - Validate the current file.


Dtd Commands
-----------------

- :ref:`:Validate <:Validate_dtd>` - Validate the current file.


Html Commands
-----------------

- :ref:`:Validate <:Validate_html>` - Validate the current file.
- :ref:`:BrowserOpen` - Opens the current file in your configured browser.


Ivy Commands
-----------------

- :ref:`:IvyRepo <:IvyRepo>` <path> -
  Sets the necessary IVY_REPO classpath variable for supporting automatic
  updates to .classpath files upon ivy.xml updates.
- :ref:`:IvyDependencySearch <:IvyDependencySearch>` <artifact> -
  Searches online repository and opens a window with results that can be added
  to the current ivy file by hitting <Enter> on a result.  Available when
  editing an ``ivy.xml`` file.


Java Commands
-----------------

- :ref:`:JavaGet` - Create a java bean getter method.
- :ref:`:JavaSet` - Create a java bean setter method.
- :ref:`:JavaGetSet` - Create both a java bean getter and setter method.
- :ref:`:JavaConstructor` -
  Creates class constructor, either empty or based on selected class fields.
- :ref:`:JavaHierarchy` - View the type hierarchy tree.
- :ref:`:JavaImpl` -
  View implementable / overridable methods from super classes and implemented
  interfaces.
- :ref:`:JavaDelegate` -
  View list of methods that delegate to the field under the cursor.
- :ref:`:JUnitImpl <:JUnitImpl>` -
  Similar to **:JavaImpl**, but creates test methods.
- :ref:`:JUnitExecute <:JUnitExecute>` [testcase] -
  Allows you to execute test cases in your favorite build tool.
- :ref:`:JUnitResult <:JUnitResult>` [testcase] -
  Allows you to view the results of a test case.
- :ref:`:JavaImport` - Import the class under the cursor.
- :ref:`:JavaImportMissing` - Import all undefined types.
- :ref:`:JavaSearch <:JavaSearch>`
  [-p <pattern>] [-t <type>] [-x <context>] [-s <scope>] -
  Search for classes, methods, fields, etc.  (With pattern supplied, searches
  for the element under the cursor).
- :ref:`:JavaSearchContext` -
  Perform a context sensitive search for the element under the cursor.
- :ref:`:JavaCorrect` - Suggest possible corrections for a source error.
- :ref:`:JavaRegex` - Opens a window for testing java regular expressions.
- :ref:`:JavaDocSearch <:JavaDocSearch>` -
  Search for javadocs.  Same usage as **:JavaSearch**.
- :ref:`:JavaDocComment <:JavaDocComment>` -
  Adds or updates the comments for the element under the cursor.
- :ref:`:JavaRename <:JavaRename>` [new_name] -
  Rename the element under the cursor.
- :ref:`:Java <:Java>` -
  Executes the java using your project's main class.
- :ref:`:Javac <:Javac>` -
  Executes the javac utility against all source files.
- :ref:`:Javadoc <:Javadoc>` [file, file, ...] -
  Executes the javadoc utility against all or just the supplied source files.
- :ref:`:JavaListInstalls <:JavaListInstalls>` - List known JDK/JRE installs.
- :ref:`:JavaFormat` - Formats java source code.
- :ref:`:Checkstyle` - Invokes checkstyle on the current file.
- :ref:`:Jps` -
  Opens window with information about the currently running java processes.
- :ref:`:Validate <:Validate_java>` - Manually runs source code validation.


Java .classpath Commands
------------------------

- :ref:`:NewSrcEntry` <dir> [<dir> ...] - Add a new source dir entry.
- :ref:`:NewProjectEntry` <project> [<project> ...] - Add a new project entry.
- :ref:`:NewJarEntry` <file> [<file> ...] - Add a jar entry.
- :ref:`:NewVarEntry` <VAR/file> [<VAR/file> ...] - Add a new var entry.
- :ref:`:VariableList`
  List available classpath variables and their corresponding values.
- :ref:`:VariableCreate` <name> <path> -
  Creates or updates the variable with the supplied name.
- :ref:`:VariableDelete` <name> -
  Deletes the variable with the supplied name.


Javascript Commands
--------------------

- :ref:`:Validate <:Validate_javascript>` - Validate the current javascript file.


Log4j Commands
-----------------

- :ref:`:Validate <:Validate_log4j>` -
  Validate the current log4j xml configuration file.


Maven Commands
-----------------

- :ref:`:Maven` [<goal> ...] - Execute maven 1.x from the current project.
- :ref:`:Mvn` [<goal> ...] - Execute maven 2.x from the current project.
- :ref:`:MavenRepo <:MavenRepo>` -
  Sets the necessary MAVEN_REPO classpath variable for maven's (1.x) eclipse
  support.
- :ref:`:MvnRepo <:MvnRepo>` -
  Sets the necessary M2_REPO classpath variable for maven's (2.x) eclipse
  support.
- :ref:`:MavenDependencySearch` <artifact> -
  Searches online repository and opens a window with results that can be
  added to the current project file by hitting <Enter> on a result.
  Available when editing a maven 1.x ``project.xml`` file.
- :ref:`:MvnDependencySearch` <artifact> -
  Searches online repository and opens a window with results that can be
  added to the current pom file by hitting <Enter> on a result.
  Available when editing a maven 2.x ``pom.xml`` file.


Php Commands
-----------------

- :ref:`:PhpSearch <:PhpSearch>`
  [-p <pattern> -t <type> -s <scope> -x <context>] -
  Search for classes, methods, and constants.
- :ref:`:PhpSearchContext` -
  Find the element under the cursor based on its context.
- :ref:`:Validate <:Validate_php>` - Manually runs source code validation.


Python Commands
-----------------

- :ref:`:PythonFindDefinition <:PythonFindDefinition>` -
  Find the element under the cursor.
- :ref:`:PythonSearchContext` -
  Find the element under the cursor based on its context.
- :ref:`:Validate <:Validate_python>` - Validates the current file using pyflakes_.
- :ref:`:PyLint` - Runs pylint_ on the current file.
- :ref:`:PythonRegex` - Opens a window for testing python regular expressions.
- :ref:`:DjangoManage <:DjangoManage>` -
  Invokes django's manage.py from any file in the same directory as your
  manage.py or in any of the child directories.
- :ref:`:DjangoFind` -
  Available when editing a django html template file.  Finds tag/filter
  definition, other template files, and static files.
- :ref:`:DjangoTemplateOpen` -
  Available when editing a python file.  Finds the template referenced under
  the cursor.
- :ref:`:DjangoViewOpen` -
  Available when editing a python file.  When within a django url patterns
  definition, finds the view referenced under the cursor.
- :ref:`:DjangoContextOpen` -
  Available when editing a python file.  Executes **:DjangoViewOpen**,
  **:DjangoTemplateOpen**, or **:PythonSearchContext** depending on the
  context of the text under the cursor.


Ruby Commands
-----------------

- :ref:`:RubySearch <:RubySearch>`
  [-p <pattern> -t <type> -s <scope> -x <context>] -
  Search for modules, classes, methods, etc.
- :ref:`:RubySearchContext` -
  Find the element under the cursor based on its context.
- :ref:`:Validate <:Validate_ruby>` - Manually runs source code validation.


Vim Commands
-----------------

- :ref:`:FindCommandDef` [<command>] - Finds a command definition.
- :ref:`:FindCommandRef` [<command>] - Finds references of a command.
- :ref:`:FindFunctionDef` [<function>] - Finds a function definition.
- :ref:`:FindFunctionRef` [<function>] - Finds references of a function.
- :ref:`:FindVariableDef` [<variable>] -
  Finds the definition of a global variable.
- :ref:`:FindVariableRef` [<variable>] - Finds references of a global variable.
- :ref:`:FindByContext` -
  Finds command, function, or variable based on the context of the element
  under the cursor.
- :ref:`:VimDoc` [<keyword>] - Opens the vim help for a keyword.


WebXml Commands
-----------------

- :ref:`:Validate <:Validate_webxml>` - Validate the current web.xml file.


Xml Commands
-----------------

- :ref:`:DtdDefinition` [<element>] -
  Open the current xml file's dtd and jump to the element definition if
  supplied.
- :ref:`:XsdDefinition` [<element>] -
  Open the current xml file's xsd and jump to the element definition if
  supplied.
- :ref:`:Validate <:Validate_xml>` [<file>] -
  Validates the supplied xml file or the current file if none supplied.
- :ref:`:XmlFormat <:XmlFormat>` - Reformats the current xml file.


Xsd Commands
-----------------

- :ref:`:Validate <:Validate_xsd>` - Validate the current file.


Version Control Commands
-------------------------

.. note::

  Currently cvs, subversion, mercurial, and git are supported by the following
  commands where applicable.

- :ref:`:VcsAnnotate` -
  Toggles annotation of the currently versioned file using vim signs.
- :ref:`:VcsInfo` - Echos vcs info about the current versioned file.
- :ref:`:VcsLog` - Opens a buffer with log information for the current file.
- :ref:`:VcsDiff` [revision] -
  Performs a vertical diffsplit of the current file against the last
  committed revision of the current file or the revision supplied.
- :ref:`:VcsCat` [revision] -
  Splits the current file with the contents of the last committed version
  of the current file or the supplied revision.
- :ref:`:VcsWebLog` -
  Opens the log for the currently versioned file in the configured vcs web app.
- :ref:`:VcsWebAnnotate` [revision] -
  Opens the annotated view for the currently versioned file in the configured
  vcs web app.
- :ref:`:VcsWebChangeSet` [revision] -
  Opens the change set for the currently versioned file in the configured vcs
  web app.
- :ref:`:VcsWebDiff` [revision, revision] -
  Opens a diff view for the currently versioned file in the configured in the
  configured vcs web app.


Misc. Commands
-----------------

- :ref:`:LocateFile` [file] -
  Locates a relative file and opens it.
- :ref:`:Split` file [file ...] -
  Behaves like the 'split' command, but allows multiple files to be supplied.
- :ref:`:SplitRelative` file [file ...] -
  Like **:Split** this command provides splitting of multiple files, but this
  command splits file relative to the file in the current buffer.
- :ref:`:Tabnew` file [file ...] -
  Behaves like **:Split**, but issues a :tabnew on each file.
- :ref:`:TabnewRelative` - file [file ...] -
  Behaves like **:SplitRelative**, but issues a :tabnew on each file.
- :ref:`:EditRelative` file -
  Behaves like **:SplitRelative**, but issues an 'edit' and only supports one
  file at a time.
- :ref:`:ReadRelative` file -
  Behaves like **:SplitRelative**, but issues a 'read' and only supports one
  file at a time.
- :ref:`:ArgsRelative` file_pattern [ file_pattern ...] -
  Behaves like **:SplitRelative**, but executes 'args'.
- :ref:`:ArgAddRelative` file_pattern [ file_pattern ...] -
  Behaves like **:SplitRelative**, but executes 'argadd'.
- :ref:`:VimgrepRelative`
  /regex/ file_pattern [ file_pattern ...] -
  Executes :vimgrep relative to the current file.
- :ref:`:VimgrepAddRelative`
  /regex/ file_pattern [ file_pattern ...] -
  Executes :vimgrepadd relative to the current file.
- :ref:`:LvimgrepRelative`
  /regex/ file_pattern [ file_pattern ...] -
  Executes :lvimgrep relative to the current file.
- :ref:`:LvimgrepAddRelative`
  /regex/ file_pattern [ file_pattern ...] -
  Executes :lvimgrepadd relative to the current file.
- :ref:`:CdRelative` dir -
  Executes :cd relative to the current file.
- :ref:`:LcdRelative` dir -
  Executes :lcd relative to the current file.
- :ref:`:Tcd` dir -
  Like :lcd but sets the tab's local working directory.
- :ref:`:DiffLastSaved` -
  Performs a diffsplit with the last saved version of the currently modifed
  file.
- :ref:`:SwapWords` -
  Swaps two words (with cursor placed on the first word).  Supports swapping
  around non-word characters like commas, periods, etc.
- :ref:`:Sign` - Toggles adding or removing a vim sign on the current line.
- :ref:`:Signs` -
  Opens a new window containing a list of signs for the current buffer.
- :ref:`:SignClearUser` - Removes all vim signs added via :Sign.
- :ref:`:SignClearAll` - Removes all vim signs.
- :ref:`:QuickFixClear` -
  Removes all entries from the quick fix window.
- :ref:`:LocationListClear` -
  Removes all entries from the location list window.
- :ref:`:MaximizeWindow <:MaximizeWindow>` -
  Toggles maximization of the current window.
- :ref:`:MinimizeWindow <:MinimizeWindow>` [winnr ...] -
  Minimizes the current window or the windows corresponding to the window
  numbers supplied.
- :ref:`:MinimizeRestore <:MinimizeRestore>` - Restore all minimized windows.
- :ref:`:Buffers` -
  Opens a temporary window with a list of all the currently listed
  buffers, allowing you to open or remove them.
- :ref:`:BuffersToggle` -
  Opens the buffers window if not open, otherwise closes it.
- :ref:`:Only` -
  Closes all but the current window and any windows excluded by
  **g:EclimOnlyExclude**.
- :ref:`:OtherWorkingCopyDiff` <project> -
  Diffs the current file against the same file in another project (one which
  has the same project relative path).
- :ref:`:OtherWorkingCopyEdit` <project> -
  Like **:OtherWorkingCopyDiff**, except open the file in the current window.
- :ref:`:OtherWorkingCopySplit` <project> -
  Like **:OtherWorkingCopyDiff**, except open the file in a new window.
- :ref:`:OtherWorkingCopyTabopen` <project> -
  Like **:OtherWorkingCopyDiff**, except open the file in a new tab.
- :ref:`:History` -
  View the local history entries for the current file.
- :ref:`:HistoryClear` -
  Clear the local history entries for the current file.
- :ref:`:HistoryDiffNext` -
  Diff the current file against the next entry in the history stack.
- :ref:`:HistoryDiffPrev` -
  Diff the current file against the previous entry in the history stack.
- :ref:`:OpenUrl <:OpenUrl>` [url] - Opens a url in your configured web browser.

.. _pyflakes: http://www.divmod.org/trac/wiki/DivmodPyflakes
.. _pylint: http://www.logilab.org/857
