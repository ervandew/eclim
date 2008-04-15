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

.. _vim/cheatsheet:

Cheatsheet
==========

Here you will find a comprehensive list of eclim commands that you can use as a
reference.


Global Commands
---------------

- <a href="index.html#PingEclim">**:PingEclim**</a> -
  Pings eclimd server.
- <a href="index.html#ShutdownEclim">**:ShutdownEclim**</a> -
  Shuts down eclimd server.
- <a href="index.html#EclimSettings">**:EclimSettings**</a> -
  View / edit global settings.


Project Commands
----------------

- <a href="common/project.html#ProjectCreate">**:ProjectCreate**</a>
  <folder> [-p <project_name>] -n <nature> ... [-d <project_dependency> ...]
  Create a new project.
- <a href="common/project.html#ProjectList">**:ProjectList**</a> -
  List current projects.
- <a href="common/project.html#ProjectSettings">**:ProjectSettings**</a>
  [<project>] - View / edit project settings.
- <a href="common/project.html#ProjectDelete">**:ProjectDelete**</a>
  <project> - Delete a project.
- <a href="common/project.html#ProjectRefresh">**:ProjectRefresh**</a>
  [<project> <project> ...] -
  Refresh supplied list of projects against the current files on disk.  If
  no project names supplied, refresh the current project.
- <a href="common/project.html#ProjectRefreshAll">**:ProjectRefreshAll**</a> -
  Refresh all projects.
- <a href="common/project.html#ProjectInfo">**:ProjectInfo**</a>
  [<project>] - Echos info for the current or supplied project.
- <a href="common/project.html#ProjectOpen">**:ProjectOpen**</a>
  <project> - Opens a project.
- <a href="common/project.html#ProjectClose">**:ProjectClose**</a>
  <project> - Closes a project.
- <a href="common/project.html#ProjectNatures">**:ProjectNatures**</a>
  [<project>] - View the configured natures for one or all projects.
- <a href="common/project.html#ProjectNatureAdd">**:ProjectNatureAdd**</a>
  <project> [<nature> ...] - Add one or more natures to a project.
- <a href="common/project.html#ProjectNatureRemove">**:ProjectNatureRemove**</a>
  <project> [<nature> ...] - Remove one or more natures from a project.
- <a href="common/project.html#ProjectCD">**:ProjectCD**</a> -
  Changes the global current working directory to the root directory of
  the current file's project (executes a :cd).
- <a href="common/project.html#ProjectLCD">**:ProjectLCD**</a> -
  Changes the current working directory of the current window to the
  root directory of the current file's project (executes a :lcd).
- <a href="common/project.html#ProjectTree">**:ProjectTree**</a>
  [<project> <project> ...] - Opens navigable a tree for one
  or more projects.
- <a href="common/project.html#ProjectsTree">**:ProjectsTree**</a> -
  Opens a tree containing all projects.
- <a href="common/project.html#ProjectGrep">**:ProjectGrep**</a>
  /<pattern>/ file_pattern [file_pattern ...] -
  Issues a vimgrep starting at the root of the current project.
- <a href="common/project.html#ProjectGrepAdd">**:ProjectGrepAdd**</a>
  /<pattern>/ file_pattern [file_pattern ...] -
  Issues a vimgrepadd starting at the root of the current project.
- <a href="common/project.html#ProjectLGrep">**:ProjectLGrep**</a>
  /<pattern>/ file_pattern [file_pattern ...] -
  Issues a lvimgrep starting at the root of the current project.
- <a href="common/project.html#ProjectLGrepAdd">**:ProjectLGrepAdd**</a>
  /<pattern>/ file_pattern [file_pattern ...] -
  Issues a lvimgrepadd starting at the root of the current project.


Ant Commands
------------

- <a href="java/ant/run.html#Ant">**:Ant**</a>
  [<target> ...] -
  Execute ant from the current project.
- <a href="java/ant/doc.html#AntDoc">**:AntDoc**</a>
  [<element>] -
  Find and open the documentation for the element under the cursor or the
  element supplied as an argument.
- <a href="java/ant/validate.html#Validate">**:Validate**</a> -
  Validate the current ant build file.


Css Commands
-----------------

- <a href="css/validate.html#Validate">**:Validate**</a> -
  Validate the current file.


Dtd Commands
-----------------

- <a href="dtd/validate.html#Validate">**:Validate**</a> -
  Validate the current file.


Html Commands
-----------------

- <a href="html/validate.html#Validate">**:Validate**</a> -
  Validate the current file.
- <a href="html/util.html#BrowserOpen">**:BrowserOpen**</a> -
  Opens the current file in your configured browser.


Ivy Commands
-----------------

- <a href="../guides/java/ivy/ivy_classpath.html#IvyRepo">**:IvyRepo**</a>
  <path>-
  Sets the necessary IVY_REPO classpath variable for supporting
  automatic updates to .classpath files upon ivy.xml updates.
- <a href="../guides/java/ivy/ivy_classpath.html#IvyDependencySearch">**:IvyDependencySearch**</a>
  <artifact> -
  Searches online repository and opens a window with results that can be
  added to the current ivy file by hitting <Enter> on a result.
  Available when editing an ``ivy.xml`` file.


Java Commands
-----------------

- <a href="java/bean.html#JavaGet">**:JavaGet**</a> -
  Create a java bean getter method.
- <a href="java/bean.html#JavaSet">**:JavaSet**</a> -
  Create a java bean setter method.
- <a href="java/bean.html#JavaGetSet">**:JavaGetSet**</a> -
  Create both a java bean getter and setter method.
- <a href="java/constructor.html#JavaConstructor">**:JavaConstructor**</a> -
  Creates class constructor, either empty or based on selected class
  fields.
- <a href="java/impl.html#JavaImpl">**:JavaImpl**</a> -
  View implementable / overridable methods from super classes and
  implemented interfaces.
- <a href="java/delegate.html#JavaDelegate">**:JavaDelegate**</a> -
  View list of methods that delegate to the field under the cursor.
- <a href="java/junit.html#JUnitImpl">**:JUnitImpl**</a> -
  Similar to **:JavaImpl**, but creates test methods.
- <a href="java/junit.html#JUnitExecute">**:JUnitExecute**</a> - [testcase]
  Allows you to execute test cases in your favorite build tool.
- <a href="java/junit.html#JUnitResult">**:JUnitResult**</a> - [testcase]
  Allows you to view the results of a test case.
- <a href="java/import.html#JavaImport">**:JavaImport**</a> -
  Import the class under the cursor.
- <a href="java/search.html#JavaSearch">**:JavaSearch**</a>
  [-p <pattern>] [-t <type>] [-x <context>] [-s <scope>]-
  Search for classes, methods, fields, etc.
  (With pattern supplied, searches for the element under the cursor).
- <a href="java/search.html#JavaSearchContext">**:JavaSearchContext**</a> -
  Perform a context sensitive search for the element under the cursor.
- <a href="java/correct.html#JavaCorrect">**:JavaCorrect**</a> -
  Suggest possible corrections for a source error.
- <a href="java/regex.html#JavaRegex">**:JavaRegex**</a> -
  Opens a window for testing java regular expressions.
- <a href="java/doc.html#JavaDocSearch">**:JavaDocSearch**</a> -
  Search for javadocs.  Same usage as **:JavaSearch**.
- <a href="java/doc.html#JavaDocComment">**:JavaDocComment**</a> -
  Adds or updates the comments for the element under the cursor.
- <a href="java/tools.html#Jps">**:Jps**</a> -
  Opens window with information about the currently running java
  processes.
- <a href="java/source.html#Validate">**:Validate**</a> -
  Manually runs source code validation.


Java .classpath Commands
------------------------

- <a href="java/classpath.html#NewSrcEntry">**:NewSrcEntry**</a>
  <dir> [<dir> ...] -
  Add a new source dir entry.
- <a href="java/classpath.html#NewProjectEntry">**:NewProjectEntry**</a>
  <project> [<project> ...] -
  Add a new project entry.
- <a href="java/classpath.html#NewJarEntry">**:NewJarEntry**</a>
  <file> [<file> ...] -
  Add a jar entry.
- <a href="java/classpath.html#NewVarEntry">**:NewVarEntry**</a>
  <VAR/file> [<VAR/file> ...] -
  Add a new var entry.
- <a href="java/classpath.html#VariableList">**:VariableList**</a>
  List available classpath variables and their corresponding values.
- <a href="java/classpath.html#VariableCreate">**:VariableCreate**</a>
  <name> <path> -
  Creates or updates the variable with the supplied name.
- <a href="java/classpath.html#VariableDelete">**:VariableDelete**</a>
  <name> -
  Deletes the variable with the supplied name.


Javascript Commands
--------------------

- <a href="javascript/validate.html#Validate">**:Validate**</a> -
  Validate the current javascript file.


Log4j Commands
-----------------

- <a href="java/log4j/validate.html#Validate">**:Validate**</a> -
  Validate the current log4j xml configuration file.


Maven Commands
-----------------

- <a href="java/maven/run.html#Maven">**:Maven**</a>
  [<goal> ...] -
  Execute maven 1.x from the current project.
- <a href="java/maven/run.html#Mvn">**:Mvn**</a>
  [<goal> ...] -
  Execute maven 2.x from the current project.
- <a href="../guides/java/maven/maven_classpath.html#MavenRepo">**:MavenRepo**</a>
  - Sets the necessary MAVEN_REPO classpath variable for maven's (1.x)
  eclipse support.
- <a href="../guides/java/maven/mvn_classpath.html#MvnRepo">**:MvnRepo**</a>
  - Sets the necessary M2_REPO classpath variable for maven's (2.x)
  eclipse support.
- <a href="java/maven/dependency.html#MavenDependencySearch">**:MavenDependencySearch**</a>
  <artifact> -
  Searches online repository and opens a window with results that can be
  added to the current project file by hitting <Enter> on a result.
  Available when editing a maven 1.x ``project.xml`` file.
- <a href="java/maven/dependency.html#MvnDependencySearch">**:MvnDependencySearch**</a>
  <artifact> -
  Searches online repository and opens a window with results that can be
  added to the current pom file by hitting <Enter> on a result.
  Available when editing a maven 2.x ``pom.xml`` file.


Php Commands
-----------------

- <a href="php/search.html#PhpSearch">**:PhpSearch**</a>
  -p <pattern> -t <type> [-s <scope>]-
  Search for classes, methods, and constants.
- <a href="php/search.html#PhpFindDefinition">**:PhpFindDefinition**</a>
  Find the element under the cursor.
- <a href="php/search.html#PhpSearchContext">**:PhpSearchContext**</a>
  Find the element under the cursor based on its context.
- <a href="php/validate.html#Validate">**:Validate**</a> -
  Manually runs source code validation.


Php .projectOption Commands
---------------------------

- <a href="php/includepath.html#NewLibEntry">**:NewLibEntry**</a>
  <file> [<file> ...] -
  Add a lib entry referencing an external folder.
- <a href="php/includepath.html#NewProjectEntry">**:NewProjectEntry**</a>
  <project> [<project> ...] -
  Add a new project entry.
- <a href="php/includepath.html#NewVarEntry">**:NewVarEntry**</a>
  <VAR/file> [<VAR/file> ...] -
  Add a new var entry.
- <a href="php/includepath.html#VariableList">**:VariableList**</a>
  List available include path variables and their corresponding values.
- <a href="php/includepath.html#VariableCreate">**:VariableCreate**</a>
  <name> <path> -
  Creates or updates the variable with the supplied name.
- <a href="php/includepath.html#VariableDelete">**:VariableDelete**</a>
  <name> -
  Deletes the variable with the supplied name.


Python Commands
-----------------

- <a href="python/validate.html#Validate">**:Validate**</a> -
  Validates the current file using <a href="site:pyflakes">pyflakes</a>.
- <a href="python/validate.html#PyLint">**:PyLint**</a> -
  Runs <a href="site:pyflakes">pylint</a> on the current file.
- <a href="python/regex.html#PythonRegex">**:PythonRegex**</a> -
  Opens a window for testing python regular expressions.
- <a href="python/django.html#DjangoManage">**:DjangoManage**</a> -
  Invokes django's manage.py from any file in the same directory as your
  manage.py or in any of the child directories.
- <a href="python/django.html#DjangoFind">**:DjangoFind**</a> -
  Available when editing a django html template file.  Finds tag/filter
  definition, other template files, and static files.
- <a href="python/django.html#DjangoTemplateOpen">**:DjangoTemplateOpen**</a> -
  Available when editing a python file.  Finds the template referenced
  under the cursor.
- <a href="python/django.html#DjangoViewOpen">**:DjangoViewOpen**</a> -
  Available when editing a python file.  When within a django url patterns
  definition, finds the view referenced under the cursor.
- <a href="python/django.html#DjangoContextOpen">**:DjangoContextOpen**</a> -
  Available when editing a python file.  Executes
  **:DjangoViewOpen**, **:DjangoTemplateOpen**,
  or **:PythonFindDefinition** depending on the context of
  the text under the cursor.


Vim Commands
-----------------

- <a href="vim/find.html#FindCommandDef">**:FindCommandDef**</a>
  [<command>] -
  Finds a command definition.
- <a href="vim/find.html#FindCommandRef">**:FindCommandRef**</a>
  [<command>] -
  Finds references of a command.
- <a href="vim/find.html#FindFunctionDef">**:FindFunctionDef**</a>
  [<function>] -
  Finds a function definition.
- <a href="vim/find.html#FindFunctionRef">**:FindFunctionRef**</a>
  [<function>] -
  Finds references of a function.
- <a href="vim/find.html#FindVariableDef">**:FindVariableDef**</a>
  [<variable>] -
  Finds the definition of a global variable.
- <a href="vim/find.html#FindVariableRef">**:FindVariableRef**</a>
  [<variable>] -
  Finds references of a global variable.
- <a href="vim/find.html#FindByContext">**:FindByContext**</a>
  Finds command, function, or variable based on the context of the element
  under the cursor.
- <a href="vim/doc.html#VimDoc">**:VimDoc**</a>
  [<keyword>] -
  Opens the vim help for a keyword.


WebXml Commands
-----------------

- <a href="java/webxml/validate.html#Validate">**:Validate**</a> -
  Validate the current web.xml file.


Wsdl Commands
-----------------

- <a href="dtd/validate.html#Validate">**:Validate**</a> -
  Validate the current file.


Xml Commands
-----------------

- <a href="xml/definition.html#DtdDefinition">**:DtdDefinition**</a>
  [<element>] -
  Open the current xml file's dtd and jump to the element definition if
  supplied.
- <a href="xml/definition.html#XsdDefinition">**:XsdDefinition**</a>
  [<element>] -
  Open the current xml file's xsd and jump to the element definition if
  supplied.
- <a href="xml/validate.html#Validate">**:Validate**</a>
  [<file>] -
  Validates the supplied xml file or the current file if none supplied.
- <a href="xml/format.html#XmlFormat">**:XmlFormat**</a>
  Reformats the current xml file.


Xsd Commands
-----------------

- <a href="dtd/validate.html#Validate">**:Validate**</a> -
  Validate the current file.


Version Control Commands
-------------------------

.. note::

  Currently cvs, subversion, and mercurial are supported by the following
  commands where applicable.

- <a href="common/vcs.html#Viewvc">**:Viewvc**</a> [file] -
  Opens the <a href="site:viewvc">ViewVc</a> url to the supplied file or
  directory, or to the current buffer if no argument supplied.
- <a href="common/vcs.html#ViewvcAnnotate">**:ViewvcAnnotate**</a>
  [revision] -
  Opens the <a href="site:viewvc">ViewVc</a> url for the current file in
  the annotation view for the last committed revision or the revision
  supplied.
- <a href="common/vcs.html#ViewvcChangeSet">**:ViewvcChangeSet**</a>
  [revision] -
  Opens the <a href="site:viewvc">ViewVc</a> url for the change set of
  the last committed revision of the current file or the revision
  supplied.
- <a href="common/vcs.html#ViewvcDiff">**:ViewvcDiff**</a>
  [revision, revision] -
  Opens the <a href="site:viewvc">ViewVc</a> url for the current file's
  revision diffed against the previous revision, the revision supplied,
  or a diff of the two revision numbers supplied.
- <a href="common/vcs.html#VcsAnnotate">**:VcsAnnotate**</a> -
  Toggles annotation of the currently versioned file using vim signs.
- <a href="common/vcs.html#VcsInfo">**:VcsInfo**</a> -
  Echos vcs info about the current versioned file.
- <a href="common/vcs.html#VcsLog">**:VcsLog**</a> -
  Opens a buffer with log information for the current file.
- <a href="common/vcs.html#VcsChangeSet">**:VcsChangeSet**</a>
  [revision] -
  Opens a buffer with change set information for the supplied
  repository version or the current revision of the currently open file.
- <a href="common/vcs.html#VcsDiff">**:VcsDiff**</a>
  [revision] -
  Performs a vertical diffsplit of the current file against the last
  committed revision of the current file or the revision supplied.
- <a href="common/vcs.html#VcsCat">**:VcsCat**</a>
  [revision] -
  Splits the current file with the contents of the last committed version
  of the current file or the supplied revision.


Web Lookup Commands
--------------------

- <a href="common/web.html#OpenUrl">**:OpenUrl**</a> [url] -
  Opens a url in your configured web browser.
- <a href="common/web.html#Google">**:Google**</a> [word ...] -
  Looks up a word or phrase with google.
- <a href="common/web.html#Clusty">**:Clusty**</a> [word ...] -
  Looks up a word or phrase with clusty.
- <a href="common/web.html#Wikipedia">**:Wikipedia**</a> [word ...] -
  Looks up a word or phrase on wikipedia.
- <a href="common/web.html#Dictionary">**:Dictionary**</a> [word] -
  Looks up a word on dictionary.reference.com.
- <a href="common/web.html#Thesaurus">**:Thesaurus**</a> [word] -
  Looks up a word on thesaurus.reference.com.


Misc. Commands
-----------------

- <a href="common/util.html#LocateFileEdit">**:LocateFileEdit**</a> [file] -
  Locates a relative file and opens it via :edit.
- <a href="common/util.html#LocateFileSplit">**:LocateFileSplit**</a> [file] -
  Locates a relative file and opens it via :split.
- <a href="common/util.html#LocateFileTab">**:LocateFileTab**</a> [file] -
  Locates a relative file and opens it via :tabnew.
- <a href="common/util.html#Split">**:Split**</a>
  file [file ...] -
  Behaves like the 'split' command, but allows multiple files to be
  supplied.
- <a href="common/util.html#SplitRelative">**:SplitRelative**</a>
  file [file ...] -
  Like **:Split** this command provides splitting of multiple
  files, but this command splits file relative to the file in the current
  buffer.
- <a href="common/util.html#Tabnew">**:Tabnew**</a>
  file [file ...] -
  Behaves like **:Split**, but issues a :tabnew on each file.
- <a href="common/util.html#TabnewRelative">**:TabnewRelative**</a> -
  file [file ...] -
  Behaves like **:SplitRelative**, but issues a :tabnew on
  each file.
- <a href="common/util.html#EditRelative">**:EditRelative**</a>
  file -
  Behaves like **:SplitRelative**, but issues an 'edit' and only
  supports one file at a time.
- <a href="common/util.html#ReadRelative">**:ReadRelative**</a>
  file -
  Behaves like **:SplitRelative**, but issues a 'read' and only
  supports one file at a time.
- <a href="common/util.html#ArgsRelative">**:ArgsRelative**</a>
  file_pattern [ file_pattern ...] -
  Behaves like **:SplitRelative**, but executes 'args'.
- <a href="common/util.html#ArgAddRelative">**:ArgAddRelative**</a>
  file_pattern [ file_pattern ...] -
  Behaves like **:SplitRelative**, but executes 'argadd'.
- <a href="common/util.html#VimgrepRelative">**:VimgrepRelative**</a>
  /regex/ file_pattern [ file_pattern ...] -
  Executes :vimgrep relative to the current file.
- <a href="common/util.html#VimgrepAddRelative">**:VimgrepAddRelative**</a>
  /regex/ file_pattern [ file_pattern ...] -
  Executes :vimgrepadd relative to the current file.
- <a href="common/util.html#LvimgrepRelative">**:LvimgrepRelative**</a>
  /regex/ file_pattern [ file_pattern ...] -
  Executes :lvimgrep relative to the current file.
- <a href="common/util.html#LvimgrepAddRelative">**:LvimgrepAddRelative**</a>
  /regex/ file_pattern [ file_pattern ...] -
  Executes :lvimgrepadd relative to the current file.
- <a href="common/util.html#CdRelative">**:CdRelative**</a>
  dir -
  Executes :cd relative to the current file.
- <a href="common/util.html#LcdRelative">**:LcdRelative**</a>
  dir -
  Executes :lcd relative to the current file.
- <a href="common/util.html#DiffLastSaved">**:DiffLastSaved**</a> -
  Performs a diffsplit with the last saved version of the currently
  modifed file.
- <a href="common/util.html#SwapWords">**:SwapWords**</a> -
  Swaps two words (with cursor placed on the first word).
  Supports swapping around non-word characters like commas, periods, etc.
- <a href="common/util.html#Sign">**:Sign**</a> -
  Toggles adding or removing a vim sign on the current line.
- <a href="common/util.html#Signs">**:Signs**</a> -
  Opens a new window containing a list of signs for the current buffer.
- <a href="common/util.html#SignClearUser">**:SignClearUser**</a> -
  Removes all vim signs added via :Sign.
- <a href="common/util.html#SignClearAll">**:SignClearAll**</a> -
  Removes all vim signs.
- <a href="common/util.html#QuickFixClear">**:QuickFixClear**</a> -
  Removes all entries from the quick fix window.
- <a href="common/util.html#LocationListClear">**:LocationListClear**</a> -
  Removes all entries from the location list window.
- <a href="common/maximize.html#MaximizeWindow">**:MaximizeWindow**</a> -
  Toggles maximization of the current window.
- <a href="common/maximize.html#MinimizeWindow">**:MinimizeWindow**</a> [winnr ...] -
  Minimizes the current window or the windows corresponding to the window
  numbers supplied.
- <a href="common/maximize.html#MinimizeRestore">**:MinimizeRestore**</a> -
  Restore all minimized windows.
- <a href="common/util.html#Buffers">**:Buffers**</a> -
  Opens a temporary window with a list of all the currently listed
  buffers, allowing you to open or remove them.
- <a href="common/util.html#Only">**:Only**</a> -
  Closes all but the current window and any windows excluded by
  **g:EclimOnlyExclude**.
