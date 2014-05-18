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

Cheatsheet
==========

Here you will find a comprehensive list of eclim commands that you can use as a
reference.


Global Commands
---------------

- :ref:`:PingEclim <:PingEclim>` - Pings eclimd server.
- :ref:`:ShutdownEclim <:ShutdownEclim>` - Shuts down eclimd server.
- :ref:`:VimSettings <:VimSettings>` -
  View / edit eclim's vim client settings.
- :ref:`:WorkspaceSettings <:WorkspaceSettings>` -
  View / edit global workspace settings.
- :ref:`:EclimDisable <:EclimDisable>` -
  Temporarily disables communication with eclimd.
- :ref:`:EclimEnable <:EclimEnable>` - Re-enables communication with eclimd.
- :ref:`:EclimHelp <:EclimHelp>` [helptopic] - View eclim documentation in vim.
- :ref:`:EclimHelpGrep <:EclimHelpGrep>` /regex/ -
  Search the eclim documentation in vim.


Project Commands
----------------

- :ref:`:ProjectCreate <:ProjectCreate>`
  <folder> [-p <project_name>] -n <nature> ... [-d <project_dependency> ...] -
  Create a new project.
- :ref:`:ProjectImport <:ProjectImport>` <folder> -
  Import a project from an existing eclipse project folder.
- :ref:`:ProjectList <:ProjectList>` - List current projects.
- :ref:`:ProjectSettings <:ProjectSettings>` [<project>] -
  View / edit project settings.
- :ref:`:ProjectDelete <:ProjectDelete>` <project> - Delete a project.
- :ref:`:ProjectRename <:ProjectRename>` [<project>] <name> - Rename a project.
- :ref:`:ProjectMove <:ProjectMove>` [<project>] <dir> - Move a project.
- :ref:`:ProjectRefresh <:ProjectRefresh>` [<project> <project> ...] -
  Refresh supplied list of projects against the current files on disk.  If
  no project names supplied, refresh the current project.
- :ref:`:ProjectRefreshAll <:ProjectRefreshAll>` - Refresh all projects.
- :ref:`:ProjectBuild <:ProjectBuild>` [<project>] -
  Build the current or supplied project.
- :ref:`:ProjectInfo <:ProjectInfo>` [<project>] -
  Echos info for the current or supplied project.
- :ref:`:ProjectOpen <:ProjectOpen>` [<project>] - Opens a project.
- :ref:`:ProjectClose <:ProjectClose>` [<project>] - Closes a project.
- :ref:`:ProjectNatures <:ProjectNatures>` [<project>] -
  View the configured natures for one or all projects.
- :ref:`:ProjectNatureAdd <:ProjectNatureAdd>` <project> [<nature> ...] -
  Add one or more natures to a project.
- :ref:`:ProjectNatureRemove <:ProjectNatureRemove>` <project> [<nature> ...] -
  Remove one or more natures from a project.
- :ref:`:ProjectProblems <:ProjectProblems>` [<project>] -
  Populates vim's quickfix with all eclipse build errors and warnings for the
  current, or specified project, and all related projects.
- :ref:`:ProjectCD <:ProjectCD>` -
  Changes the global current working directory to the root directory of the
  current file's project (executes a :cd).
- :ref:`:ProjectLCD <:ProjectLCD>` -
  Changes the current working directory of the current window to the root
  directory of the current file's project (executes a :lcd).
- :ref:`:ProjectTree <:ProjectTree>` [<project> <project> ...] -
  Opens navigable a tree for one or more projects.
- :ref:`:ProjectsTree <:ProjectsTree>` - Opens a tree containing all projects.
- :ref:`:ProjectTab <:ProjectTab>` <project> - Opens a new tab containing the project tree
  and the tab local working directory set to the supplied project's root.
- :ref:`:ProjectGrep <:ProjectGrep>` /<pattern>/ file_pattern [file_pattern ...] -
  Issues a vimgrep starting at the root of the current project.
- :ref:`:ProjectGrepAdd <:ProjectGrepAdd>` /<pattern>/ file_pattern [file_pattern ...] -
  Issues a vimgrepadd starting at the root of the current project.
- :ref:`:ProjectLGrep <:ProjectLGrep>` /<pattern>/ file_pattern [file_pattern ...] -
  Issues a lvimgrep starting at the root of the current project.
- :ref:`:ProjectLGrepAdd <:ProjectLGrepAdd>` /<pattern>/ file_pattern [file_pattern ...] -
  Issues a lvimgrepadd starting at the root of the current project.
- :ref:`:ProjectTodo <:ProjectTodo>` -
  Searches project files for todo / fixme patterns and adds them to the
  location list.
- :ref:`:Todo <:Todo>` -
  Searches the current file for todo / fixme patterns and adds them to the
  location list.


Android Commands
-----------------

- :ref:`:AndroidReload <:AndroidReload>` - Reloads the Android SDK environment in the
  running eclimd/eclipse instance. Useful if you've made changes to the SDK
  outside of eclipse (installed a new target platform, etc).


Ant Commands
------------

- :ref:`:Ant <:Ant>` [<target> ...] - Execute ant from the current project.
- :ref:`:AntDoc <:AntDoc>` [<element>] -
  Find and open the documentation for the element under the cursor or the
  element supplied as an argument.
- :ref:`:Validate <:Validate_ant>` - Validate the current ant build file.


C/C++ Commands
-----------------

- :ref:`:Validate <:Validate_c>` - Validate the current file.
- :ref:`:CSearch <:CSearch>` [-p <pattern> -t <type> -s <scope> -x <context>] -
  Search for classes, functions, methods, macros, etc.
- :ref:`:CSearchContext <:CSearchContext>` -
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
- :ref:`:BrowserOpen <:BrowserOpen>` -
  Opens the current file in your configured browser.


Groovy Commands
-----------------

- :ref:`:Validate <:Validate_groovy>` - Validate the current file.


Ivy Commands
-----------------

- :ref:`:IvyRepo <:IvyRepo>` <path> -
  Sets the necessary IVY_REPO classpath variable for supporting automatic
  updates to .classpath files upon ivy.xml updates.


Java Commands
-----------------

- :ref:`:JavaGet <:JavaGet>` - Create a java bean getter method.
- :ref:`:JavaSet <:JavaSet>` - Create a java bean setter method.
- :ref:`:JavaGetSet <:JavaGetSet>` -
  Create both a java bean getter and setter method.
- :ref:`:JavaConstructor <:JavaConstructor>` -
  Creates class constructor, either empty or based on selected class fields.
- :ref:`:JavaCallHierarchy <:JavaCallHierarchy>` -
  Display the call hierarchy for the method under the cursor.
- :ref:`:JavaHierarchy <:JavaHierarchy>` - View the type hierarchy tree.
- :ref:`:JavaImpl <:JavaImpl>` -
  View implementable / overridable methods from super classes and implemented
  interfaces.
- :ref:`:JavaDelegate <:JavaDelegate>` -
  View list of methods that delegate to the field under the cursor.
- :ref:`:JUnit <:JUnit>` [testcase] -
  Allows you to execute junit test cases.
- :ref:`:JUnitFindTest <:JUnitFindTest>` -
  Attempts to find the corresponding test for the current source file.
- :ref:`:JUnitImpl <:JUnitImpl>` -
  Similar to **:JavaImpl**, but creates test methods.
- :ref:`:JUnitResult <:JUnitResult>` [testcase] -
  Allows you to view the results of a test case.
- :ref:`:JavaImport <:JavaImport>` - Import the class under the cursor.
- :ref:`:JavaImportOrganize <:JavaImportOrganize>` -
  Import undefined types, remove unused imports, sort and format imports.
- :ref:`:JavaSearch <:JavaSearch>`
  [-p <pattern>] [-t <type>] [-x <context>] [-s <scope>] -
  Search for classes, methods, fields, etc.  (With pattern supplied, searches
  for the element under the cursor).
- :ref:`:JavaSearchContext <:JavaSearchContext>` -
  Perform a context sensitive search for the element under the cursor.
- :ref:`:JavaCorrect <:JavaCorrect>` -
  Suggest possible corrections for a source error.
- :ref:`:JavaDocSearch <:JavaDocSearch>` -
  Search for javadocs.  Same usage as **:JavaSearch**.
- :ref:`:JavaDocComment <:JavaDocComment>` -
  Adds or updates the comments for the element under the cursor.
- :ref:`:JavaDocPreview <:JavaDocPreview>` -
  Display the javadoc of the element under the cursor in vim's preview window.
- :ref:`:JavaRename <:JavaRename>` [new_name] -
  Rename the element under the cursor.
- :ref:`:JavaMove <:JavaMove>` [new_package] -
  Move the current class/interface to another package.
- :ref:`:Java <:Java>` -
  Executes the java using your project's main class.
- :ref:`:JavaClasspath <:JavaClasspath>` [-d <delim>] -
  Echos the project's classpath delimited by the system path separator or the
  supplied delimiter.
- :ref:`:Javadoc <:Javadoc>` [file, file, ...] -
  Executes the javadoc utility against all or just the supplied source files.
- :ref:`:JavaListInstalls <:JavaListInstalls>` - List known JDK/JRE installs.
- :ref:`:JavaFormat <:JavaFormat>` - Formats java source code.
- :ref:`:Checkstyle <:Checkstyle>` - Invokes checkstyle on the current file.
- :ref:`:Jps <:Jps>` -
  Opens window with information about the currently running java processes.
- :ref:`:Validate <:Validate_java>` - Manually runs source code validation.


Java .classpath Commands
------------------------

- :ref:`:NewSrcEntry <:NewSrcEntry_java>` <dir> -
  Add a new source dir entry.
- :ref:`:NewProjectEntry <:NewProjectEntry_java>` <project> -
  Add a new project entry.
- :ref:`:NewJarEntry <:NewJarEntry_java>` <file> [<src_path> <javadoc_path>] -
  Add a jar entry.
- :ref:`:NewVarEntry <:NewVarEntry_java>` <VAR/file> [<src_path> <javadoc_path>] -
  Add a new var entry.
- :ref:`:VariableList <:VariableList>`
  List available classpath variables and their corresponding values.
- :ref:`:VariableCreate <:VariableCreate>` <name> <path> -
  Creates or updates the variable with the supplied name.
- :ref:`:VariableDelete <:VariableDelete>` <name> -
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

- :ref:`:Maven <:Maven>` [<goal> ...] -
  Execute maven 1.x from the current project.
- :ref:`:Mvn <:Mvn>` [<goal> ...] - Execute maven 2.x from the current project.
- :ref:`:MavenRepo <:MavenRepo>` -
  Sets the necessary MAVEN_REPO classpath variable for maven's (1.x) eclipse
  support.
- :ref:`:MvnRepo <:MvnRepo>` -
  Sets the necessary M2_REPO classpath variable for maven's (2.x) eclipse
  support.


Php Commands
-----------------

- :ref:`:PhpSearch <:PhpSearch>`
  [-p <pattern> -t <type> -s <scope> -x <context>] -
  Search for classes, methods, and constants.
- :ref:`:PhpSearchContext <:PhpSearchContext>` -
  Find the element under the cursor based on its context.
- :ref:`:Validate <:Validate_php>` - Manually runs source code validation.


Python Commands
-----------------

- :ref:`:PythonSearch <:PythonSearch>` -
  Find the element under the cursor or, if requested, all references to that
  element.
- :ref:`:PythonSearchContext <:PythonSearchContext>` -
  Find the element under the cursor or its references based on the current
  context in the file.
- :ref:`:Validate <:Validate_python>` - Validates the current file.
- :ref:`:DjangoManage <:DjangoManage>` -
  Invokes django's manage.py from any file in the same directory as your
  manage.py or in any of the child directories.
- :ref:`:DjangoFind <:DjangoFind>` -
  Available when editing a django html template file.  Finds tag/filter
  definition, other template files, and static files.
- :ref:`:DjangoTemplateOpen <:DjangoTemplateOpen>` -
  Available when editing a python file.  Finds the template referenced under
  the cursor.
- :ref:`:DjangoViewOpen <:DjangoViewOpen>` -
  Available when editing a python file.  When within a django url patterns
  definition, finds the view referenced under the cursor.
- :ref:`:DjangoContextOpen <:DjangoContextOpen>` -
  Available when editing a python file.  Executes **:DjangoViewOpen**,
  **:DjangoTemplateOpen**, or **:PythonSearchContext** depending on the
  context of the text under the cursor.


Ruby Commands
-----------------

- :ref:`:RubySearch <:RubySearch>`
  [-p <pattern> -t <type> -s <scope> -x <context>] -
  Search for modules, classes, methods, etc.
- :ref:`:RubySearchContext <:RubySearchContext>` -
  Find the element under the cursor based on its context.
- :ref:`:Validate <:Validate_ruby>` - Manually runs source code validation.
- :ref:`:RubyInterpreterAdd <:RubyInterpreterAdd>` [-n <name>] <path> -
  Add a ruby interpreter.
- :ref:`:RubyInterpreterRemove <:RubyInterpreterRemove>` <path> -
  Remove a ruby interpreter.
- :ref:`:RubyInterpreterList <:RubyInterpreterList>`  -
  List the available ruby interpreters.


Scala Commands
-----------------

- :ref:`:ScalaSearch <:ScalaSearch>` -
  Search for the definition of the element under the cursor.
- :ref:`:ScalaImport <:ScalaImport>` - Import the type under the cursor.
- :ref:`:Validate <:Validate_scala>` - Manually runs source code validation.


WebXml Commands
-----------------

- :ref:`:Validate <:Validate_webxml>` - Validate the current web.xml file.


Xml Commands
-----------------

- :ref:`:DtdDefinition <:DtdDefinition>` [<element>] -
  Open the current xml file's dtd and jump to the element definition if
  supplied.
- :ref:`:XsdDefinition <:XsdDefinition>` [<element>] -
  Open the current xml file's xsd and jump to the element definition if
  supplied.
- :ref:`:Validate <:Validate_xml>` [<file>] -
  Validates the supplied xml file or the current file if none supplied.
- :ref:`:XmlFormat <:XmlFormat>` - Reformats the current xml file.


Xsd Commands
-----------------

- :ref:`:Validate <:Validate_xsd>` - Validate the current file.


Misc. Commands
-----------------

- :ref:`:LocateFile <:LocateFile>` [file] -
  Locates a relative file and opens it.
- :ref:`:Tcd <:Tcd>` dir -
  Like :lcd but sets the tab's local working directory.
- :ref:`:DiffLastSaved <:DiffLastSaved>` -
  Performs a diffsplit with the last saved version of the currently modifed
  file.
- :ref:`:SwapWords <:SwapWords>` -
  Swaps two words (with cursor placed on the first word). Supports swapping
  around non-word characters like commas, periods, etc.
- :ref:`:Sign <:Sign>` -
  Toggles adding or removing a vim sign on the current line.
- :ref:`:Signs <:Signs>` -
  Opens a new window containing a list of signs for the current buffer.
- :ref:`:SignClearUser <:SignClearUser>` -
  Removes all vim signs added via :Sign.
- :ref:`:SignClearAll <:SignClearAll>` - Removes all vim signs.
- :ref:`:QuickFixClear <:QuickFixClear>` -
  Removes all entries from the quick fix window.
- :ref:`:LocationListClear <:LocationListClear>` -
  Removes all entries from the location list window.
- :ref:`:Buffers <:Buffers>` -
  Opens a temporary window with a list of all the currently listed
  buffers, allowing you to open or remove them.
- :ref:`:BuffersToggle <:BuffersToggle>` -
  Opens the buffers window if not open, otherwise closes it.
- :ref:`:Only <:Only>` -
  Closes all but the current window and any windows excluded by
  **g:EclimOnlyExclude**.
- :ref:`:History <:History>` -
  View the local history entries for the current file.
- :ref:`:HistoryClear <:HistoryClear>` -
  Clear the local history entries for the current file.
- :ref:`:HistoryDiffNext <:HistoryDiffNext>` /
  :ref:`:HistoryDiffPrev <:HistoryDiffPrev>` -
  Diff the current file against the next/previous entry in the history stack.
- :ref:`:RefactorUndo <:RefactorUndo>` /
  :ref:`:RefactorRedo <:RefactorRedo>` -
  Undo / Redo the last refactoring.
- :ref:`:RefactorUndoPeek <:RefactorUndoPeek>` /
  :ref:`:RefactorRedoPeek <:RefactorRedoPeek>` -
  Display a short description of the refactoring to be undone / redone.
- :ref:`:OpenUrl <:OpenUrl>` [url] - Opens a url in your configured web browser.

.. _pylint: http://www.logilab.org/857
